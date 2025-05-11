package edu.touro.mco152.bm.ui;

import edu.touro.mco152.bm.observer.BenchmarkObserver;
import edu.touro.mco152.bm.persist.DiskRun;

/**
 * Observer that adds the benchmark result to the GUI run panel.
 */
public class GuiRunPanelObserver implements BenchmarkObserver {

    /**
     * Adds the run to the GUI panel if available.
     *
     * @param run the completed DiskRun to display
     */
    @Override
    public void update(DiskRun run) {
        if (Gui.runPanel != null) {
            Gui.runPanel.addRun(run);
        }
    }
}
