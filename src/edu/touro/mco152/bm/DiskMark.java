package edu.touro.mco152.bm;

import java.text.DecimalFormat;

/**
 * The {@code DiskMark} class represents a single measurement of disk performance
 * during a benchmarking process.
 * <p>
 * Each {@code DiskMark} instance tracks performance data for a specific disk operation
 * (either a read or write). It records:
 * </p>
 * <ul>
 *     <li>The mark number (indicating the sequence of the test).</li>
 *     <li>The measured bandwidth in megabytes per second.</li>
 *     <li>The cumulative minimum, maximum, and average speeds.</li>
 * </ul>
 *
 * <p>This class provides methods for formatting and retrieving these values,
 * ensuring consistency in performance reporting. It is primarily used in conjunction
 * with disk benchmarking processes.</p>
 *
 * @author Isaac Levin
 * @version 1.0
 */

public class DiskMark {

    static DecimalFormat df = new DecimalFormat("###.###");
    MarkType type;
    private int markNum = 0;       // x-axis
    private double bwMbSec = 0;    // y-axis
    private double cumMin = 0;
    private double cumMax = 0;
    private double cumAvg = 0;
    DiskMark(MarkType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Mark(" + type + "): " + getMarkNum() + " bwMbSec: " + getBwMbSecAsString() + " avg: " + getAvgAsString();
    }

    String getBwMbSecAsString() {
        return df.format(getBwMbSec());
    }

    String getMinAsString() {
        return df.format(getCumMin());
    }

    String getMaxAsString() {
        return df.format(getCumMax());
    }

    String getAvgAsString() {
        return df.format(getCumAvg());
    }

    public int getMarkNum() {
        return markNum;
    }

    public void setMarkNum(int markNum) {
        this.markNum = markNum;
    }

    public double getBwMbSec() {
        return bwMbSec;
    }

    public void setBwMbSec(double bwMbSec) {
        this.bwMbSec = bwMbSec;
    }

    public double getCumAvg() {
        return cumAvg;
    }

    public void setCumAvg(double cumAvg) {
        this.cumAvg = cumAvg;
    }

    public double getCumMin() {
        return cumMin;
    }

    public void setCumMin(double cumMin) {
        this.cumMin = cumMin;
    }

    public double getCumMax() {
        return cumMax;
    }

    public void setCumMax(double cumMax) {
        this.cumMax = cumMax;
    }

    public enum MarkType {READ, WRITE}
}
