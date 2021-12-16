package cambio.simulator.orchestration.management;


import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.RandomLoadBalanceStrategy;
import cambio.simulator.models.ArchitectureModel;
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
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;
import cambio.simulator.orchestration.scaling.IAutoScaler;
import cambio.simulator.orchestration.scheduling.FirstFitScheduler;
import cambio.simulator.orchestration.scheduling.IScheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.*;
import java.util.stream.Collectors;

public class ManagementPlane {
    List<Deployment> deployments;
    Cluster cluster;
    Model model;
    Map<String, IScheduler> schedulerMap;
    Map<String, IAutoScaler> scalerMap;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
        schedulerMap = new HashMap<>();
        scalerMap = new HashMap<>();
        deployments = new ArrayList<>();
    }

    public static ManagementPlane getInstance() {
        return instance;
    }

    public void buildDeploymentScheme(ArchitectureModel architectureModel) {
//        final Set<MicroserviceOrchestration> services = architectureModel.getServices();
//        connectLoadBalancersToServices(services);
//        for (MicroserviceOrchestration service : services) {
//            Set<MicroserviceOrchestration> deploymentServices = new HashSet<>();
//            deploymentServices.add(service);
//            deployments.add(new Deployment(getModel(), "Deployment_" + ++deploymentCounter, getModel().traceIsOn(), deploymentServices, service.getStartingInstanceCount(), "firstFit"));
//        }
    }

    /**
     * For each deployment, check if desired state equals current state, if not trigger actions
     */
    public void maintainDeployments() {
        for (Deployment deployment : deployments) {
            deployment.deploy();
        }
    }

    public void populateSchedulerMap() {
        schedulerMap.put("firstFit", FirstFitScheduler.getInstance());
    }

    // TODO Delete this map -> scaler should be mapped to deployments
    public void populateScalerMap() {
        scalerMap.put("HPA", HorizontalPodAutoscaler.getInstance());
    }

    public void connectLoadBalancer(MicroserviceOrchestration microserviceOrchestration, ILoadBalancingStrategy loadBalancingStrategy) {
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategy) {
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), RandomLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), microserviceOrchestration));
        } else if (loadBalancingStrategy instanceof LeastUtilizationLoadBalanceStrategyOrchestration)
            microserviceOrchestration.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), LeastUtilizationLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), loadBalancingStrategy, microserviceOrchestration));
    }

//    public void connectLoadBalancerToService(MicroserviceOrchestration service) {
////        service.setLoadBalancer(new LoadBalancerOrchestration(getModel(), "RandomLoadBalancer", getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), service));
////        service.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), "LeastUtilLB", getModel().traceIsOn(), new LeastUtilizationLoadBalanceStrategyOrchestration(), service));
//    }

    //Do this periodically
    public void checkForPendingPods() {
        schedulerMap.values().forEach(IScheduler::schedulePods);
    }

    public String getSchedulerByNameOrStandard(String name) {
        if (name != null) {
            final SchedulerType schedulerType = SchedulerType.fromString(name);
            if(schedulerType!=null){
                return schedulerType.getName();
            }
        }
        System.out.println("Using default Scheduler: " + SchedulerType.FIRSTFIT.getName());
        return SchedulerType.FIRSTFIT.getName();
    }

    public Optional<Node> getNodeForPod(Pod pod) {
        return getCluster().getNodes().stream().filter(node -> node.getPods().contains(pod)).findFirst();
    }

    public void addPodToSpecificSchedulerQueue(Pod pod, String schedulerType) {
        final IScheduler scheduler = schedulerMap.get(schedulerType);
        if (scheduler != null) {
            scheduler.getPodWaitingQueue().add(pod);
        } else {
            getModel().sendTraceNote("Unknown scheduler type: " + schedulerType + ". Cannot send" + pod + " to its scheduler queue");
        }
    }

    public void checkForScaling() {
        // TODO: for each deployment call checkForScaling
        /*
        for (Deployment deployment : deployments) {
            deployment.checkForScaling();
        }
         */
        final IAutoScaler hpa = scalerMap.get("HPA");
        final Set<Deployment> deploymentsWithHPA = deployments.stream().filter(deployment -> deployment.getScalerType() != null && deployment.getScalerType().equals("HPA")).collect(Collectors.toSet());
        for (Deployment deployment : deploymentsWithHPA) {
            //make event out of it
            hpa.apply(deployment);
        }
    }

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

    public Map<String, IScheduler> getSchedulerMap() {
        return schedulerMap;
    }

    public void setSchedulerMap(Map<String, IScheduler> schedulerMap) {
        this.schedulerMap = schedulerMap;
    }
}
