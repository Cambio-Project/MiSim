package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.k8objects.Deployment;
import desmoj.core.simulator.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Pod extends NamedEntity {
    private PodState podState;
    private Set<Container> containers;
    private Node lastKnownNode;
    private Deployment owner;

    public Pod(Model model, String name, boolean showInTrace, Deployment deployment) {
        super(model, name, showInTrace);
        this.containers = new HashSet<>();
        this.podState = PodState.PENDING;
        this.owner = deployment;
    }

    public int getCPUDemand() {
        return this.getContainers().stream().mapToInt(container -> container.getMicroserviceInstance().getOwner().getCapacity()).sum();
    }

    public void die() {
        setPodStateAndApplyEffects(PodState.FAILED);
    }

    public void startAllContainers() {
        setPodStateAndApplyEffects(PodState.RUNNING);
    }

//    /**
//     * Should be called when a Pod has died due to a ChaosMonkeyForPodsEvents.
//     * It restarts all containers that belong to this pod.
//     */
//    public void restartAllContainers() {
//        containers.forEach(container -> container.restartTerminatedContainer());
//    }

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

    public void setPodStateAndApplyEffects(PodState podState) {
        this.podState = podState;
        if (podState == PodState.TERMINATING) {
            getContainers().forEach(container -> container.getMicroserviceInstance().startShutdown());
        } else if (podState == PodState.SUCCEEDED) {
            getContainers().forEach(container -> container.setContainerState(ContainerState.TERMINATED));
        } else if (podState == PodState.FAILED) {
            getContainers().forEach(Container::die);
        } else if (podState == PodState.RUNNING) {
            List<Container> collect = getContainers().stream().filter(container -> !container.getContainerState().equals(ContainerState.RUNNING)).collect(Collectors.toList());
            collect.forEach(container -> container.start());
        }
    }

    public Node getLastKnownNode() {
        return lastKnownNode;
    }

    public void setLastKnownNode(Node lastKnownNode) {
        this.lastKnownNode = lastKnownNode;
    }

    public Deployment getOwner() {
        return owner;
    }
}
