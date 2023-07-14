package cambio.simulator.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.MiSimReporters;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;

/**
 * A {@link SimulationEndEvent} is an {@link ExternalEvent} that is called upon the end of the simulation.
 *
 * <p>
 * It is used for cleanup and finalizing statistics.
 */
public class SimulationEndEvent extends NamedExternalEvent {

    private final MiSimModel model;

    /**
     * Creates a new {@link SimulationEndEvent} that finishes off the simulation.
     *
     * <p>
     * This Event automatically is assigned {@link Priority#HIGH} so it executes before the {@link
     * desmoj.core.simulator.ExternalEventStop} that stops the simulation.
     */
    public SimulationEndEvent(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.model = model;
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        model.getArchitectureModel().getMicroservices().forEach(Microservice::finalizeStatistics);
        MiSimReporters.finalizeReports();

        model.getExperimentMetaData().markEndOfExecution(System.nanoTime());
    }
}
