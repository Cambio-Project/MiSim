package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.patterns.InstanceOwnedPattern;
import desmoj.core.simulator.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void startAllContainers() {
        List<Container> collect = getContainers().stream().filter(container -> !container.getContainerState().equals(ContainerState.RUNNING)).collect(Collectors.toList());
        collect.forEach(container -> container.setContainerState(ContainerState.RUNNING));
        collect.forEach(container -> container.getMicroserviceInstance().start());
        setPodState(PodState.RUNNING);
    }

    /**
     * Should be called when a Pod has died due to a ChaosMonkeyForPodsEvents.
     * It restarts all containers that belong to this pod.
     */
    public void restartAllContainers() {
        containers.forEach(container -> container.restart());
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
}
