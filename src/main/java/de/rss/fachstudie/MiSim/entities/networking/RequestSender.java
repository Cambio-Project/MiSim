package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.Objects;
import java.util.PriorityQueue;

/**
 * @author Lion Wagner
 */
public class RequestSender extends Entity {

    private final PriorityQueue<IRequestUpdateListener> updateListeners = new PriorityQueue<>();

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
        updateListeners.forEach(request::addUpdateListener);

        NetworkRequestSendEvent sendEvent;
        if (target == null || target.getClass() == Microservice.class)
            sendEvent = new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (Microservice) target);
        else
            sendEvent = new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (MicroserviceInstance) target);
        sendEvent.schedule(delay);
        return sendEvent;
    }
}
