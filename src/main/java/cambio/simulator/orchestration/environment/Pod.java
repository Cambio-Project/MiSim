package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import cambio.simulator.orchestration.k8objects.Service;
import desmoj.core.simulator.Model;

import java.util.HashSet;
import java.util.Set;

public class Pod extends NamedEntity {
    private PodState podState;
    private Set<Container> containers;

    public Pod(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.containers = new HashSet<>();
        this.podState = PodState.PENDING;
    }

    public int getCPUDemand() {
        return this.getContainers().stream().mapToInt(container -> container.getMicroserviceInstance().getOwner().getCapacity()).sum();
    }

    public void kill(){
        containers.forEach(Container::kill);
        setPodState(PodState.FAILED);
    }

    public void restart(){
        containers.forEach(this::restartMicroService);
        setPodState(PodState.RUNNING);
        sendTraceNote(this.getQuotedName() + "was restarted");
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    public PodState getPodState() {
        return podState;
    }

    public void setPodState(PodState podState) {
        this.podState = podState;
    }

    //    https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
    public void applyRestartPolicy() {
        for (Container container : getContainers()) {
            if (container.getContainerState() == ContainerState.TERMINATED) {
                restartMicroService(container);
            }
        }
    }

    public void restartMicroService(Container container){
        MicroserviceInstance microserviceInstance = container.getMicroserviceInstance();
        microserviceInstance.setState(InstanceState.SHUTDOWN);
        microserviceInstance.getPatterns().forEach(InstanceOwnedPattern::start);
        microserviceInstance.start();
        container.setContainerState(ContainerState.RUNNING);
        sendTraceNote(microserviceInstance.getQuotedName() + " was restarted");
    }

}
