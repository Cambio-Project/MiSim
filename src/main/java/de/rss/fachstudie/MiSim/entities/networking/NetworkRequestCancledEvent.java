package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;

/**
 * @author Lion Wagner
 */
public class NetworkRequestCancledEvent extends MainModelAwareRequestEvent {

    private final Throwable reason;

    public NetworkRequestCancledEvent(MainModel model, String name, boolean showInTrace, Throwable reason) {
        super(model, name, showInTrace);
        this.reason = reason;
    }

    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        if (traceIsOn())
            sendTraceNote(String.format("Request %s was not handled. Cause: %s", request.getQuotedName(), reason.getMessage()));
    }
}
