package edu.touro.mco152.bm.observer;

import edu.touro.mco152.bm.persist.DiskRun;

/**
 * An observer that gets notified when a benchmark finishes.
 */
public interface BenchmarkObserver {
    /**
     * Called when a benchmark completes.
     * @param run the completed DiskRun
     */
    void update(DiskRun run);
}
