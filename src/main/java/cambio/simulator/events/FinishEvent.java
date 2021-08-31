package cambio.simulator.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.models.MiSimModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;

/**
 * A {@link FinishEvent} is an {@link ExternalEvent} that is called upon the end of the simulation.
 *
 * <p>
 * Its used for cleanup and finalizing statistics.
 */
public class FinishEvent extends NamedExternalEvent {

    private final MiSimModel model;

    public FinishEvent(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.model = model;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        model.getArchitectureModel().getMicroservices().forEach(Microservice::finalizeStatistics);

    }
}
