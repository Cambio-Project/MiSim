package cambio.simulator.entities.microservice;

import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Triggers the instance to stats its shutdown process.
 *
 * <p>
 * During the shutdown processes a {@code MicroserviceInstance} does not accept new requests but finishes the ones its
 * currently handling.
 *
 * @author Lion Wagner
 */
public class InstanceShutdownStartEvent extends Event<MicroserviceInstance> {


    public InstanceShutdownStartEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(MicroserviceInstance microserviceInstance) throws SuspendExecution {
        microserviceInstance.startShutdown();
    }
}
