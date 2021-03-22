package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.Objects;
import java.util.TreeSet;

/**
 * @author Lion Wagner
 */
public class RequestSender extends Entity {

    private final TreeSet<IRequestUpdateListener> updateListeners = new TreeSet<>();

    public RequestSender(Model model, String s, boolean b) {
        super(model, s, b);
    }

    public final void addUpdateListeners(Iterable<IRequestUpdateListener> listeners) {
        for (IRequestUpdateListener listener : listeners) addUpdateListener(listener);
    }

    public final void addUpdateListener(IRequestUpdateListener listener) {
        Objects.requireNonNull(listener);
        updateListeners.add(listener);
    }

    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, MicroserviceInstance target) {
        return sendRequestInternal(eventName, request, target, new TimeSpan(0));
    }

    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, Microservice target) {
        return sendRequestInternal(eventName, request, target, new TimeSpan(0));
    }

    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, MicroserviceInstance target, TimeSpan delay) {
        return sendRequestInternal(eventName, request, target, delay);
    }

    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, Microservice target, TimeSpan delay) {
        return sendRequestInternal(eventName, request, target, delay);
    }

    private NetworkRequestSendEvent sendRequestInternal(String eventName, Request request, Object target, TimeSpan delay) {
        request.addUpdateListener(updateListenerProxy);

        NetworkRequestSendEvent sendEvent;
        if (target == null || target.getClass() == Microservice.class)
            sendEvent = new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (Microservice) target);
        else
            sendEvent = new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (MicroserviceInstance) target);
        sendEvent.schedule(delay);
        return sendEvent;
    }

    public final IRequestUpdateListener updateListenerProxy = new IRequestUpdateListener() {
        @Override
        public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
            return updateListeners.stream().anyMatch(listener -> listener.onRequestFailed(request, when, reason));
        }

        @Override
        public boolean onRequestArrivalAtTarget(Request request, TimeInstant when) {
            return updateListeners.stream().anyMatch(listener -> listener.onRequestArrivalAtTarget(request, when));
        }

        @Override
        public boolean onRequestSend(Request request, TimeInstant when) {
            return updateListeners.stream().anyMatch(listener -> listener.onRequestSend(request, when));
        }

        @Override
        public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
            return updateListeners.stream().anyMatch(listener -> listener.onRequestResultArrivedAtRequester(request, when));
        }
    };

}
