package cambio.simulator.orchestration;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

public class Container extends NamedEntity {
    private MicroserviceInstance microserviceInstance;
    private ContainerState containerState;

    public Container(Model model, String name, boolean showInTrace, MicroserviceInstance microserviceInstance) {
        super(model, name, showInTrace);
        this.microserviceInstance = microserviceInstance;
        this.containerState = ContainerState.WAITING;
    }

    public MicroserviceInstance getMicroserviceInstance() {
        return microserviceInstance;
    }

    public void setMicroserviceInstance(MicroserviceInstance microserviceInstance) {
        this.microserviceInstance = microserviceInstance;
    }

    public ContainerState getContainerState() {
        return containerState;
    }

    public void setContainerState(ContainerState containerState) {
        this.containerState = containerState;
    }
}
