package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.models.ArchitectureModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * A <code>FinishEvent</code> is an <code>ExternalEvent</code> that is called upon the end of the simulation.
 * <p>
 * Its used for cleanup and finalizing statistics.
 */
public class FinishEvent extends ExternalEvent {

    public FinishEvent(Model owner, String name, boolean showInTraceMode) {
        super(owner, name, showInTraceMode);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

        ArchitectureModel.get().getMicroservices().forEach(Microservice::finalizeStatistics);

    }
}
