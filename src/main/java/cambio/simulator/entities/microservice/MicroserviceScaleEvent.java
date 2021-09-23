package cambio.simulator.entities.microservice;

import cambio.simulator.entities.NamedExternalEvent;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * For now this is an unused event to represent the scaling of a microservice.
 *
 * @author Lion Wagner
 */
public class MicroserviceScaleEvent extends NamedExternalEvent {

    private final Microservice microservice;
    private final int targetInstanceCount;

    /**
     * Creates a new scaling event.
     */
    public MicroserviceScaleEvent(Model model, String name, boolean showInTrace, Microservice microservice,
                                  int targetInstanceCount) {
        super(model, name, showInTrace);
        this.microservice = microservice;
        this.targetInstanceCount = targetInstanceCount;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        microservice.scaleToInstancesCount(targetInstanceCount);
    }

}
