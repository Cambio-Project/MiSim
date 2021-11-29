package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.ManagementPlane;
import cambio.simulator.orchestration.environment.*;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Deployment extends NamedEntity {
    private Set<Service> services;
    private Set<Pod> replicaSet = new HashSet<>();
    private String schedulerType;


    private TimeInstant lastScaleUp = new TimeInstant(0);
    private TimeInstant lastScaleDown = new TimeInstant(0);
    private int desiredReplicaCount;
    private int maxReplicaCount = 10;
    private int minReplicaCount = 1;

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
        final int diff = Math.abs(getCurrentRunningOrPendingReplicaCount() - desiredReplicaCount);
        int i = 0;
        while (i< diff) {
            i++;
            if (getCurrentRunningOrPendingReplicaCount() < desiredReplicaCount) {
                createPod();
            } else {
                removePod();
            }
        }
    }

    public void createPod() {
        final Pod pod = new Pod(getModel(), "Pod", traceIsOn());
        for (Service service : services) {
            final MicroserviceInstance microServiceInstance = service.createMicroServiceInstance();
            final Container container = new Container(getModel(), "Container", traceIsOn(), microServiceInstance);
            pod.getContainers().add(container);
        }
        replicaSet.add(pod);
        //add to specific scheduler queue
        addPodToWaitingQueue(pod);
    }

    public void removePod(){
        final Optional<Pod> optionalPod = replicaSet.stream().filter(pod -> pod.getPodState() == PodState.RUNNING).findFirst();
        if(!optionalPod.isPresent()){
            //No Pod found that could be removed
            return;
        }
        final Pod podToRemove = optionalPod.get();
        final Optional<Node> first = ManagementPlane.getInstance().getCluster().getNodes().stream().filter(node -> node.getPods().contains(podToRemove)).findFirst();
        if(first.isPresent()){
            final Node node = first.get();
            node.startRemoving(podToRemove);
        }
    }

    public void addPodToWaitingQueue(Pod pod) {
        ManagementPlane.getInstance().addPodToSpecificSchedulerQueue(pod, this.getSchedulerType());
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

    public int getCurrentRunningOrPendingReplicaCount(){
        return (int) getReplicaSet().stream().filter(pod -> pod.getPodState()==PodState.RUNNING || pod.getPodState()==PodState.PENDING).count();
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

    public int getMaxReplicaCount() {
        return maxReplicaCount;
    }

    public void setMaxReplicaCount(int maxReplicaCount) {
        this.maxReplicaCount = maxReplicaCount;
    }

    public int getMinReplicaCount() {
        return minReplicaCount;
    }

    public void setMinReplicaCount(int minReplicaCount) {
        this.minReplicaCount = minReplicaCount;
    }

    public TimeInstant getLastScaleUp() {
        return lastScaleUp;
    }

    public void setLastScaleUp(TimeInstant lastScaleUp) {
        this.lastScaleUp = lastScaleUp;
    }

    public TimeInstant getLastScaleDown() {
        return lastScaleDown;
    }

    public void setLastScaleDown(TimeInstant lastScaleDown) {
        this.lastScaleDown = lastScaleDown;
    }
}
