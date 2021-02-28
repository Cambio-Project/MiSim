package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class NetworkRequestCanceledEvent extends Event<Request> {

    private final Throwable reason;

    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Throwable reason) {
        super(model, name, showInTrace);
        this.reason = reason;
    }

    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        if (traceIsOn())
            sendTraceNote(String.format("Request %s was not handled. Cause: %s", request.getQuotedName(), reason.getMessage()));
    }
}
