package cambio.simulator.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.models.ArchitectureModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * A {@link FinishEvent} is an {@link ExternalEvent} that is called upon the end of the simulation.
 *
 * <p>
 * Its used for cleanup and finalizing statistics.
 */
public class FinishEvent extends NamedExternalEvent {

    public FinishEvent(Model owner, String name, boolean showInTraceMode) {
        super(owner, name, showInTraceMode);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        ArchitectureModel.get().getMicroservices().forEach(Microservice::finalizeStatistics);

    }
}
