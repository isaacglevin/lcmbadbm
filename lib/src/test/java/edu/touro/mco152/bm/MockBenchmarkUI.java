package edu.touro.mco152.bm;

import java.beans.PropertyChangeListener;

public class MockBenchmarkUI implements BenchmarkUI {

    private boolean progressSet = false;
    private boolean publishCalled = false;

    public boolean wasProgressSet() {
        return progressSet;
    }

    public boolean wasPublishCalled() {
        return publishCalled;
    }

    @Override
    public void start() {}

    @Override
    public void publishIt(DiskMark... mark) {
        publishCalled = true;
        for (DiskMark m : mark) {
            System.out.println("Mock publish: " + m.getBwMbSec() + " MB/s");
        }
    }

    @Override
    public void showMessage() {}

    @Override
    public boolean getStatus() {
        return true;
    }

    @Override
    public boolean isItCancelled() {
        return false;
    }

    @Override
    public void cancelIt(boolean b) {}

    @Override
    public void addThePropertyChangeListener(PropertyChangeListener listener) {}

    @Override
    public void setTheProgress(int percentComplete) {
        progressSet = true;
        System.out.println("Progress set to: " + percentComplete + "%");
    }
}
