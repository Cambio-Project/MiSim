package de.unistuttgart.sqa.orcas.misim.entities.networking;

import java.util.Collection;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Superclass for network events that take care of exactly one traveling {@code Request}. It provides its subclasses
 * with references to the traveling request and the {@code IRequestUpdateListener}.
 *
 * @author Lion Wagner
 * @see NetworkRequestSendEvent
 * @see NetworkRequestReceiveEvent
 * @see NetworkRequestCanceledEvent
 */
public abstract class NetworkRequestEvent extends ExternalEvent {

    private final Collection<IRequestUpdateListener> updateListeners;
    protected final Request travelingRequest;

    protected final IRequestUpdateListener updateListener;

    /**
     * Common constructor that enforces the association of a request with an External Event.
     *
     * @param travelingRequest the request that is associated with this request.
     */
    public NetworkRequestEvent(Model model, String name, boolean showInTrace, Request travelingRequest) {
        super(model, name, showInTrace);
        this.travelingRequest = travelingRequest;
        updateListeners = travelingRequest.getUpdateListeners();

        //adapter/proxy, so each inheriting object only sees one single anonymous updateListener
        updateListener = new IRequestUpdateListener() {
            @Override
            public boolean onRequestFailed(final Request request, final TimeInstant when,
                                           final RequestFailedReason reason) {
                updateListeners.forEach(listener -> listener.onRequestFailed(request, when, reason));
                return true;
            }

            @Override
            public boolean onRequestArrivalAtTarget(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestArrivalAtTarget(request, when));
                return true;
            }

            @Override
            public boolean onRequestSend(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestSend(request, when));
                return true;
            }

            @Override
            public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestResultArrivedAtRequester(request, when));
                return true;
            }
        };
    }

    /**
     * To be implemented by Subclasses.
     */
    @Override
    public abstract void eventRoutine() throws SuspendExecution;

    public Request getTravelingRequest() {
        return travelingRequest;
    }
}
