package cambio.simulator.orchestration;

import cambio.simulator.orchestration.deprecated.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Deployment {
    private Set<Service> services;
    private Set<Pod> replicaSet = new HashSet<>();
    private Scheduler scheduler;

    private int desiredReplicaCount;


    public Deployment(Set<Service> services, int desiredReplicaCount) {
        this.services = services;
        this.desiredReplicaCount = desiredReplicaCount;
    }


    /**
     * Schedules the Deployments. Services will be created. These create ServiceInstances that later are scheduled in pods
     */
    public void deploy() {

        while(getCurrentReplicaCount() != desiredReplicaCount) {

            final Pod pod = new Pod();
            for(Service service : services){
                final ServiceInstance microServiceInstance = service.createMicroServiceInstance();
                final Container container = new Container(microServiceInstance);
                pod.getContainers().add(container);
            }
            replicaSet.add(pod);
            schedulePod(pod);
        }
        System.out.println("Pods succesfully deployed for this deployment");

    }

    public void schedulePod(Pod pod){
        scheduler.schedulePod(pod);
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

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
