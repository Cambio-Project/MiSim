package cambio.simulator.resources.cpu;

import cambio.simulator.entities.networking.Request;
import desmoj.core.simulator.TimeInstant;

/**
 * Data-class that contains information about a currently running (or potentially finished) process.
 *
 * <p>
 * More specifically, it concentrates on holding information about the total work units totally needed and left to
 * complete the process. Further, there are methods to manipulate the latter.
 *
 * <p>
 * For the purpose of compatibility (e.g. non impacting operations) a demand of {@code 0} is allowed.
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
     * Constructor that can be used to create artificial load onto the system.
     *
     * @param demandTotal load in simulation units
     */
    public CPUProcess(int demandTotal) {
        this(demandTotal, null);
    }

    /**
     * Creates a {@link CPUProcess} for the given {@link Request}.
     *
     * @param request {@link Request} that should be represented by this process.
     */
    public CPUProcess(Request request) {
        this(request.operation.getDemand(), request);
    }


    private CPUProcess(int demand, Request request) {
        if (demand < 0) {
            throw new IllegalArgumentException("Demand has to be 0 or greater");
        }
        this.demandTotal = demand;
        this.demandRemainder = demandTotal;
        this.request = request;
    }

    public int getDemandTotal() {
        return demandTotal;
    }

    /**
     * Gets the remaining demand from before the start of the latest burst.
     *
     * @return the remaining demand from before the start of the latest burst.
     */
    public int getDemandRemainder() {
        return demandRemainder;
    }

    /**
     * Calculates the time/work left for the current burst of this processes at the specific point int time.
     *
     * <p>
     * Specifically returns: Remainder at start of Burst - (peekTime - startTime) * computingCapacityPerTimeUnit
     *
     * @param peekTime                     time for which the current remainder should be calculated
     * @param computingCapacityPerTimeUnit computing capacity of a thread per time unit
     * @return the remaining demand of the currently handled process
     */
    public double getDemandRemainder(TimeInstant peekTime, double computingCapacityPerTimeUnit) {
        int remainder = getDemandRemainder();
        double runningTime = peekTime.getTimeAsDouble() - startOfCurrentBurst.getTimeAsDouble();

        return remainder - (runningTime * computingCapacityPerTimeUnit);
    }

    public Request getRequest() {
        return request;
    }

    /**
     * Natural sorting is based on left over demand.
     */
    @Override
    public int compareTo(CPUProcess other) {
        if (other == null) {
            return -1;
        }
        return this.demandRemainder - other.demandRemainder;
    }

    /**
     * Subtracts the given demand amount from he this process remaining demand.
     *
     * @param amount value by which the demand remainder should be reduced
     * @throws IllegalArgumentException if the given amount is larger than the demand remainder.
     */
    public void reduceDemandRemainder(double amount) {
        if (this.demandRemainder < amount) {
            throw new IllegalArgumentException(
                String.format("Cannot reduce left over demand (which is %d) by %f", demandRemainder, amount));
        }
        this.demandRemainder -= amount;
    }

    /**
     * Stamps the start of the next cpu burst.
     *
     * @param start time when the burst started.
     */
    public void stampCurrentBurstStarted(TimeInstant start) {
        startOfCurrentBurst = start;
    }

    /**
     * Cancels the current Burst of this process and reset its progress.
     */
    public void cancel() {
        if (currentBurstCompletionEvent != null) {
            currentBurstCompletionEvent.cancel();
        }
    }

    /**
     * Sets the event, that should be executed upon the next completion of a burst of this process.
     *
     * @param currentBurstCompletionEvent event, that should be executed upon the next completion of a burst of this
     *                                    process.
     */
    public void setCurrentBurstCompletionEvent(ComputationBurstCompletedEvent currentBurstCompletionEvent) {
        this.currentBurstCompletionEvent = currentBurstCompletionEvent;
    }
}
