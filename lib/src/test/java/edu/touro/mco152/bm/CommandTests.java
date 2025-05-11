package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commands.*;
import edu.touro.mco152.bm.persist.DiskRun;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests for the Command Pattern implementation of BadBM,
 * using mock UI and hardcoded parameters.
 */
public class CommandTests {

    static final int NUM_MARKS = 25;
    static final int NUM_BLOCKS = 128;
    static final int BLOCK_SIZE_KB = 2048;
    static final DiskRun.BlockSequence SEQ = DiskRun.BlockSequence.SEQUENTIAL;
    static final File TEST_DIR = new File(System.getProperty("java.io.tmpdir") + File.separator + "badbmtest");


    /**
     * This tests the execution of the WriteCommand.
     * it verifies that the command can complete without throwing any exceptions and that it interacts properly with mocks,
     * particularly focusing on writing progress and publishing results.
     */
    @Test
    public void testWriteCommand() {
        if (!TEST_DIR.exists()) TEST_DIR.mkdirs();
        MockBenchmarkUI ui = new MockBenchmarkUI();

        Command write = new WriteCommand(
                NUM_MARKS, NUM_BLOCKS, BLOCK_SIZE_KB,
                SEQ, ui, true, true, TEST_DIR, 1
        );

        SimpleExecutor.getInstance();

        assertTrue(ui.wasProgressSet());
        assertTrue(ui.wasPublishCalled());
    }

    /**
     * This tests the execution of the ReadCommand.
     * It ensures that the read benchmarking logic can complete successfully and it publishes performance metrics,
     * while avoiding any dependencies on the GUI components.
     */
    @Test
    public void testReadCommand() {
        MockBenchmarkUI ui = new MockBenchmarkUI();

        Command read = new ReadCommand(
                NUM_MARKS, NUM_BLOCKS, BLOCK_SIZE_KB,
                SEQ, ui, true, TEST_DIR, 1
        );

        SimpleExecutor.getInstance();

        assertTrue(ui.wasProgressSet());
        assertTrue(ui.wasPublishCalled());
    }
}
