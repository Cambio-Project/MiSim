package de.rss.fachstudie.MiSim.resources;

import de.rss.fachstudie.MiSim.entities.networking.Request;
import desmoj.core.simulator.TimeInstant;

/**
 * Data-class that contains information about a currently running (or potentially finished) process.
 * <p>
 * More specifically, it concentrates on holding information about the total work units needed and left to complete the
 * processes. Further, there are methods to manipulate the later.
 * <p>
 * For the purpose of compatibility (e.g. non impacting operations) a demand of 0 is allowed.
 *
 * @author Lion Wagner
 */
public final class CPUProcess implements Comparable<CPUProcess> {
    private final int demandTotal;
    private final Request request;
    private TimeInstant startOfCurrentBurst;
    private int demandRemainder;
    private ComputationBurstCompletedEvent currentBurstCompletionEvent;

    /**
     * Constructor that can be used to create artificial load onto the system
     *
     * @param demandTotal load in simulation units
     */
    public CPUProcess(int demandTotal) {
        this(demandTotal, null);
    }

    public CPUProcess(Request request) {
        this(request.operation.getDemand(), request);
    }

    private CPUProcess(int demand, Request request) {
        if (demand < 0) throw new IllegalArgumentException("Demand has to be 0 or greater");
        this.demandTotal = demand;
        this.demandRemainder = demandTotal;
        this.request = request;
    }

    public int getDemandTotal() {
        return demandTotal;
    }

    public int getDemandRemainder() {
        return demandRemainder;
    }

    /**
     * Calculates the time/work left for the current burst of this processes at the specific point int time.
     * <p>
     * Specifically returns: Remainder at start of Burst - (peekTime - startTime) * computingCapacityPerTimeUnit
     *
     * @param peekTime
     * @param computingCapacityPerTimeUnit
     * @return
     */
    public double getDemandRemainder(TimeInstant peekTime, int computingCapacityPerTimeUnit) {
        int remainder = getDemandRemainder();
        double runningTime = peekTime.getTimeAsDouble() - startOfCurrentBurst.getTimeAsDouble();

        return remainder - (runningTime * computingCapacityPerTimeUnit);
    }

    public Request getRequest() {
        return request;
    }

    /**
     * Natural sorting should by base don left over demand.
     */
    @Override
    public int compareTo(CPUProcess o) {
        if (o == null) return -1;
        int difference = this.demandRemainder - o.demandRemainder;
        return difference;
    }

    public void reduceDemandRemainder(int amount) {
        if (this.demandRemainder < amount)
            throw new IllegalArgumentException(String.format("Cannot reduce left over demand (which is %d) by %d", demandRemainder, amount));
        this.demandRemainder -= amount;
    }


    void stampCurrentBurstStarted(TimeInstant start) {
        startOfCurrentBurst = start;
    }


    /**
     * Cancels the current Burst of the this process and reset its progress.
     */
    public void cancel() {
        if (currentBurstCompletionEvent != null)
            currentBurstCompletionEvent.cancel();
    }

    public void setCurrentBurstCompletionEvent(ComputationBurstCompletedEvent currentBurstCompletionEvent) {
        this.currentBurstCompletionEvent = currentBurstCompletionEvent;
    }
}
