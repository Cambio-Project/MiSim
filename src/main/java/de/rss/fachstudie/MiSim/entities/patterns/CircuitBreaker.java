package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.networking.IRequestUpdateListener;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import de.rss.fachstudie.MiSim.entities.networking.RequestFailedReason;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class CircuitBreaker extends NetworkPattern implements IRequestUpdateListener {

    @FromJson
    private int rollingWindow;
    @FromJson
    private int requestVolumeThreshold;
    @FromJson
    private float errorThresholdPercentage;
    @FromJson
    private int sleepWindow;
    @FromJson
    private int timeout;

    private State state = State.CLOSED;

    public enum State {
        CLOSED, HALF_OPEN, OPEN;
    }

    public CircuitBreaker(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace, owner);
    }

    @Override
    public int getListeningPriority() {
        return Priority.VERY_HIGH;
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


    @Override
    public void close() {

    }


    @Override
    public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {

    }

    @Override
    public void onRequestArrivalAtTarget(Request request, TimeInstant when) {

    }

    @Override
    public void onRequestSend(Request request, TimeInstant when) {

    }

    @Override
    public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {

    }
}
