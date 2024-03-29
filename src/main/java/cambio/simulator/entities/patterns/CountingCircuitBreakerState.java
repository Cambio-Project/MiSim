package cambio.simulator.entities.patterns;

import java.util.LinkedList;

import cambio.simulator.entities.microservice.Microservice;
import desmoj.core.simulator.*;
import org.javatuples.Quartet;
import org.javatuples.Tuple;

/**
 * This class represents an actual CircuitBreaker with the behavior defined by Hystrix.
 *
 * @author Lion Wagner
 * @see CircuitBreaker
 */
public class CountingCircuitBreakerState implements ICircuitBreakerState {

    private final double errorThresholdPercentage;
    private final int rollingWindow; //window over which error rates are collected
    private final double sleepWindow;
    private final Microservice monitoredService;

    //contains the results of the last {rollingWindow size} requests (values, 1 for success and 0 for failure)
    private final LinkedList<Integer> currentWindow = new LinkedList<>();
    private int totalSuccessCounter = 0;
    private int totalFailureCounter = 0;
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    CountingCircuitBreakerState(Microservice monitoredService, double errorThresholdPercentage, int rollingWindow,
                                double sleepWindow) {
        this.errorThresholdPercentage = errorThresholdPercentage;
        this.rollingWindow = rollingWindow;
        this.monitoredService = monitoredService;
        this.sleepWindow = sleepWindow;
    }

    public Tuple getCurrentStatistics() {
        double errorRate = getErrorRate();
        return new Quartet<>(state, totalSuccessCounter, totalFailureCounter, errorRate);
    }

    public CircuitBreakerState getState() {
        return state;
    }

    public boolean isOpen() {
        return state == CircuitBreakerState.OPEN;
    }

    /**
     * {@inheritDoc}
     */
    public void notifySuccessfulCompletion(TimeInstant when) {
        totalSuccessCounter++;

        if (state == CircuitBreakerState.HALF_OPEN) {
            currentWindow.clear();
            state = CircuitBreakerState.CLOSED;
        }

        currentWindow.addLast(1);
        checkErrorRate();
    }

    /**
     * {@inheritDoc}
     */
    public void notifyArrivalFailure(TimeInstant when) {
        totalFailureCounter++;

        if (state == CircuitBreakerState.HALF_OPEN) {
            openBreaker();
            return;
        }

        currentWindow.addLast(0);
        checkErrorRate();
    }

    /**
     * Method called by the {@link HalfOpenBreakerEvent} to half open this circuit after a certain amount of time.
     */
    public void toHalfOpen() {
        state = CircuitBreakerState.HALF_OPEN;
    }

    private synchronized void checkErrorRate() {
        //cut down the current window to rolling window size
        while (currentWindow.size() > rollingWindow) {
            currentWindow.removeFirst();
        }

        //check error rate if enough entries are present
        if (currentWindow.size() >= rollingWindow) {
            double errorRate = getErrorRate();
            if (errorRate >= errorThresholdPercentage) {
                openBreaker();
            }
        }
    }

    private void openBreaker() {
        state = CircuitBreakerState.OPEN;
        currentWindow.clear();
        ExternalEvent openEvent = new HalfOpenBreakerEvent(monitoredService.getModel(), null, false, this);
        openEvent.schedule(new TimeSpan(sleepWindow, monitoredService.getModel().getExperiment().getReferenceUnit()));

    }

    private double getErrorRate() {
        //if there are not enough data points we can't determine the error rate, so it's assumed to be 0
        return currentWindow.size() < rollingWindow ? 0 :
            1.0 - ((double) currentWindow.stream().mapToInt(value -> value).sum() / currentWindow.size());
    }
}
