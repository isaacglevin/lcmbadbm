package edu.touro.mco152.bm.observer;

import edu.touro.mco152.bm.persist.DiskRun;

import java.util.List;

/**
 * A subject that manages benchmark observers.
 */
public interface BenchmarkSubject {
    /**
     * Register an observer to receive notifications.
     */
    void registerObserver(BenchmarkObserver o);

    /**
     * Remove an observer.
     */
    void removeObserver(BenchmarkObserver o);

    /**
     * Notify all observers with the benchmark result.
     */
    void notifyObservers(DiskRun run);
}
