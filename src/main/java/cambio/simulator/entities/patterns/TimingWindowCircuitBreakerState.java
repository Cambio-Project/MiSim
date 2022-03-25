package cambio.simulator.entities.patterns;

import java.util.ArrayDeque;
import java.util.Deque;

import cambio.simulator.entities.microservice.Microservice;
import desmoj.core.simulator.*;
import org.javatuples.Quartet;
import org.javatuples.Tuple;

/**
 * This class represents an actual CircuitBreaker with the behavior defined by Hystrix.
 *
 * @author Lion Wagner
 * @see CircuitBreaker
 * @see <a href="https://github.com/Netflix/Hystrix/blob/master/hystrix-core/src/main/java/com/netflix/hystrix/HystrixCircuitBreaker.java">HystrixCircuitBreaker
 *     implementation</a>
 */
public class TimingWindowCircuitBreakerState implements ICircuitBreakerState {

    private transient boolean hasHalfOpenBreakerEventScheduled = false;

    private final double errorThresholdPercentage;
    private final long windowTimeLength; //window over which error rates are collected
    private final TimeSpan sleepWindow;
    private final Microservice monitoredService;

    private final Deque<Long> currentWindow = new ArrayDeque<>(1000);
    private int currentSuccessfulCount = 0;
    private int currentFailedCount = 0;
    private int totalSuccessCounter = 0;
    private int totalFailureCounter = 0;

    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    TimingWindowCircuitBreakerState(Microservice monitoredService,
                                    double errorThresholdPercentage,
                                    double rollingWindow,
                                    double sleepWindow) {
        this.errorThresholdPercentage = errorThresholdPercentage;
        this.windowTimeLength = new TimeSpan(rollingWindow).getTimeInEpsilon();
        this.monitoredService = monitoredService;
        this.sleepWindow = new TimeSpan(sleepWindow);
    }

    @Override
    public Tuple getCurrentStatistics() {
        double errorRate = getErrorRate();
        return new Quartet<>(state, totalSuccessCounter, totalFailureCounter, errorRate);
    }

    @Override
    public CircuitBreakerState getState() {
        return state;
    }

    @Override
    public boolean isOpen() {
        return state == CircuitBreakerState.OPEN;
    }

    /**
     * Removes all entries from the currentWindow, that are older than the windowTime Length.
     *
     * @param currentTime the current time of the simulation
     */
    private void purgeStateList(TimeInstant currentTime) {
        long windowEnd = (currentTime.getTimeInEpsilon() - windowTimeLength) << 1; //left shift because of decoding
        while (currentWindow.size() > 0 && currentWindow.peekFirst() < windowEnd) {
            long successfulBit = currentWindow.removeFirst() & 1;
            currentSuccessfulCount -= successfulBit;
            currentFailedCount -= 1 - successfulBit;
        }
    }

    private void addToStateList(TimeInstant currentTime, boolean failed) {
        int successfulBit = failed ? 0 : 1;
        //shifts the current time one bit to the left and then adds  failure/success as the LSB
        currentWindow.addLast(currentTime.getTimeInEpsilon() << 1 | successfulBit);
        currentSuccessfulCount += successfulBit;
        currentFailedCount += 1 - successfulBit;
    }

    @Override
    public void notifySuccessfulCompletion(TimeInstant when) {
        purgeStateList(when);

        totalSuccessCounter++;

        if (state == CircuitBreakerState.HALF_OPEN) {
            state = CircuitBreakerState.CLOSED;
            resetState();
        }

        addToStateList(when, false);
        //checkErrorRate(); Error rate does not need to be updated here, since it can only go up
    }


    @Override
    public void notifyArrivalFailure(TimeInstant when) {
        purgeStateList(when);
        totalFailureCounter++;

        if (state == CircuitBreakerState.HALF_OPEN) {
            openBreaker();
            return;
        }

        addToStateList(when, true);
        checkErrorRate();
    }

    @Override
    public void toHalfOpen() {
        state = CircuitBreakerState.HALF_OPEN;
        hasHalfOpenBreakerEventScheduled = false;
    }

    private void checkErrorRate() {
        double errorRate = getErrorRate();
        if (errorRate >= errorThresholdPercentage) {
            openBreaker();
        }
    }

    private void openBreaker() {
        state = CircuitBreakerState.OPEN;
        if (!hasHalfOpenBreakerEventScheduled) {
            ExternalEvent openEvent = new HalfOpenBreakerEvent(monitoredService.getModel(), null, false, this);
            openEvent.schedule((sleepWindow));
            hasHalfOpenBreakerEventScheduled = true;
        }
    }

    private double getErrorRate() {
        if (currentSuccessfulCount + currentFailedCount < 5) {
            return 0;
        } else {
            return (double) currentFailedCount / (currentSuccessfulCount + currentFailedCount);
        }
    }

    private void resetState() {
        currentSuccessfulCount = 0;
        currentFailedCount = 0;
        currentWindow.clear();
    }
}
