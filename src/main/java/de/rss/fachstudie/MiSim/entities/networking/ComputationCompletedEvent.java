package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.resources.Thread;
import desmoj.core.simulator.EventOf3Entities;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class ComputationCompletedEvent extends EventOf3Entities<Microservice, Thread, MessageObject> {

    public ComputationCompletedEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Microservice microservice, Thread thread, MessageObject messageObject) {
        Request rqs = (Request) messageObject;
        sendTraceNote(String.format("Request %s was computed.", rqs.getQuotedName()));
        rqs.setComputation_completed();
    }

    @Override
    public void schedule(Microservice microservice, Thread thread, MessageObject messageObject) {
        super.schedule(microservice, thread, messageObject, getModel().presentTime());
    }
}
