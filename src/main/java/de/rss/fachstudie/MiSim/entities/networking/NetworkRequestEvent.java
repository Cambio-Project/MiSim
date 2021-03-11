package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.List;

/**
 * Superclass for network events that take care of exactly one Request. Provides its subclasses with references to the
 * traveling request and the sending Listener.
 *
 * @author Lion Wagner
 * @see NetworkRequestSendEvent
 * @see NetworkRequestReceiveEvent
 * @see UserRequestArrivalEvent
 * @see NetworkRequestCanceledEvent
 */
public abstract class NetworkRequestEvent extends ExternalEvent {

    private final List<IRequestUpdateListener> updateListeners;
    protected final Request traveling_request;

    protected final IRequestUpdateListener updateListener;

    public NetworkRequestEvent(Model model, String name, boolean showInTrace, Request request) {
        super(model, name, showInTrace);
        this.traveling_request = request;
        updateListeners = request.getUpdateListeners();

        //adapter, so each inheriting object only sees one anonymous updateListener
        updateListener = new IRequestUpdateListener() {
            @Override
            public void onRequestFailed(final Request request, final TimeInstant when, final RequestFailedReason reason) {
                updateListeners.forEach(listener -> listener.onRequestFailed(request, when, reason));
            }

            @Override
            public void onRequestArrivalAtTarget(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestArrivalAtTarget(request, when));
            }

            @Override
            public void onRequestSend(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestSend(request, when));
            }

            @Override
            public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
                updateListeners.forEach(listener -> listener.onRequestResultArrivedAtRequester(request, when));
            }
        };
    }

    /**
     * To be implemented by Subclasses
     */
    @Override
    public abstract void eventRoutine() throws SuspendExecution;


}
