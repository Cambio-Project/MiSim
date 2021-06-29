package cambio.simulator.entities.microservice;

import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * For now this is an unused event that represents the killing of an instance.
 *
 * @author Lion Wagner
 */
public class InstanceKillEvent extends Event<MicroserviceInstance> {
    public InstanceKillEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.VERY_HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {
        microserviceInstance.die();
    }
}
