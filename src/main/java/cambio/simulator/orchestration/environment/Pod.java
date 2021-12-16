package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import desmoj.core.simulator.Model;

import java.util.HashSet;
import java.util.Set;

public class Pod extends NamedEntity {
    private PodState podState;
    private Set<Container> containers;
    // Map<Container, Integer> restartBackoff = new HashMap<>();

    public Pod(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.containers = new HashSet<>();
        this.podState = PodState.PENDING;
    }

    public int getCPUDemand() {
        return this.getContainers().stream().mapToInt(container -> container.getMicroserviceInstance().getOwner().getCapacity()).sum();
    }

    public void die() {
        containers.forEach(Container::die);
        setPodState(PodState.FAILED);
    }

    public void startAllContainers(){
        getContainers().forEach(container -> container.setContainerState(ContainerState.RUNNING));
        getContainers().forEach(container -> container.getMicroserviceInstance().start());
        setPodState(PodState.RUNNING);
    }

    public void restartAllContainers() {
        containers.forEach(this::restartContainer);
        setPodState(PodState.RUNNING);
        sendTraceNote(this.getQuotedName() + " was restarted");
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
        if (podState == PodState.TERMINATING) {
            getContainers().forEach(container -> container.getMicroserviceInstance().startShutdown());
        } else if (podState == PodState.SUCCEEDED) {
            getContainers().forEach(container -> container.setContainerState(ContainerState.TERMINATED));
        }
    }

    //    https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
    public void restartTerminatedContainers() {
        getContainers().stream().filter(container -> container.getContainerState() == ContainerState.TERMINATED)
                .forEach(this::restartContainer);
    }

    public void restartContainer(Container container) {
        if (containers.contains(container)) {
            // TODO Check if this is correct
            MicroserviceInstance microserviceInstance = container.getMicroserviceInstance();
            //state must be switched from KILLED to SHUTDOWN. Otherwise start method would throw error
            microserviceInstance.setState(InstanceState.SHUTDOWN);
            microserviceInstance.getPatterns().forEach(InstanceOwnedPattern::start);
            microserviceInstance.start();
            container.setContainerState(ContainerState.RUNNING);
            sendTraceNote(microserviceInstance.getQuotedName() + " was restarted");
        } else {
            throw new IllegalArgumentException("Container " + container.getQuotedPlainName() + " does not belong to this pod");
        }
    }

}
