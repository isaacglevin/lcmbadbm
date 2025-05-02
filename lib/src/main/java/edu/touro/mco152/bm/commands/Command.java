package edu.touro.mco152.bm.commands;

/**
 * The Command interface which represents an executable benchmark task.
 * Any class that implements this interface can encapsulate a specific operation
 * and follow the Command Pattern.
 */

public interface Command {

    void execute();
}
