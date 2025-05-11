package edu.touro.mco152.bm;

import edu.touro.mco152.bm.ui.Gui;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.List;

public class SwingWorkerUI extends SwingWorker<Boolean, DiskMark> implements BenchmarkUI {

    Boolean lastStatus = null;

    @Override
    protected Boolean doInBackground() throws Exception {
        return DiskWorker.beginDiskWorker();
    }

    @Override
    public boolean getStatus() {
        return lastStatus;
    }

    @Override
    public boolean isItCancelled() {
        return super.isCancelled();
    }

    @Override
    public void cancelIt(boolean mayInterruptIfRunning) {
        super.cancel(mayInterruptIfRunning);
    }

    @Override
    public void setTheProgress(int percentComplete) {
        super.setProgress(percentComplete);
    }

    @Override
    protected void done() {
        DiskWorker.done();
    }
    @Override
    public void publishIt(DiskMark... marks) {
        super.publish(marks);
    }

    @Override
    protected void process(List<DiskMark> markList) {
        DiskWorker.process(markList);
    }

    @Override
    public void addThePropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void start() {
        super.execute();
    }

    @Override
    public void showMessage() {
        JOptionPane.showMessageDialog(Gui.mainFrame,
                """
                        For valid READ measurements please clear the disk cache by
                        using the included RAMMap.exe or flushmem.exe utilities.
                        Removable drives can be disconnected and reconnected.
                        For system drives use the WRITE and READ operations\s
                        independently by doing a cold reboot after the WRITE""",
                "Clear Disk Cache Now", JOptionPane.PLAIN_MESSAGE);
    }
    //
}
