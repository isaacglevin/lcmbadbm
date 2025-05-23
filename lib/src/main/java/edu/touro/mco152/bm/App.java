package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commands.SimpleExecutor;
import edu.touro.mco152.bm.persist.DiskRun;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import edu.touro.mco152.bm.ui.SelectFrame;

import javax.swing.SwingWorker.StateValue;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code App} class serves as the main entry point for the BadBM benchmarking tool.
 * <p>
 * This class is responsible for initializing the application, handling configuration settings,
 * managing global variables, and coordinating the execution of disk benchmarking tests.
 * It provides methods for setting up and saving configuration, running benchmarks,
 * and interacting with the graphical user interface.
 * This is the class that gets run to start the program.
 * </p>
 */
public class App {

    public static final String APP_CACHE_DIR = System.getProperty("user.home") + File.separator + ".jDiskMark";
    public static final String PROPERTIESFILE = "jdm.properties";
    public static final String DATADIRNAME = "jDiskMarkData";
    public static final int MEGABYTE = 1024 * 1024;
    public static final int KILOBYTE = 1024;
    public static final int IDLE_STATE = 0;
    public static final int DISK_TEST_STATE = 1;
    public static State state = State.IDLE_STATE;
    public static Properties p;
    public static File locationDir = null;
    public static File dataDir = null;
    public static File testFile = null;
    // options
    public static boolean multiFile = true;
    public static boolean autoRemoveData = false;
    public static boolean autoReset = true;
    public static boolean showMaxMin = true;
    public static boolean writeSyncEnable = true;
    // run configuration
    public static boolean readTest = false;
    public static boolean writeTest = true;
    public static DiskRun.BlockSequence blockSequence = DiskRun.BlockSequence.SEQUENTIAL;
    public static int numOfMarks = 25;      // desired number of marks
    public static int numOfBlocks = 32;     // desired number of blocks
    public static int blockSizeKb = 512;    // size of a block in KBs
    public static DiskWorker worker = null;
    public static int nextMarkNumber = 1;   // number of the next mark
    public static double wMax = -1, wMin = -1, wAvg = -1;
    public static double rMax = -1, rMin = -1, rAvg = -1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /* Set the Nimbus look and feel */
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
             */
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            //</editor-fold>
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(App::init);
    }

    /**
     * Get the version from the build properties. Defaults to 0.0 if not found.
     *
     * @return
     */
    public static String getVersion() {
        Properties bp = new Properties();
        String version = "0.0";
        try {
            bp.load(new FileInputStream("build.properties"));
            version = bp.getProperty("version");
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        return version;
    }

    /**
     * Initialize the GUI Application.
     */
    public static void init() {
        Gui.mainFrame = new MainFrame();
        Gui.selFrame = new SelectFrame();
        p = new Properties();
        loadConfig();
        System.out.println(App.getConfigString());
        Gui.mainFrame.refreshConfig();
        Gui.mainFrame.setLocationRelativeTo(null);
        Gui.progressBar = Gui.mainFrame.getProgressBar();

        // configure the embedded DB in .jDiskMark
        System.setProperty("derby.system.home", APP_CACHE_DIR);
        loadSavedRuns();

        Gui.mainFrame.setVisible(true);

        // save configuration on exit...
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                App.saveConfig();
            }
        });
    }

    /**
     * Loads most recent configuration settings from a file, and saves them to
     * the local Properties field p
     */
    public static void loadConfig() {
        File pFile = new File(PROPERTIESFILE);
        if (!pFile.exists()) {
            return;
        }
        try {
            InputStream is = new FileInputStream(pFile);
            p.load(is);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        String value;
        value = p.getProperty("locationDir", System.getProperty("user.home"));
        locationDir = new File(value);
        value = p.getProperty("multiFile", String.valueOf(multiFile));
        multiFile = Boolean.valueOf(value);
        value = p.getProperty("autoRemoveData", String.valueOf(autoRemoveData));
        autoRemoveData = Boolean.valueOf(value);
        value = p.getProperty("autoReset", String.valueOf(autoReset));
        autoReset = Boolean.valueOf(value);
        value = p.getProperty("blockSequence", String.valueOf(blockSequence));
        blockSequence = DiskRun.BlockSequence.valueOf(value);
        value = p.getProperty("showMaxMin", String.valueOf(showMaxMin));
        showMaxMin = Boolean.valueOf(value);
        value = p.getProperty("numOfFiles", String.valueOf(numOfMarks));
        numOfMarks = Integer.valueOf(value);
        value = p.getProperty("numOfBlocks", String.valueOf(numOfBlocks));
        numOfBlocks = Integer.valueOf(value);
        value = p.getProperty("blockSizeKb", String.valueOf(blockSizeKb));
        blockSizeKb = Integer.valueOf(value);
        value = p.getProperty("writeTest", String.valueOf(writeTest));
        writeTest = Boolean.valueOf(value);
        value = p.getProperty("readTest", String.valueOf(readTest));
        readTest = Boolean.valueOf(value);
        value = p.getProperty("writeSyncEnable", String.valueOf(writeSyncEnable));
        writeSyncEnable = Boolean.valueOf(value);
    }

    /**
     * Saves the current configuration that will
     * be used upon restarting jDiskMark.
     */
    public static void saveConfig() {
        p.setProperty("locationDir", App.locationDir.getAbsolutePath());
        p.setProperty("multiFile", String.valueOf(multiFile));
        p.setProperty("autoRemoveData", String.valueOf(autoRemoveData));
        p.setProperty("autoReset", String.valueOf(autoReset));
        p.setProperty("blockSequence", String.valueOf(blockSequence));
        p.setProperty("showMaxMin", String.valueOf(showMaxMin));
        p.setProperty("numOfFiles", String.valueOf(numOfMarks));
        p.setProperty("numOfBlocks", String.valueOf(numOfBlocks));
        p.setProperty("blockSizeKb", String.valueOf(blockSizeKb));
        p.setProperty("writeTest", String.valueOf(writeTest));
        p.setProperty("readTest", String.valueOf(readTest));
        p.setProperty("writeSyncEnable", String.valueOf(writeSyncEnable));

        try {
            OutputStream out = new FileOutputStream(new File(PROPERTIESFILE));
            p.store(out, "jDiskMark Properties File");
        } catch (IOException ex) {
            Logger.getLogger(SelectFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Gets data about the configuration and returns it in String form.
     *
     * @return a comprehensive description of the configuration
     */
    public static String getConfigString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Config for Java Disk Mark ").append(getVersion()).append('\n');
        sb.append("readTest: ").append(readTest).append('\n');
        sb.append("writeTest: ").append(writeTest).append('\n');
        sb.append("locationDir: ").append(locationDir).append('\n');
        sb.append("multiFile: ").append(multiFile).append('\n');
        sb.append("autoRemoveData: ").append(autoRemoveData).append('\n');
        sb.append("autoReset: ").append(autoReset).append('\n');
        sb.append("blockSequence: ").append(blockSequence).append('\n');
        sb.append("showMaxMin: ").append(showMaxMin).append('\n');
        sb.append("numOfFiles: ").append(numOfMarks).append('\n');
        sb.append("numOfBlocks: ").append(numOfBlocks).append('\n');
        sb.append("blockSizeKb: ").append(blockSizeKb).append('\n');
        return sb.toString();
    }

    /**
     *  populate run table with saved runs from db
     */
    public static void loadSavedRuns() {
        Gui.runPanel.clearTable();

        System.out.println("loading stored run data");
        DiskRun.findAll().stream().forEach((DiskRun run) -> {
            Gui.runPanel.addRun(run);
        });
    }

    /**
     * Deletes all previously saved benchmark run data from the database.
     * <p>
     * This method removes all existing disk performance records by calling
     * {@code DiskRun.deleteAll()} and then reloads the GUI with an empty dataset.
     * </p>
     */

    public static void clearSavedRuns() {
        DiskRun.deleteAll();

        loadSavedRuns();
    }

    public static void msg(String message) {
        Gui.mainFrame.msg(message);
    }

    /**
     * Cancels the currently running disk benchmark test, if one is active.
     * <p>
     * If no benchmark is in progress (i.e., {@code worker} is {@code null}),
     * a message is displayed and no further action is taken. Otherwise,
     * the active worker thread is instructed to cancel execution.
     * </p>
     */

    public static void cancelBenchmark() {
        if (worker == null) {
            msg("worker is null abort...");
            return;
        }
        worker.cancel(true);
    }

    /**
     * This method is used to start a new benchmark if there isn't one already in progress.
     * It starts a new DiskWorker thread, setting up its event handlers and then executes the SwingWorker.
     */
    public static void startBenchmark() throws Exception {

        //1. check that there isn't already a worker in progress
        if (state == State.DISK_TEST_STATE) {
            //if (!worker.isCancelled() && !worker.isDone()) {
            msg("Test in progress, aborting...");
            return;
            //}
        }

        //2. Set up area on disk for benchmark files, either as specified, or in temp area
        if (!setupDataArea()) {
            return;
        }

        //3. update state
        state = State.DISK_TEST_STATE;
        Gui.mainFrame.adjustSensitivity();

        //4. set up disk worker thread and its event handlers
        worker = new DiskWorker(new SwingWorkerUI());
        worker.addPropertyChangeListener((final PropertyChangeEvent event) -> {
            switch (event.getPropertyName()) {
                case "progress":
                    int value = (Integer) event.getNewValue();
                    Gui.progressBar.setValue(value);
                    long kbProcessed = (value) * App.targetTxSizeKb() / 100;
                    Gui.progressBar.setString(kbProcessed + " / " + App.targetTxSizeKb());
                    break;
                case "state":
                    switch ((StateValue) event.getNewValue()) {
                        case STARTED:
                            Gui.progressBar.setString("0 / " + App.targetTxSizeKb());
                            break;
                        case DONE:
                            break;
                    } // end inner switch
                    break;
            }
        });

        //5. start the Swing worker thread
        worker.execute();
    }

    /**
     * Set up data area for use by temp benchmark files. Will try to use configured area,
     * if not available, will use opsys temp area
     * @return true if successful
     */
    private static boolean setupDataArea() {
        // Check if can write to configured location
        if (!locationDir.canWrite()) {
            msg("Selected directory '" + locationDir +"' can not be written to. Trying Temp area");

            try {
                locationDir = Files.createTempDirectory("badBM").toFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            if (!locationDir.canWrite()) {
                msg("Can not write to Temp area, aborting this benchmark");
                return false;
            }
        }

        // Remove existing test data if exist
        if (App.autoRemoveData && dataDir.exists()) {
            if (dataDir.delete()) {
                msg("removed existing data dir");
            } else {
                msg("unable to remove existing data dir");
            }
        }

        // Create data dir if not already present
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        return true;
    }

    public static long targetMarkSizeKb() {
        return (long) blockSizeKb * numOfBlocks;
    }

    public static long targetTxSizeKb() {
        return (long) blockSizeKb * numOfBlocks * numOfMarks;
    }

    public static void updateMetrics(DiskMark mark) {
        if (mark.type == DiskMark.MarkType.WRITE) {
            if (wMax == -1 || wMax < mark.getBwMbSec()) {
                wMax = mark.getBwMbSec();
            }
            if (wMin == -1 || wMin > mark.getBwMbSec()) {
                wMin = mark.getBwMbSec();
            }
            if (wAvg == -1) {
                wAvg = mark.getBwMbSec();
            } else {
                int mn = mark.getMarkNum();
                wAvg = (((double) (mn - 1) * wAvg) + mark.getBwMbSec()) / (double) mn;
            }
            mark.setCumAvg(wAvg);
            mark.setCumMax(wMax);
            mark.setCumMin(wMin);
        } else {
            if (rMax == -1 || rMax < mark.getBwMbSec()) {
                rMax = mark.getBwMbSec();
            }
            if (rMin == -1 || rMin > mark.getBwMbSec()) {
                rMin = mark.getBwMbSec();
            }
            if (rAvg == -1) {
                rAvg = mark.getBwMbSec();
            } else {
                int mn = mark.getMarkNum();
                rAvg = (((double) (mn - 1) * rAvg) + mark.getBwMbSec()) / (double) mn;
            }
            mark.setCumAvg(rAvg);
            mark.setCumMax(rMax);
            mark.setCumMin(rMin);
        }
    }

    static public void resetSequence() {
        nextMarkNumber = 1;
    }

    /**
     * Reverts BM data to default starting values
     *
     */
    static public void resetTestData() {
        nextMarkNumber = 1;
        wAvg = -1;
        wMax = -1;
        wMin = -1;
        rAvg = -1;
        rMax = -1;
        rMin = -1;
    }

    public enum State {IDLE_STATE, DISK_TEST_STATE}

    public static SimpleExecutor buildExecutorWithObservers() {
        SimpleExecutor executor = SimpleExecutor.getInstance();

        executor.registerObserver(new edu.touro.mco152.bm.persist.DatabaseObserver());
        executor.registerObserver(new edu.touro.mco152.bm.ui.GuiRunPanelObserver());
        executor.registerObserver(new edu.touro.mco152.bm.externalsys.SlackObserver());

        return executor;
    }

}
