package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.MicroserviceInstance;

public class Container {
    private MicroserviceInstance microserviceInstance;
    private ContainerState containerState;

    public Container(MicroserviceInstance microserviceInstance) {
        this.microserviceInstance = microserviceInstance;
        this.containerState = ContainerState.IDLE;
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
