package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class ComputationCompletedEvent extends Event<Request> {

    public ComputationCompletedEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        sendTraceNote(String.format("Request %s was computed.", request.getQuotedName()));
        request.setComputation_completed();
    }

}
