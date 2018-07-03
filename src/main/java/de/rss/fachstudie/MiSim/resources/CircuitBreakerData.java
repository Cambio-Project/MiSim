package de.rss.fachstudie.MiSim.resources;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;

public class CircuitBreakerData {

    private Operation operation;
    private CircuitBreaker.State state;
    private double requestVolume;
    private double errorCount;
    private double rollingWindowStartTime;
    private double cbOpenTime;
    private boolean trialSent;
    private Thread trialThread;

    public CircuitBreakerData(Operation operation) {
        this.operation = operation;

        this.state = CircuitBreaker.State.CLOSED;
        this.requestVolume = 0;
        this.errorCount = 0;
        this.rollingWindowStartTime = -1;
        this.cbOpenTime = -1;
        this.trialSent = false;
        this.trialThread = null;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public CircuitBreaker.State getState() {
        return state;
    }

    public void setState(CircuitBreaker.State state) {
        this.state = state;
    }

    public double getRequestVolume() {
        return requestVolume;
    }

    public void setRequestVolume(double requestVolume) {
        this.requestVolume = requestVolume;
    }

    public void increaseRequestVolume() {
        this.requestVolume = requestVolume + 1;
    }

    public double getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(double errorCount) {
        this.errorCount = errorCount;
    }

    public void increaseErrorCount() {
        this.errorCount = errorCount + 1;
    }

    public double getRollingWindowStartTime() {
        return rollingWindowStartTime;
    }

    public void setRollingWindowStartTime(double rollingWindowStartTime) {
        this.rollingWindowStartTime = rollingWindowStartTime;
    }

    public double getCbOpenTime() {
        return cbOpenTime;
    }

    public void setCbOpenTime(double cbOpenTime) {
        this.cbOpenTime = cbOpenTime;
    }

    public boolean isTrialSent() {
        return trialSent;
    }

    public void setTrialSent(boolean trialSent) {
        this.trialSent = trialSent;
    }

    public Thread getTrialThread() {
        return trialThread;
    }

    public void setTrialThread(Thread trialThread) {
        this.trialThread = trialThread;
    }
}