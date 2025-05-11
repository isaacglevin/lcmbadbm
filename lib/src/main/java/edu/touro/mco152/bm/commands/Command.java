package edu.touro.mco152.bm.commands;

import edu.touro.mco152.bm.persist.DiskRun;

/**
 * The Command interface represents an executable benchmark task that returns a result.
 */
public interface Command {

    /**
     * Executes the benchmark and returns a DiskRun result.
     *
     * @return the completed DiskRun
     */
    DiskRun execute();
}
