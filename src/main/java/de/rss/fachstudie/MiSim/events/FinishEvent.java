package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * A <code>FinishEvent</code> is an <code>ExternalEvent</code> that is called upon the end of the simulation.
 */
public class FinishEvent extends ExternalEvent {
    private MainModel model;

    public FinishEvent(Model owner,  String name, boolean showInTraceMode) {
        super(owner, name, showInTraceMode);

        model = (MainModel) owner;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

        MainModel.microservices.forEach(Microservice::finalizeStatistics);

    }
}
