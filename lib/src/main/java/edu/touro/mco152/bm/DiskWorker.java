package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commands.Command;
import edu.touro.mco152.bm.commands.ReadCommand;
import edu.touro.mco152.bm.commands.SimpleExecutor;
import edu.touro.mco152.bm.commands.WriteCommand;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import edu.touro.mco152.bm.ui.Gui;

import jakarta.persistence.EntityManager;
import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.touro.mco152.bm.App.*;
import static edu.touro.mco152.bm.DiskMark.MarkType.READ;
import static edu.touro.mco152.bm.DiskMark.MarkType.WRITE;

/**
 * Run the disk benchmarking as a Swing-compliant thread (only one of these threads can run at
 * once.) Cooperates with Swing to provide and make use of interim and final progress and
 * information, which is also recorded as needed to the persistence store, and log.
 * <p>
 * Depends on static values that describe the benchmark to be done having been set in App and Gui classes.
 * The DiskRun class is used to keep track of and persist info about each benchmark at a higher level (a run),
 * while the DiskMark class described each iteration's result, which is displayed by the UI as the benchmark run
 * progresses.
 * <p>
 * This class only knows how to do 'read' or 'write' disk benchmarks. It is instantiated by the
 * startBenchmark() method.
 * <p>
 * To be Swing compliant this class extends SwingWorker and declares that its final return (when
 * doInBackground() is finished) is of type Boolean, and declares that intermediate results are communicated to
 * Swing using an instance of the DiskMark class.
 */

public class DiskWorker {

    private static BenchmarkUI benchmarkUI;

    public DiskWorker(BenchmarkUI benchmarkUI) {
        DiskWorker.benchmarkUI = benchmarkUI;
    }

    // Record any success or failure status returned from SwingWorker (might be us or super)
    static Boolean lastStatus = null;  // so far unknown


    public static Boolean beginDiskWorker() throws Exception {

        /*
          We 'got here' because: 1: End-user clicked 'Start' on the benchmark UI,
          which triggered the start-benchmark event associated with the App::startBenchmark()
          method.  2: startBenchmark() then instantiated a DiskWorker, and called
          its (super class's) execute() method, causing Swing to eventually
          call this doInBackground() method.
         */
        Logger.getLogger(App.class.getName()).log(Level.INFO, "*** New worker thread started ***");
        msg("Running readTest " + App.readTest + "   writeTest " + App.writeTest);
        msg("num files: " + App.numOfMarks + ", num blks: " + App.numOfBlocks
                + ", blk size (kb): " + App.blockSizeKb + ", blockSequence: " + App.blockSequence);

        /*
          init local vars that keep track of benchmarks, and a large read/write buffer
         */
        int wUnitsComplete = 0, rUnitsComplete = 0, unitsComplete;
        int wUnitsTotal = App.writeTest ? numOfBlocks * numOfMarks : 0;
        int rUnitsTotal = App.readTest ? numOfBlocks * numOfMarks : 0;
        int unitsTotal = wUnitsTotal + rUnitsTotal;
        float percentComplete;

        int blockSize = blockSizeKb * KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) {
                blockArr[b] = (byte) 0xFF;
            }
        }

        DiskMark wMark, rMark;  // declare vars that will point to objects used to pass progress to UI

        Gui.updateLegend();  // init chart legend info

        if (App.autoReset) {
            App.resetTestData();
            Gui.resetTestData();
        }

        int startFileNum = App.nextMarkNumber;

        /*
          The GUI allows a Write, Read, or both types of BMs to be started. They are done serially.
         */

        /**
         * If the write benchmark test is enabled, this method will create and execute a WriteCommand
         * using the Command Pattern. This method encapsulates all the write test parameters and
         * allows the command to be executed by a simple executor.
         */


        if (App.writeTest) {
            Command writeCommand = new WriteCommand(
                    App.numOfMarks,
                    App.numOfBlocks,
                    App.blockSizeKb,
                    App.blockSequence,
                    benchmarkUI,
                    App.multiFile,
                    App.writeSyncEnable,
                    App.dataDir,
                    App.nextMarkNumber
            );
            new SimpleExecutor().executeCommand(writeCommand);
        }



        /*
          Most benchmarking systems will try to do some cleanup in between 2 benchmark operations to
          make it more 'fair'. For example a networking benchmark might close and re-open sockets,
          a memory benchmark might clear or invalidate the Op Systems TLB or other caches, etc.
         */

        // try renaming all files to clear catch
        if (App.readTest && App.writeTest && !benchmarkUI.isItCancelled()) {
            benchmarkUI.showMessage();
        }

        // Same as above, just for Read operations instead of Writes.

        /**
         * If the read benchmark test is enabled, this method will create and execute a ReadCommand
         * using the Command Pattern. This method encapsulates all the read test parameters and
         * allows the command to be executed by a simple executor.
         */
        if (App.readTest) {
            Command readCommand = new ReadCommand(
                    App.numOfMarks,
                    App.numOfBlocks,
                    App.blockSizeKb,
                    App.blockSequence,
                    benchmarkUI,
                    App.multiFile,
                    App.dataDir,
                    App.nextMarkNumber
            );
            new SimpleExecutor().executeCommand(readCommand);
        }

        App.nextMarkNumber += App.numOfMarks;
        return true;
    }

    /**
     * Process a list of 'chunks' that have been processed, ie that our thread has previously
     * published to Swing. For my info, watch Professor Cohen's video -
     * Module_6_RefactorBadBM Swing_DiskWorker_Tutorial.mp4
     * @param markList a list of DiskMark objects reflecting some completed benchmarks
     */
    public static void process(List<DiskMark> markList) {
        markList.stream().forEach((dm) -> {
            if (dm.type == DiskMark.MarkType.WRITE) {
                Gui.addWriteMark(dm);
            } else {
                Gui.addReadMark(dm);
            }
        });
    }


    protected static void done() {
        // Obtain final status, might from doInBackground ret value, or SwingWorker error
        try {
            lastStatus = benchmarkUI.getStatus();   // record for future access
        } catch (Exception e) {
            Logger.getLogger(App.class.getName()).warning("Problem obtaining final status: " + e.getMessage());
        }

        if (App.autoRemoveData) {
            Util.deleteDirectory(dataDir);
        }
        App.state = App.State.IDLE_STATE;
        Gui.mainFrame.adjustSensitivity();
    }

    public void cancel(boolean b) {
        benchmarkUI.cancelIt(b);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        benchmarkUI.addThePropertyChangeListener(listener);
    }

    public void execute() throws Exception {
        benchmarkUI.start();
    }

    public void publish() {
        benchmarkUI.publishIt();
    }
}