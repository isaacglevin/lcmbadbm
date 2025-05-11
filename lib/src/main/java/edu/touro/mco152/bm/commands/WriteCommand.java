package edu.touro.mco152.bm.commands;


import edu.touro.mco152.bm.App;
import edu.touro.mco152.bm.BenchmarkUI;
import edu.touro.mco152.bm.DiskMark;
import edu.touro.mco152.bm.Util;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.persist.EM;
import jakarta.persistence.EntityManager;
import edu.touro.mco152.bm.ui.Gui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command implementation that performs a write benchmark test.
 * Encapsulates all parameters and execution logic necessary to run a disk write benchmark.
 */

public class WriteCommand implements Command {

    private final int numMarks;
    private final int numBlocks;
    private final int blockSizeKb;
    private final DiskRun.BlockSequence blockSequence;
    private final BenchmarkUI ui;
    private final boolean multiFile;
    private final boolean writeSyncEnable;
    private final File dataDir;
    private final int startFileNum;

    public WriteCommand(int numMarks, int numBlocks, int blockSizeKb,
                        DiskRun.BlockSequence blockSequence,
                        BenchmarkUI ui,
                        boolean multiFile,
                        boolean writeSyncEnable,
                        File dataDir,
                        int startFileNum) {
        this.numMarks = numMarks;
        this.numBlocks = numBlocks;
        this.blockSizeKb = blockSizeKb;
        this.blockSequence = blockSequence;
        this.ui = ui;
        this.multiFile = multiFile;
        this.writeSyncEnable = writeSyncEnable;
        this.dataDir = dataDir;
        this.startFileNum = startFileNum;
    }

    /**
     * Executes the write benchmark.
     * Writes data to disk based on the provided configuration, tracks performance,
     * updates the UI, and persists results.
     */

    @Override
    public DiskRun execute() {
        DiskRun run = new DiskRun(DiskRun.IOMode.WRITE, blockSequence);
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
        for (int b = 0; b < blockArr.length; b++) {
            if (b % 2 == 0) blockArr[b] = (byte) 0xFF;
        }

        int wUnitsComplete = 0;
        int unitsTotal = numMarks * numBlocks;

        for (int m = startFileNum; m < startFileNum + numMarks && !ui.isItCancelled(); m++) {
            testFile = new File(dataDir.getAbsolutePath() + File.separator +
                    (multiFile ? "testdata" + m : "testdata") + ".jdm");

            DiskMark wMark = new DiskMark(DiskMark.MarkType.WRITE);
            wMark.setMarkNum(m);
            long startTime = System.nanoTime();
            long totalBytesWrittenInMark = 0;

            String mode = writeSyncEnable ? "rwd" : "rw";

            try (RandomAccessFile rAccFile = new RandomAccessFile(testFile, mode)) {
                for (int b = 0; b < numBlocks; b++) {
                    if (blockSequence == DiskRun.BlockSequence.RANDOM) {
                        int rLoc = Util.randInt(0, numBlocks - 1);
                        rAccFile.seek((long) rLoc * blockSize);
                    } else {
                        rAccFile.seek((long) b * blockSize);
                    }
                    rAccFile.write(blockArr, 0, blockSize);
                    totalBytesWrittenInMark += blockSize;
                    wUnitsComplete++;
                    float percentComplete = (float) wUnitsComplete / unitsTotal * 100f;
                    ui.setTheProgress((int) percentComplete);
                }
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

            long endTime = System.nanoTime();
            long elapsedTimeNs = endTime - startTime;
            double sec = (double) elapsedTimeNs / 1_000_000_000.0;
            double mbWritten = (double) totalBytesWrittenInMark / App.MEGABYTE;
            wMark.setBwMbSec(mbWritten / sec);
            System.out.println("m:" + m + " write IO is " + wMark.getBwMbSecAsString() + " MB/s     "
                    + "(" + Util.displayString(mbWritten) + "MB written in "
                    + Util.displayString(sec) + " sec)");
            App.updateMetrics(wMark);

            ui.publishIt(wMark);

            run.setRunMax(wMark.getCumMax());
            run.setRunMin(wMark.getCumMin());
            run.setRunAvg(wMark.getCumAvg());
            run.setEndTime(new Date());
        }

        run.setEndTime(new Date());
        SimpleExecutor.getInstance().notifyObservers(run);
        return run;

    }
}
