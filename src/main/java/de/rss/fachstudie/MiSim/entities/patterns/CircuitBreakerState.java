package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Quartet;
import org.javatuples.Tuple;

import java.util.LinkedList;

/**
 * @author Lion Wagner
 */
public class CircuitBreakerState {

    private final double errorThresholdPercentage;
    private final int rollingWindow; //window over which error rates are collected
    private final double sleepWindow;
    private final Microservice monitoredService;


    private int totalSuccessCounter = 0;
    private int totalFailureCounter = 0;

    //contains the results of the last 20 requests (1 for success, 0 for failure)
    private final LinkedList<Integer> currentWindow = new LinkedList<>();

    private BreakerState state = BreakerState.CLOSED;

    public Tuple getCurrentStatistics() {
        double errorRate = getErrorRate();
        return new Quartet<>(state, totalSuccessCounter, totalFailureCounter, errorRate);
    }

    public enum BreakerState {
        CLOSED, HALF_OPEN, OPEN;
    }

    CircuitBreakerState(Microservice monitoredService, double errorThresholdPercentage, int rollingWindow, double sleepWindow) {
        this.errorThresholdPercentage = errorThresholdPercentage;
        this.rollingWindow = rollingWindow;
        this.monitoredService = monitoredService;
        this.sleepWindow = sleepWindow;
    }

    public BreakerState getState() {
        return state;
    }

    public boolean isOpen() {
        return state == BreakerState.OPEN;
    }

    synchronized void notifySuccessfulCompletion() {
        totalSuccessCounter++;

        if (state == BreakerState.HALF_OPEN) {
            currentWindow.clear();
            state = BreakerState.CLOSED;
        }

        currentWindow.addLast(1);
        checkErrorRate();
    }

    synchronized void notifyArrivalFailure() {
        totalFailureCounter++;

        if (state == BreakerState.HALF_OPEN) {
            openBreaker();
            return;
        }

        currentWindow.addLast(0);
        checkErrorRate();
    }


    /**
     * Method called by the {@link HalfOpenBreakerEvent} to half open this circuit after a certain amount of time
     */
    synchronized void toHalfOpen() {
        state = BreakerState.HALF_OPEN;
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

    private synchronized void openBreaker() {
        state = BreakerState.OPEN;
        currentWindow.clear();
        ExternalEvent openEvent = new HalfOpenBreakerEvent(monitoredService.getModel(), null, false, this);
        openEvent.schedule(new TimeSpan(sleepWindow, monitoredService.getModel().getExperiment().getReferenceUnit()));

    }


    private synchronized double getErrorRate() {
        //if there are not enough datapoints we cant determine errorrate -> 0
        return currentWindow.size() < rollingWindow ? 0 : 1.0 - ((double) currentWindow.stream().mapToInt(value -> value).sum() / currentWindow.size());
    }

}
