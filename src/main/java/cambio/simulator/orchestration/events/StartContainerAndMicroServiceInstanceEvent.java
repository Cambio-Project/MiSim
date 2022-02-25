package cambio.simulator.orchestration.events;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

public class StartContainerAndMicroServiceInstanceEvent extends Event<Container> {

    public StartContainerAndMicroServiceInstanceEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Container container) {
        container.getMicroserviceInstance().start();
        container.setContainerState(ContainerState.RUNNING);
    }
}
