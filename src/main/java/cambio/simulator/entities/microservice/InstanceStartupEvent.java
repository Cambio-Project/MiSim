package cambio.simulator.entities.microservice;

import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;

/**
 * Triggers the startup procedure for a {@code MicroserviceInstance}.
 *
 * @author Lion Wagner
 */
public class InstanceStartupEvent extends Event<MicroserviceInstance> {

    public InstanceStartupEvent(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {

        microserviceInstance.start();
    }
}
