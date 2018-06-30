package de.rss.fachstudie.MiSim.entities.patterns;

public class CircuitBreaker {

    public enum State {
        CLOSED, HALF_OPEN, OPEN;
    }

    private int rollingWindow;
    private int requestVolumeThreshold;
    private float errorThresholdPercentage;
    private int sleepWindow;
    private int timeout;

    private State state;

    public CircuitBreaker() {
        state = State.CLOSED;
    }

    public CircuitBreaker(int rollingWindow, int requestVolumeThreshold, float errorThresholdPercentage, int sleepWindow, int timeout) {
        this.rollingWindow = rollingWindow;
        this.requestVolumeThreshold = requestVolumeThreshold;
        this.errorThresholdPercentage = errorThresholdPercentage;
        this.sleepWindow = sleepWindow;
        this.timeout = timeout;
        state = State.CLOSED;
    }

    public int getRollingWindow() {
        return rollingWindow;
    }

    public void setRollingWindow(int rollingWindow) {
        this.rollingWindow = rollingWindow;
    }

    public int getRequestVolumeThreshold() {
        return requestVolumeThreshold;
    }

    public void setRequestVolumeThreshold(int requestVolumeThreshold) {
        this.requestVolumeThreshold = requestVolumeThreshold;
    }

    public float getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }

    public void setErrorThresholdPercentage(float errorThresholdPercentage) {
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public int getSleepWindow() {
        return sleepWindow;
    }

    public void setSleepWindow(int sleepWindow) {
        this.sleepWindow = sleepWindow;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
