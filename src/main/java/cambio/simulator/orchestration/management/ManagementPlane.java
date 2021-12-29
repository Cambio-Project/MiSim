package cambio.simulator.orchestration.management;


import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.RandomLoadBalanceStrategy;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.loadbalancing.LeastUtilizationLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import cambio.simulator.orchestration.loadbalancing.RandomLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.parsing.K8DeploymentDto;
import cambio.simulator.orchestration.scheduling.FirstFitScheduler;
import cambio.simulator.orchestration.scheduling.IScheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.*;

public class ManagementPlane {
    List<Deployment> deployments;
    Cluster cluster;
    Model model;
    Map<String, IScheduler> schedulerMap;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
        schedulerMap = new HashMap<>();
        deployments = new ArrayList<>();
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
    }

    public void addPodToSpecificSchedulerQueue(Pod pod, String schedulerType) {
        final IScheduler scheduler = schedulerMap.get(schedulerType);
        if (scheduler != null) {
            scheduler.getPodWaitingQueue().add(pod);
        } else {
            getModel().sendTraceNote("Unknown scheduler type: " + schedulerType + ". Cannot send" + pod + " to its scheduler queue");
        }
    }


    public void connectLoadBalancer(MicroserviceOrchestration microserviceOrchestration, ILoadBalancingStrategy loadBalancingStrategy) {
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategy) {
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), RandomLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), microserviceOrchestration));
        } else if (loadBalancingStrategy instanceof LeastUtilizationLoadBalanceStrategyOrchestration)
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), LeastUtilizationLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), loadBalancingStrategy, microserviceOrchestration));
    }

    public String getSchedulerByNameOrStandard(K8DeploymentDto k8DeploymentDto) {
        final String name = k8DeploymentDto.getSpec().getTemplate().getSpec().getSchedulerName();
        if (name != null) {
            final SchedulerType schedulerType = SchedulerType.fromString(name);
            if (schedulerType != null) {
                return schedulerType.getName();
            }
        }
        System.out.println("Using default Scheduler '" + SchedulerType.FIRSTFIT.getName() + "' for deployment "+ k8DeploymentDto.getMetadata().getName());
        return SchedulerType.FIRSTFIT.getName();
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

    public void populateSchedulerMap() {
        schedulerMap.put("firstFit", FirstFitScheduler.getInstance());
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
