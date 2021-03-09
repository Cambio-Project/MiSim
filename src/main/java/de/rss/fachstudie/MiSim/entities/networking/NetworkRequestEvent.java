package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

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
        updateListener = new IRequestUpdateListener() {
            @Override
            public void onRequestFailed(Request request) {
                updateListeners.forEach(listener -> listener.onRequestFailed(request));
            }

            @Override
            public void onRequestArrivalAtTarget(Request request) {
                updateListeners.forEach(listener -> listener.onRequestArrivalAtTarget(request));
            }

            @Override
            public void onRequestSend(Request request) {
                updateListeners.forEach(listener -> listener.onRequestSend(request));

            }

            @Override
            public void onRequestResultArrivedAtRequester(Request request) {
                updateListeners.forEach(listener -> listener.onRequestResultArrivedAtRequester(request));
            }
        };
    }

    /**
     * To be implemented by Subclasses
     */
    @Override
    public abstract void eventRoutine() throws SuspendExecution;


}
