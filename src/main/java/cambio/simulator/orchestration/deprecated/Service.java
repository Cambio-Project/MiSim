package cambio.simulator.orchestration.deprecated;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.orchestration.ServiceInstance;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

import java.util.*;

public class Service extends Microservice {

    /**
     * Creates a new instance of a {@link Microservice}.
     *
     */
    public Service(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    public int getCpuDemand() {
        return Arrays.stream(super.getOperations()).mapToInt(Operation::getDemand).sum();
    }

    public ServiceInstance createMicroServiceInstance(){
        Event<MicroserviceInstance> changeEvent;
        ServiceInstance changedInstance;
        changedInstance =
                new ServiceInstance(getModel(), String.format("[%s]_I%d", getName(), instanceSpawnCounter),
                        this.traceIsOn(), this, instanceSpawnCounter);
        changedInstance.activatePatterns(instanceOwnedPatternConfigurations);

        instanceSpawnCounter++;
        changeEvent =
                new InstanceStartupEvent(getModel(), "Instance Startup of " + changedInstance.getQuotedName(),
                        traceIsOn());
        instancesSet.add(changedInstance);
        changeEvent.schedule(changedInstance, presentTime());
        return changedInstance;
    }

}
