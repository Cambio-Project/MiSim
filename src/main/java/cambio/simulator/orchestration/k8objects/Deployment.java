package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.events.RestartPodEvent;
import cambio.simulator.orchestration.events.StartPodShutdown;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.*;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Deployment extends K8Object {
    private Set<Service> services;
    private Set<Pod> replicaSet = new HashSet<>();
    private String schedulerType;
    private String scalerType;


    private TimeInstant lastScaleUp = new TimeInstant(0);
    private TimeInstant lastScaleDown = new TimeInstant(0);
    private int desiredReplicaCount;
    private int maxReplicaCount = 10;
    private int minReplicaCount = 1;
    private double averageUtilization = 50.0;

    public Deployment(Model model, String name, boolean showInTrace, Set<Service> services, int desiredReplicaCount, String schedulerType) {
        super(model, name, showInTrace, K8Kind.DEPLOYMENT);
        this.services = services;
        this.desiredReplicaCount = desiredReplicaCount;
        this.schedulerType = schedulerType;
    }

    public void deploy() {
        final int diff = Math.abs(getCurrentRunningOrPendingReplicaCount() - desiredReplicaCount);
        int i = 0;
        while (i < diff) {
            i++;
            if (getCurrentRunningOrPendingReplicaCount() < desiredReplicaCount) {
                createPod();
            } else {
                removePod();
            }
        }
    }

    public synchronized void createPod() {

        //first try to restart failed pods
        final Optional<Pod> first = replicaSet.stream().filter(pod -> pod.getPodState().equals(PodState.FAILED)).findFirst();
        if(first.isPresent()){
            final Pod pod = first.get();
            final RestartPodEvent restartPodEvent = new RestartPodEvent(getModel(), "RestartPodEvent", traceIsOn());
            restartPodEvent.schedule(pod, presentTime());
            return;
        }

        final Pod pod = new Pod(getModel(), "Pod", traceIsOn());
        for (Service service : services) {
            final MicroserviceInstance microServiceInstance = service.createMicroServiceInstance();
            final Container container = new Container(getModel(), "Container[" + service.getPlainName()+"]", traceIsOn(), microServiceInstance);
            pod.getContainers().add(container);
        }
        replicaSet.add(pod);
        //add to specific scheduler queue
        addPodToWaitingQueue(pod);
    }

    public void removePod() {
        final Optional<Pod> optionalPod = replicaSet.stream().filter(pod -> pod.getPodState() == PodState.RUNNING).findFirst();
        if (!optionalPod.isPresent()) {
            //No Pod found that could be removed
            return;
        }
        final Pod podToRemove = optionalPod.get();
        final Optional<Node> first = ManagementPlane.getInstance().getCluster().getNodes().stream().filter(node -> node.getPods().contains(podToRemove)).findFirst();
        if (first.isPresent()) {
            final Node node = first.get();
            //need to set other state than running. Happens in event anyways but event is delayed. This avoids picking the same pod from the replicaSet
            podToRemove.setPodState(PodState.PRETERMINATING);
            final StartPodShutdown startPodShutdownEvent = new StartPodShutdown(getModel(), "StartPodShutdownEvent", traceIsOn());
            startPodShutdownEvent.schedule(podToRemove, node, presentTime());
        }
    }


    public synchronized void killPodInstances(final int numberOfInstances) {
        final int maxKills = Math.max(0, Math.min(numberOfInstances, getCurrentRunningOrPendingReplicaCount()));
        for (int i = 0; i < maxKills; i++) {
            killPodInstance();
        }
    }

    /**
     * Kills a random instance. Can be called on a service that has 0 running instances.
     */
    public synchronized void killPodInstance() {
        Pod instanceToKill =
                getCurrentRunningOrPendingReplicas().stream().findAny().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        instanceToKill.die();

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

    public int getCurrentRunningOrPendingReplicaCount() {
        return getCurrentRunningOrPendingReplicas().size();
    }

    public Set<Pod> getCurrentRunningOrPendingReplicas(){
        return getReplicaSet().stream().filter(pod -> pod.getPodState() == PodState.RUNNING || pod.getPodState() == PodState.PENDING).collect(Collectors.toSet());
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

    public String getScalerType() {
        return scalerType;
    }

    public void setScalerType(String scalerType) {
        this.scalerType = scalerType;
    }

    public double getAverageUtilization() {
        return averageUtilization;
    }

    public void setAverageUtilization(double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }
}
