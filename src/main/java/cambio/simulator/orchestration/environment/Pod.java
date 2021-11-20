package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
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
        int cpuDemand = 0;
        for (Container container : this.getContainers()) {
            final Service microservice = (Service) container.getMicroserviceInstance().getOwner();
            cpuDemand += microservice.getCapacity();
        }
        return cpuDemand;
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
                final Service service = (Service) container.getMicroserviceInstance().getOwner();
                final MicroserviceInstance newMicroServiceInstance = service.createMicroServiceInstance();
                container.setMicroserviceInstance(newMicroServiceInstance);
                container.setContainerState(ContainerState.RUNNING);
                newMicroServiceInstance.start();
                //Warum kommt Trace Nachricht nicht durch?
                sendTraceNote(newMicroServiceInstance.getQuotedName() + " was restarted");
            }
        }
    }

}
