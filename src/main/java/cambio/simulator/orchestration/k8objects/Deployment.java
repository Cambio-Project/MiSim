package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.events.RestartPodEvent;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.parsing.K8Kind;
import cambio.simulator.orchestration.scaling.AutoScaler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Deployment extends K8Object {
    private Set<MicroserviceOrchestration> services;
    private Set<Pod> replicaSet;
    private SchedulerType schedulerType;
    private TimeInstant lastRescaling;
    private int desiredReplicaCount;
    private int maxReplicaCount;
    private int minReplicaCount;
    private double averageUtilization;
    private AutoScaler autoScaler;

    public Deployment(Model model, String name, boolean showInTrace, Set<MicroserviceOrchestration> microserviceOrchestrations, int desiredReplicaCount, SchedulerType schedulerType) {
        super(model, name, showInTrace, K8Kind.DEPLOYMENT);
        this.services = microserviceOrchestrations;
        this.desiredReplicaCount = desiredReplicaCount;
        this.schedulerType = schedulerType;
        this.maxReplicaCount = 10;
        this.minReplicaCount = 1;
        this.averageUtilization = 50.0;
        this.lastRescaling = new TimeInstant(0);
        replicaSet = new HashSet<>();
    }

    public void deploy() {
        final int diff = Math.abs(getCurrentRunningOrPendingReplicaCount() - desiredReplicaCount);
        int i = 0;
        sendTraceNote("Checking state of deployment " + this.getQuotedPlainName());
        if (diff == 0) {
            sendTraceNote("no action needed");
        } else {
            while (i < diff) {
                i++;
                if (getCurrentRunningOrPendingReplicaCount() < desiredReplicaCount) {
                    createPod();
                } else {
                    removePod();
                }
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

        final Pod pod = new Pod(getModel(), "Pod-"+this.getPlainName(), traceIsOn());
        for (MicroserviceOrchestration microserviceOrchestration : services) {
            final MicroserviceInstance microServiceInstance = microserviceOrchestration.createMicroServiceInstance();
            final Container container = new Container(getModel(), "Container[" + microserviceOrchestration.getPlainName()+"]", traceIsOn(), microServiceInstance);
            pod.getContainers().add(container);
        }
        replicaSet.add(pod);
        //add to specific scheduler queue
        addPodToWaitingQueue(pod);
    }

    public void removePod() {
        final Optional<Pod> optionalPod = replicaSet.stream().filter(pod -> pod.getPodState() == PodState.RUNNING).findFirst();
        if (!optionalPod.isPresent()) {
            sendTraceNote("There is not pod that could be removed");
            return;
        }
        final Pod podToRemove = optionalPod.get();
        final Optional<Node> first = ManagementPlane.getInstance().getNodeForPod(podToRemove);
        if (first.isPresent()) {
            final Node node = first.get();
            node.startRemovingPod(podToRemove);
        } else {
            throw new IllegalStateException("There is no node which knows the pod " + podToRemove.getQuotedPlainName());
        }
    }


    public synchronized void killPodInstances(final int numberOfInstances) {
        final int maxKills = Math.max(0, Math.min(numberOfInstances, getCurrentRunningOrPendingReplicaCount()));
        for (int i = 0; i < maxKills; i++) {
            killPodInstance();
        }
    }

    /**
     * Kills a random instance. Can be called on a deployment that has 0 running instances.
     */
    public synchronized void killPodInstance() {
        Pod instanceToKill =
                getCurrentRunningOrPendingReplicas().stream().findAny().orElse(null); //selects an element of the stream, not
        if (instanceToKill == null) {
            return;
        }
        instanceToKill.die();

    }

    public void scale(){
        if(autoScaler!=null){
            autoScaler.apply(this);
        }
    }

    public void addPodToWaitingQueue(Pod pod) {
        ManagementPlane.getInstance().addPodToSpecificSchedulerQueue(pod, this.getSchedulerType());
    }

    public Set<MicroserviceOrchestration> getServices() {
        return services;
    }

    public void setServices(Set<MicroserviceOrchestration> microserviceOrchestrations) {
        this.services = microserviceOrchestrations;
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

    public Set<Pod> getRunningReplicas(){
        return getReplicaSet().stream().filter(pod -> pod.getPodState() == PodState.RUNNING).collect(Collectors.toSet());
    }

    public Set<Pod> getReplicaSet() {
        return replicaSet;
    }

    public void setReplicaSet(Set<Pod> replicaSet) {
        this.replicaSet = replicaSet;
    }

    public SchedulerType getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(SchedulerType schedulerType) {
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

    public TimeInstant getLastRescaling() {
        return lastRescaling;
    }

    public void setLastRescaling(TimeInstant lastRescaling) {
        this.lastRescaling = lastRescaling;
    }

    public double getAverageUtilization() {
        return averageUtilization;
    }

    public void setAverageUtilization(double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }

    public AutoScaler getAutoScaler() {
        return autoScaler;
    }

    public void setAutoScaler(AutoScaler autoScaler) {
        this.autoScaler = autoScaler;
    }
}
