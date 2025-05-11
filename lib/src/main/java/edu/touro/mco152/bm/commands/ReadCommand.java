package edu.touro.mco152.bm.commands;

import edu.touro.mco152.bm.*;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import edu.touro.mco152.bm.ui.Gui;
import jakarta.persistence.EntityManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Command implementation that performs a read benchmark test.
 * It encapsulates all parameters and execution logic necessary in order to run a disk read benchmark.
 */
public class ReadCommand implements Command {

    private final int numMarks;
    private final int numBlocks;
    private final int blockSizeKb;
    private final DiskRun.BlockSequence blockSequence;
    private final BenchmarkUI ui;
    private final boolean multiFile;
    private final File dataDir;
    private final int startFileNum;

    /**
     * Constructs a ReadCommand with the given parameters.
     *
     * @param numMarks      Number of benchmark marks (iterations).
     * @param numBlocks     Number of blocks per mark.
     * @param blockSizeKb   Size of each block in kilobytes.
     * @param blockSequence Sequence mode (SEQUENTIAL or RANDOM).
     * @param ui            UI handler for progress and result updates.
     * @param multiFile     Whether to read from separate files per mark.
     * @param dataDir       Directory where benchmark files are located.
     * @param startFileNum  Starting index number for file naming.
     */
    public ReadCommand(int numMarks, int numBlocks, int blockSizeKb,
                       DiskRun.BlockSequence blockSequence,
                       BenchmarkUI ui,
                       boolean multiFile,
                       File dataDir,
                       int startFileNum) {
        this.numMarks = numMarks;
        this.numBlocks = numBlocks;
        this.blockSizeKb = blockSizeKb;
        this.blockSequence = blockSequence;
        this.ui = ui;
        this.multiFile = multiFile;
        this.dataDir = dataDir;
        this.startFileNum = startFileNum;
    }

    /**
     * Executes the read benchmark.
     * Reads data from disk using the specified configuration,
     * tracks performance, updates the UI, and persists results.
     */
    @Override
    public DiskRun execute() {
        DiskRun run = new DiskRun(DiskRun.IOMode.READ, blockSequence);
        run.setNumMarks(numMarks);
        run.setNumBlocks(numBlocks);
        run.setBlockSize(blockSizeKb);
        run.setTxSize((long) blockSizeKb * numBlocks * numMarks);
        run.setDiskInfo(Util.getDiskInfo(dataDir));

        System.out.println("disk info: (" + run.getDiskInfo() + ")");

        if (Gui.chartPanel != null) {
            Gui.chartPanel.getChart().getTitle().setVisible(true);
            Gui.chartPanel.getChart().getTitle().setText(run.getDiskInfo());
        }


        File testFile;
        int blockSize = blockSizeKb * App.KILOBYTE;
        byte[] blockArr = new byte[blockSize];
        int rUnitsComplete = 0;
        int unitsTotal = numMarks * numBlocks;

        for (int m = startFileNum; m < startFileNum + numMarks && !ui.isItCancelled(); m++) {
            testFile = new File(dataDir.getAbsolutePath() + File.separator +
                    (multiFile ? "testdata" + m : "testdata") + ".jdm");

            DiskMark rMark = new DiskMark(DiskMark.MarkType.READ);
            rMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesReadInMark = 0;

            try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, "r")) {
                for (int b = 0; b < numBlocks; b++) {
                    if (blockSequence == DiskRun.BlockSequence.RANDOM) {
                        int rLoc = Util.randInt(0, numBlocks - 1);
                        rAccFile.seek((long) rLoc * blockSize);
                    } else {
                        rAccFile.seek((long) b * blockSize);
                    }
                    rAccFile.readFully(blockArr, 0, blockSize);
                    totalBytesReadInMark += blockSize;
                    rUnitsComplete++;
                    float percentComplete = (float) rUnitsComplete / unitsTotal * 100f;
                    ui.setTheProgress((int) percentComplete);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Unable to read: file not found. Did you run a write test first?");
                return null;
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

            long endTime = System.nanoTime();
            double sec = (endTime - startTime) / 1_000_000_000.0;
            double mbRead = (double) totalBytesReadInMark / App.MEGABYTE;
            rMark.setBwMbSec(mbRead / sec);
            System.out.println("m:" + m + " READ IO is " + rMark.getBwMbSec() + " MB/s    "
                    + "(MBread " + mbRead + " in " + sec + " sec)");
            App.updateMetrics(rMark);
            ui.publishIt(rMark);

            run.setRunMax(rMark.getCumMax());
            run.setRunMin(rMark.getCumMin());
            run.setRunAvg(rMark.getCumAvg());
            run.setEndTime(new Date());
        }

        run.setEndTime(new Date());
        SimpleExecutor.getInstance().notifyObservers(run);
        return run;

    }
}
