package cambio.simulator.entities.microservice;

import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Triggers the startup procedure for a {@code MicroserviceInstance}.
 *
 * @author Lion Wagner
 */
public class InstanceStartupEvent extends Event<MicroserviceInstance> {
    public static int counter = 0;

    public InstanceStartupEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {

        microserviceInstance.start();
    }
}
