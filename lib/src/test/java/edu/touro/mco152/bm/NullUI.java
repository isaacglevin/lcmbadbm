package edu.touro.mco152.bm;

import edu.touro.mco152.bm.BenchmarkUI;
import edu.touro.mco152.bm.DiskMark;

import java.beans.PropertyChangeListener;
import java.util.List;

public class NullUI implements BenchmarkUI {
    @Override public void start() {}
    @Override public boolean isItCancelled() { return false; }
    @Override public void cancelIt(boolean value) {}

    @Override
    public void setTheProgress(int percentComplete) {

    }

    @Override
    public void publishIt(DiskMark... marks) {

    }

    @Override public void addThePropertyChangeListener(PropertyChangeListener listener) {}
    @Override public void showMessage() {}
    @Override public boolean getStatus() { return true; }
}
