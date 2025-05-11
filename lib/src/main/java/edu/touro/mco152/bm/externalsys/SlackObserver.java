package edu.touro.mco152.bm.externalsys;

import edu.touro.mco152.bm.observer.BenchmarkObserver;
import edu.touro.mco152.bm.persist.DiskRun;

/**
 * Observer that sends a Slack alert if a Read benchmark's max time
 * exceeds 3% above its average time.
 */
public class SlackObserver implements BenchmarkObserver {

    private final SlackManager slack = new SlackManager("BadBM");

    @Override
    public void update(DiskRun run) {
        if (run.getIoMode() != DiskRun.IOMode.READ) return;

        double max = run.getRunMax();
        double avg = run.getRunAvg();

        if (max > 1.03 * avg) {
            String msg = String.format(":warning: Read benchmark triggered alert: max=%.2f, avg=%.2f", max, avg);
            slack.postMsg2OurChannel(msg);
        }
    }
}
