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
import cambio.simulator.orchestration.events.DeploymentEvent;
import cambio.simulator.orchestration.events.FinishPodShutdown;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.Service;
import cambio.simulator.orchestration.loadbalancing.LeastUtilizationLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import cambio.simulator.orchestration.loadbalancing.RandomLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.scaling.HorizontalPodAutoscaler;
import cambio.simulator.orchestration.scaling.IAutoScaler;
import cambio.simulator.orchestration.scheduling.FirstFitScheduler;
import cambio.simulator.orchestration.scheduling.IScheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import com.google.common.base.Enums;
import com.google.common.base.Optional;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.*;
import java.util.stream.Collectors;

public class ManagementPlane {
    List<Deployment> deployments = new ArrayList<>();
    Cluster cluster;
    Model model;
    LinkedList<Pod> podWaitingQueue = new LinkedList<>();
    Map<String, IScheduler> schedulerMap = new HashMap<>();
    Map<String, IAutoScaler> scalerMap = new HashMap<>();

    public static int deploymentCounter = 0;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
    }

    public static ManagementPlane getInstance() {
        return instance;
    }

    public void buildDeploymentScheme(ArchitectureModel architectureModel) {
//        final Set<Service> services = architectureModel.getServices();
//        connectLoadBalancersToServices(services);
//        for (Service service : services) {
//            Set<Service> deploymentServices = new HashSet<>();
//            deploymentServices.add(service);
//            deployments.add(new Deployment(getModel(), "Deployment_" + ++deploymentCounter, getModel().traceIsOn(), deploymentServices, service.getStartingInstanceCount(), "firstFit"));
//        }
    }

    public void applyDeploymentScheme() {
        for (Deployment deployment : deployments) {
            final DeploymentEvent deploymentEvent = new DeploymentEvent(getModel(), String.format("Taking care of deployment state %s ", deployment.getQuotedName()), getModel().traceIsOn());
            deploymentEvent.schedule(deployment, getModel().presentTime());
        }
    }

    public void populateSchedulerMap() {
        schedulerMap.put("firstFit", FirstFitScheduler.getInstance());
    }

    public void populateScalerMap() {
        scalerMap.put("HPA", HorizontalPodAutoscaler.getInstance());
    }

    public void connectLoadBalancer(Service service, ILoadBalancingStrategy loadBalancingStrategy) {
        if (loadBalancingStrategy instanceof RandomLoadBalanceStrategy) {
            service.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), RandomLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), service));
        } else if (loadBalancingStrategy instanceof LeastUtilizationLoadBalanceStrategyOrchestration)
            service.setLoadBalancerOrchestration(new LoadBalancerOrchestration(getModel(), LeastUtilizationLoadBalanceStrategyOrchestration.getName(), getModel().traceIsOn(), loadBalancingStrategy, service));

    }

//    public void connectLoadBalancerToService(Service service) {
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

    public void addPodToSpecificSchedulerQueue(Pod pod, String schedulerType) {
        final IScheduler scheduler = schedulerMap.get(schedulerType);
        if (scheduler != null) {
            scheduler.getPodWaitingQueue().add(pod);
        } else {
            getModel().sendTraceNote("Unknown scheduler type: " + schedulerType + ". Cannot send" + pod + " to its scheduler queue");
        }
    }

    public void checkForScaling() {
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
        final FinishPodShutdown finishPodShutdown = new FinishPodShutdown(getModel(), "Remove Pod from Node Event", getModel().traceIsOn());
        finishPodShutdown.schedule(pod, node, model.presentTime());

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

    public LinkedList<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }

    public void setPodWaitingQueue(LinkedList<Pod> podWaitingQueue) {
        this.podWaitingQueue = podWaitingQueue;
    }

    public Map<String, IScheduler> getSchedulerMap() {
        return schedulerMap;
    }

    public void setSchedulerMap(Map<String, IScheduler> schedulerMap) {
        this.schedulerMap = schedulerMap;
    }
}
