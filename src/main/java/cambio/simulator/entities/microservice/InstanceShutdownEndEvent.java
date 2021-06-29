package cambio.simulator.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Represents the end of the shutdown process of an instance.
 *
 * @author Lion Wagner
 */
public class InstanceShutdownEndEvent extends Event<MicroserviceInstance> {

    public InstanceShutdownEndEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {
        microserviceInstance.endShutdown();
    }
}
