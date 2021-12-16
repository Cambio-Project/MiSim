package cambio.simulator.entities.networking;

import java.util.Objects;
import java.util.TreeSet;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Class that provides the ability to send requests and register listeners to this requests.
 *
 * @author Lion Wagner
 */
@SuppressWarnings("UnusedReturnValue")
public class RequestSender extends NamedEntity {

    private final TreeSet<IRequestUpdateListener> updateListeners = new TreeSet<>();
    /**
     * Proxy that forwards events to all listeners while letting the event be consumable.
     */
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
            return updateListeners.stream()
                .anyMatch(listener -> listener.onRequestResultArrivedAtRequester(request, when));
        }
    };

    public RequestSender(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    /**
     * Adds multiple listeners.
     *
     * @param listeners listeners that are to be add.
     * @see RequestSender#addUpdateListener(IRequestUpdateListener)
     */
    public final void addUpdateListeners(Iterable<IRequestUpdateListener> listeners) {
        for (IRequestUpdateListener listener : listeners) {
            addUpdateListener(listener);
        }
    }

    /**
     * Adds a listener to this sender. This listener will be update about the status of all requests sent by this
     * entity. This includes requests, that are already under way!
     *
     * @param listener listener that is to be added
     */
    public final void addUpdateListener(IRequestUpdateListener listener) {
        Objects.requireNonNull(listener);
        updateListeners.add(listener);
    }

    /**
     * Starts a RequestSendingProcess.
     *
     * @param eventName trace/debug name of the send event
     * @param request   request that should travel
     * @param target    {@code MicroserviceInstance} that should receive the request.
     * @return the created send event
     */
    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, MicroserviceInstance target) {
        return sendRequestInternal(eventName, request, target, new TimeSpan(0));
    }

    /**
     * Starts a RequestSendingProcess.
     *
     * @param eventName trace/debug name of the send event
     * @param request   request that should travel
     * @param target    {@code Microservice }  that should receive the request.
     * @return the created send event
     */
    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, Microservice target) {
        return sendRequestInternal(eventName, request, target, new TimeSpan(0));
    }

    /**
     * Starts a RequestSendingProcess.
     *
     * @param eventName trace/debug name of the send event
     * @param request   request that should travel
     * @param target    {@code MicroserviceInstance} that should receive the request.
     * @param delay     delays the send event by this amount
     * @return the created send event
     */
    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, MicroserviceInstance target,
                                                     TimeSpan delay) {
        return sendRequestInternal(eventName, request, target, delay);
    }

    /**
     * Starts a RequestSendingProcess.
     *
     * @param eventName trace/debug name of the send event
     * @param request   request that should travel
     * @param target    {@code Microservice } that should receive the request.
     * @param delay     delays the send event by this amount
     * @return the created send event
     */
    public final NetworkRequestSendEvent sendRequest(String eventName, Request request, Microservice target,
                                                     TimeSpan delay) {
        return sendRequestInternal(eventName, request, target, delay);
    }

    /**
     * Starts a RequestSendingProcess.
     *
     * @param eventName trace/debug name of the send event
     * @param request   request that should travel
     * @param target    {@code Microservice } or {@code MicroserviceInstance} that should receive the request.
     * @param delay     delays the send event by this amount
     * @return the created send event
     */
    private NetworkRequestSendEvent sendRequestInternal(String eventName, Request request, Object target,
                                                        TimeSpan delay) {
        request.addUpdateListener(updateListenerProxy);

        NetworkRequestSendEvent sendEvent;
        if (target == null || target.getClass() == MicroserviceOrchestration.class || target.getClass() == Microservice.class) {
            sendEvent = new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (Microservice) target);
        } else {
            sendEvent =
                new NetworkRequestSendEvent(getModel(), eventName, traceIsOn(), request, (MicroserviceInstance) target);
        }
        sendEvent.schedule(delay);
        return sendEvent;
    }

}
