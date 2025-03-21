import com.sun.source.tree.AssertTree;
import edu.touro.mco152.bm.*;
import edu.touro.mco152.bm.ui.Gui;
import edu.touro.mco152.bm.ui.MainFrame;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BadBMTests implements BenchmarkUI {

    boolean calledProgress = false;

    DiskWorker worker = new DiskWorker(this);

    Boolean lastStatus = null;

    @Test
    void runTheProgram() throws Exception {
        worker.execute();
        assertTrue(calledProgress);
    }
    public BadBMTests() {
        setupDefaultAsPerProperties();
    }

    /**
     * Bruteforce setup of static classes/fields to allow DiskWorker to run.
     *
     * @author lcmcohen
     */


    private static void setupDefaultAsPerProperties() {


        /// Do the minimum of what  App.init() would do to allow to run.
        Gui.mainFrame = new MainFrame();
        App.p = new Properties();
        App.loadConfig();

        Gui.progressBar = Gui.mainFrame.getProgressBar(); //must be set or get Nullptr

        // configure the embedded DB in .jDiskMark
        System.setProperty("derby.system.home", App.APP_CACHE_DIR);

        // code from startBenchmark
        //4. create data dir reference

        // may be null when tests not run in original proj dir, so use a default area
        if (App.locationDir == null) {
            App.locationDir = new File(System.getProperty("user.home"));
        }

        App.dataDir = new File(App.locationDir.getAbsolutePath()+File.separator+App.DATADIRNAME);

        //5. remove existing test data if exist
        if (App.dataDir.exists()) {
            if (App.dataDir.delete()) {
                App.msg("removed existing data dir");
            } else {
                App.msg("unable to remove existing data dir");
            }
        }
        else
        {
            App.dataDir.mkdirs(); // create data dir if not already present
        }
    }

    @Override
    public boolean getStatus() {
        return lastStatus;
    }

    @Override
    public boolean isItCancelled() {
        return false;
    }

    @Override
    public void cancelIt(boolean mayInterruptIfRunning) {

    }

    @Override
    public void setTheProgress(int percentComplete) {
        calledProgress = true;
        assertTrue(percentComplete >= 0 && percentComplete <= 100);
    }

    @Override
    public void publishIt(DiskMark... chunks) {
        DiskWorker.process(List.of(chunks));

    }

    @Override
    public void addThePropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void start() throws Exception {
        DiskWorker.beginDiskWorker();

    }

    @Override
    public void showMessage() {
        System.out.println("""
                        For valid READ measurements please clear the disk cache by
                        using the included RAMMap.exe or flushmem.exe utilities.
                        Removable drives can be disconnected and reconnected.
                        For system drives use the WRITE and READ operations\s
                        independently by doing a cold reboot after the WRITE""");

    }



//    He wants you to put some test in the Test class that a) checks if
//    the values passed to publish are valid -- between 1 and 100, and b)
//    that it got called at least once (so you'd have some false variable
//    that you change to true in that method and then assertTrue in the test method (after you call execute)
}
