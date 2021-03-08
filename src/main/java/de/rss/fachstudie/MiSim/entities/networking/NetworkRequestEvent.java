package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

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

    protected final IRequestUpdateListener updateListener;
    protected final Request traveling_request;

    public NetworkRequestEvent(Model model, String name, boolean showInTrace, Request request) {
        super(model, name, showInTrace);
        this.traveling_request = request;
        updateListener = request.getUpdateListener();
    }

    /**
     * To be implemented by Subclasses
     */
    @Override
    public abstract void eventRoutine() throws SuspendExecution;

    public final IRequestUpdateListener getUpdateListener() {
        return updateListener;
    }
}
