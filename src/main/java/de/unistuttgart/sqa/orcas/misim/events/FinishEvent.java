package de.unistuttgart.sqa.orcas.misim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.Microservice;
import de.unistuttgart.sqa.orcas.misim.models.ArchitectureModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * A {@link FinishEvent} is an {@link ExternalEvent} that is called upon the end of the simulation.
 *
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
