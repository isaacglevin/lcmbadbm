package edu.touro.mco152.bm;

import java.beans.PropertyChangeListener;

public interface BenchmarkUI {

    boolean getStatus();

    boolean isItCancelled();

    void cancelIt(boolean mayInterruptIfRunning);

    void setTheProgress(int percentComplete);

    void publishIt(DiskMark... marks);

    void addThePropertyChangeListener(PropertyChangeListener listener);

    void start() throws Exception;

    void showMessage();
}