package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.ManagementPlane;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.events.StartPendingPodsEvent;
import cambio.simulator.orchestration.scheduling.IScheduler;
import desmoj.core.simulator.Model;

import java.util.HashSet;
import java.util.Set;

public class Deployment extends NamedEntity {
    private Set<Service> services;
    private Set<Pod> replicaSet = new HashSet<>();
    private String schedulerType;

    private int desiredReplicaCount;

    public Deployment(Model model, String name, boolean showInTrace, Set<Service> services, int desiredReplicaCount, String schedulerType) {
        super(model, name, showInTrace);
        this.services = services;
        this.desiredReplicaCount = desiredReplicaCount;
        this.schedulerType = schedulerType;
    }

    /**
     * Schedules the Deployments. Services will be created. These create ServiceInstances that later are scheduled in pods
     */
    public void deploy() {

        while(getCurrentReplicaCount() != desiredReplicaCount) {

            final Pod pod = new Pod(getModel(), "Pod", traceIsOn());
            for(Service service : services){
                final MicroserviceInstance microServiceInstance = service.createMicroServiceInstance();
                final Container container = new Container(getModel(), "Container", traceIsOn(), microServiceInstance);
                pod.getContainers().add(container);
            }
            replicaSet.add(pod);
            addPodToWaitingQueue(pod);
        }
        System.out.println("Pods succesfully deployed for this deployment");

    }

    public void addPodToWaitingQueue(Pod pod){
        ManagementPlane.getInstance().getPodWaitingQueue().add(pod);
    }

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }

    public int getDesiredReplicaCount() {
        return desiredReplicaCount;
    }

    public void setDesiredReplicaCount(int desiredReplicaCount) {
        this.desiredReplicaCount = desiredReplicaCount;
    }

    public int getCurrentReplicaCount() {
        return getReplicaSet().size();
    }

    public Set<Pod> getReplicaSet() {
        return replicaSet;
    }

    public void setReplicaSet(Set<Pod> replicaSet) {
        this.replicaSet = replicaSet;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }
}
