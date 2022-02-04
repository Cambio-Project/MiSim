package cambio.simulator.orchestration.management;


import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.scheduling.IScheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.rmi.UnexpectedException;
import java.util.*;
import java.util.stream.Collectors;

public class ManagementPlane {
    List<Deployment> deployments;
    Cluster cluster;
    Model model;
    Map<SchedulerType, IScheduler> schedulerMap;
    Map<String, String> defaultValues;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
        schedulerMap = new HashMap<>();
        deployments = new ArrayList<>();
        defaultValues = new HashMap<>();
    }

    public static ManagementPlane getInstance() {
        return instance;
    }


    /**
     * For each deployment, check if rescaling is required
     */
    public void checkForScaling() {
        deployments.forEach(Deployment::scale);
    }

    /**
     * For each deployment, check if desired state equals current state, if not trigger actions
     */
    public void maintainDeployments() {
        for (Deployment deployment : deployments) {
            deployment.deploy();
        }
    }

    /**
     * Each scheduler will try to schedule the pending pods that are waiting in its queue
     */
    public void checkForPendingPods() {
        schedulerMap.values().forEach(IScheduler::schedulePods);

        String nodeInfo = "NAME | CPUAvail | CPURes | #pods";
        getModel().sendTraceNote(nodeInfo);
        for (Node node : getCluster().getNodes()) {
            getModel().sendTraceNote(node.getName() + " | " + node.getTotalCPU() + " | " + node.getReserved() + " | " + node.getPods().size());
        }

        getModel().sendTraceNote(nodeInfo);

    }

    public void addPodToSpecificSchedulerQueue(Pod pod, SchedulerType schedulerType) {
        final IScheduler scheduler = schedulerMap.get(schedulerType);
        if (scheduler != null) {
            scheduler.getPodWaitingQueue().add(pod);
        } else {
            try {
                throw new UnexpectedException("Scheduler type: " + schedulerType + " is not known in schedulerMap");
            } catch (UnexpectedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public Optional<Node> getNodeForPod(Pod pod) {
        return getCluster().getNodes().stream().filter(node -> node.getPods().contains(pod)).findFirst();
    }

    /**
     * Check if a pod's containers have no calculations left. If so then remove it from the pod.
     * Otherwise this method schedules itself again for a later time
     */
    public void checkIfPodRemovableFromNode(Pod pod, Node node) {
        for (Container container : pod.getContainers()) {
            if (container.getMicroserviceInstance().getState() != InstanceState.SHUTDOWN) {

                double relativeWorkDemand = container.getMicroserviceInstance().getRelativeWorkDemand();
                getModel().sendTraceNote("Cannot remove pod with " +container.getMicroserviceInstance().getName() + " because at least one container is still calculating. Current Relative WorkDemand: " + relativeWorkDemand);
                final CheckPodRemovableEvent checkPodRemovableEvent = new CheckPodRemovableEvent(getModel(), "Check if pod can be removed", getModel().traceIsOn());
                checkPodRemovableEvent.schedule(pod, node, new TimeSpan(2));
                return;
            }
        }
        removePodFromNode(pod, node);
    }

    public void removePodFromNode(Pod pod, Node node) {
        node.removePod(pod);
    }

    public void populateSchedulers() {
        final Set<SchedulerType> usedSchedulerTypes = deployments.stream().map(deployment -> deployment.getSchedulerType()).collect(Collectors.toSet());
        usedSchedulerTypes.forEach(schedulerType -> {
            try {
                schedulerMap.put(schedulerType, Util.getInstance().getSchedulerInstanceByType(schedulerType));
            } catch (UnexpectedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
        getModel().sendTraceNote("[INFO] Active Schedulers: " + schedulerMap.values());

    }

    public Pod getPodByName(String name) {
        List<Pod> collect = deployments.stream().map(deployment -> deployment.getReplicaSet().stream().collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<Pod> first = collect.stream().filter(pod -> pod.getName().equals(name)).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return null;
    }

    /**
     * Returns all pods that are known by all nodes. That means they either are running or at least placed on the node
     * while waiting for being started
     *
     * @return
     */
    public List<Pod> getAllPodsPlacedOnNodes() {
        List<Pod> collect = cluster.getNodes().stream().map(node -> node.getPods().stream().collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        return collect;
    }

    public Deployment getDeploymentForPod(Pod pod){
        Optional<Deployment> first = deployments.stream().filter(deployment -> deployment.getReplicaSet().contains(pod)).findFirst();
        if(first.isPresent()){
            return first.get();
        }
        return null;
    }

    public Pod getPodForContainer(Container container){
        List<Pod> collect = deployments.stream().map(deployment -> deployment.getReplicaSet()).flatMap(Collection::stream).collect(Collectors.toList());
        Optional<Pod> first = collect.stream().filter(pod -> pod.getContainers().contains(container)).findFirst();
        if(first.isPresent()){
            return first.get();
        }
        return null;
    }

    public List<Deployment> getDeployments() {
        return deployments;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

}
