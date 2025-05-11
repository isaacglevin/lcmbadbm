package edu.touro.mco152.bm;

import edu.touro.mco152.bm.commands.Command;
import edu.touro.mco152.bm.commands.SimpleExecutor;
import edu.touro.mco152.bm.commands.WriteCommand;
import edu.touro.mco152.bm.observer.BenchmarkObserver;
import edu.touro.mco152.bm.persist.DiskRun;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ObserverPatternTest {

    static boolean wasNotified = false;

    static class TestObserver implements BenchmarkObserver {
        @Override
        public void update(DiskRun run) {
            wasNotified = true;
        }
    }

    @BeforeAll
    void registerTestObserver() {
        SimpleExecutor.getInstance().registerObserver(new TestObserver());
    }

    @Test
    void testObserverGetsNotified() throws Exception {
        Command cmd = new WriteCommand(
                1, 1, 1,
                DiskRun.BlockSequence.SEQUENTIAL,
                new NullUI(),
                true,
                false,
                new File(System.getProperty("java.io.tmpdir")),
                1
        );
        SimpleExecutor.getInstance().executeCommand(cmd);
    }

    @AfterAll
    void checkObserverTriggered() {
        assertTrue(wasNotified, "Observer should have been notified after command execution.");
    }
}
