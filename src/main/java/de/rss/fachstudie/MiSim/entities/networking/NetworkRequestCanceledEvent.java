package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.Model;

/**
 * Event that should be scheduled when a request gets canceled.
 *
 * @author Lion Wagner
 */
public class NetworkRequestCanceledEvent extends NetworkRequestEvent {

    private final String reason;

    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Request request, String reason) {
        super(model, name, showInTrace, request);
        this.reason = reason;
        setSchedulingPriority(Priority.VERY_HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        sendTraceNote(String.format("Request %s was not handled. Cause: %s", traveling_request.getQuotedName(), reason));
        updateListener.onRequestFailed(traveling_request);
    }
}
