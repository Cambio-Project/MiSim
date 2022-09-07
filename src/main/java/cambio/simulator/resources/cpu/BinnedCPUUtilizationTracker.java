package cambio.simulator.resources.cpu;

import java.util.ArrayDeque;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Represents a CPU utilization tracker that periodically reports the CPU utilization in the recent time based on a bin
 * size. Bins can overlap. The {@link BinnedCPUUtilizationTracker#probeInterval} parameter may be used to adjust the
 * update rate. and the {@link BinnedCPUUtilizationTracker#BIN_SIZE} parameter may be used to adjust the bin size/time
 * taken into account for each measurement.
 *
 * @author Lion Wagner
 */
public final class BinnedCPUUtilizationTracker extends NamedSimProcess implements ISelfScheduled {

    public static TimeSpan probeInterval = new TimeSpan(0.1);
    public static TimeSpan BIN_SIZE = new TimeSpan(0.5);

    private final ArrayDeque<HistoryEntry> utilizationHistory = new ArrayDeque<>(1000);

    private final MultiDataPointReporter reporter;

    /**
     * Creates a new CPU Utilization Tracker that reports the utilization of the owning CPU periodically. The probe
     * interval can be configured by changing the static field {@link BinnedCPUUtilizationTracker#probeInterval} and
     * defaults to 0.1 units. The backwards bin can be configured by changing the static field {@link
     * BinnedCPUUtilizationTracker#BIN_SIZE} and defaults to 0.5 units.
     *
     * @param owner CPU that supplies this tracker with utilization information
     * @see #probeInterval
     * @see #BIN_SIZE
     */
    BinnedCPUUtilizationTracker(CPU owner) {
        super(owner.getModel(), String.format("Utilization Tracker of %s", owner.getName()), true, true);
        setSchedulingPriority(Priority.Very_LOW);
        reporter = new MultiDataPointReporter(String.format("C[%s]_", owner.getPlainName()), owner.getModel());

        utilizationHistory.add(new HistoryEntry(0.0, 0L, Long.MAX_VALUE));


        doInitialSelfSchedule();

    }

    @Override
    public void doInitialSelfSchedule() {
        this.activate(presentTime());
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        reporter.addDatapoint("UtilizationBinned", presentTime(), getCurrentBinnedUtilization());
        this.hold(probeInterval);
    }

    void updateUtilization(double utilization, TimeInstant startTime) {

        //last entry is the current utilization state
        HistoryEntry lastEntry = utilizationHistory.peekLast();
        assert lastEntry != null;

        //if the utilization didn't change, we don't need to update
        if (lastEntry.utilization != utilization) {
            lastEntry.endTime = startTime.getTimeInEpsilon();
            utilizationHistory.add(new HistoryEntry(utilization, startTime.getTimeInEpsilon(), Long.MAX_VALUE));
        }

        clearHistoryExceptCurrentBin();
    }

    private void clearHistoryExceptCurrentBin() {
        long binEnd = presentTime().getTimeInEpsilon();
        long binStart = Math.max(0, binEnd - BIN_SIZE.getTimeInEpsilon());

        //remove all first entries, that do not end inside the current window.
        while (utilizationHistory.peek() != null && utilizationHistory.peek().endTime < binStart) {
            utilizationHistory.pop();
        }
    }

    private double getCurrentBinnedUtilization() {
        long binEnd = presentTime().getTimeInEpsilon();
        long binStart = Math.max(0, binEnd - BIN_SIZE.getTimeInEpsilon());

        clearHistoryExceptCurrentBin();

        double accWorkDone = utilizationHistory.stream()
            .mapToDouble(entry -> entry.getWorkInTimeframe(binStart, binEnd))
            .sum();
        return accWorkDone / BIN_SIZE.getTimeInEpsilon();
    }

    double getCurrentUtilization() {
        assert utilizationHistory.peek() != null;
        return utilizationHistory.peek().utilization;
    }

    private static class HistoryEntry {
        public final double utilization;
        public final long startTime;
        public long endTime;

        public HistoryEntry(double utilization, long startTime, long endTime) {
            this.utilization = utilization;
            this.startTime = startTime;
            this.endTime = endTime;
        }


        public double getWorkInTimeframe(long frameStart, long frameEnd) {
            if (utilization == 0.0) {
                return 0.0;
            }

            long actualStart = Math.max(startTime, frameStart);
            long actualEnd = Math.min(endTime, frameEnd);
            return (actualEnd - actualStart) * utilization;
        }
    }

}
