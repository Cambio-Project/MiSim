package de.unistuttgart.sqa.orcas.misim.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import de.unistuttgart.sqa.orcas.misim.misc.Priority;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Triggers the startup procedure for a {@code MicroserviceInstance}.
 *
 * @author Lion Wagner
 */
public class InstanceStartupEvent extends Event<MicroserviceInstance> {

    public InstanceStartupEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {

        microserviceInstance.start();
    }
}
