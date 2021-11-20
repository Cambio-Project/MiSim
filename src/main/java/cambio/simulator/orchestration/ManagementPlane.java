package cambio.simulator.orchestration;


import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.events.DeploymentEvent;
import cambio.simulator.orchestration.events.StartPendingPodsEvent;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.Service;
import cambio.simulator.orchestration.loadbalancing.LoadBalancerOrchestration;
import cambio.simulator.orchestration.loadbalancing.RandomLoadBalanceStrategyOrchestration;
import cambio.simulator.orchestration.scheduling.FirstFitScheduler;
import cambio.simulator.orchestration.scheduling.IScheduler;
import desmoj.core.simulator.Model;

import java.util.*;

public class ManagementPlane {
    List<Deployment> deployments = new ArrayList<>();
    Cluster cluster;
    Model model;
    LinkedList<Pod> podWaitingQueue = new LinkedList<>();
    Map<String, IScheduler> schedulerMap = new HashMap<>();
    //Map<String, IScheduler> Name & SChedulerObjekt, nichts -> dann immer default scheduler. Schon von anfang an in der Map
    //

    public static int deploymentCounter = 0;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane() {
    }

    public static ManagementPlane getInstance() {
        return instance;
    }

    public void buildDeploymentScheme(ArchitectureModel architectureModel) {
        final Set<Service> services = architectureModel.getServices();
        connectLoadBalancersToServices(services);
        for (Service service : services) {
            Set<Service> deploymentServices = new HashSet<>();
            deploymentServices.add(service);
            deployments.add(new Deployment(getModel(), "Deployment_" + ++deploymentCounter, false, deploymentServices, service.getStartingInstanceCount(), "firstFit"));
        }
    }

    public void applyDeploymentScheme() {

        for (Deployment deployment : deployments) {
            final DeploymentEvent deploymentEvent = new DeploymentEvent(getModel(), String.format("Starting with deployment of %s ", deployment.getQuotedName()), getModel().traceIsOn());
            deploymentEvent.schedule(deployment, getModel().presentTime());
        }
        final StartPendingPodsEvent startPendingPodsEvent = new StartPendingPodsEvent(getModel(), "Start Scheduling for pending Pods", getModel().traceIsOn());
        startPendingPodsEvent.schedule(getModel().presentTime());
    }

    public void populateSchedulerMap() {
        schedulerMap.put("firstFit", FirstFitScheduler.getInstance());
    }

    public void connectLoadBalancersToServices(Set<Service> services) {
        for (Service service : services) {
            connectLoadBalancerToService(service);
        }
    }

    public void connectLoadBalancerToService(Service service) {
        service.setLoadBalancer(new LoadBalancerOrchestration(getModel(), "RandomLoadBalancer", getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), service));
    }

    //Do this periodically
    public void checkForPendingPods() {

        if (podWaitingQueue.isEmpty()) {
            getModel().sendTraceNote("Pod Waiting Queue is empty. No need for consulting the scheduler");
            return;
        }

        LinkedList<Pod> stillUnscheduledPods = new LinkedList<>();
        while (!podWaitingQueue.isEmpty()) {
            final Pod pod = podWaitingQueue.poll();
            for (Deployment deployment : deployments) {
                if (deployment.getReplicaSet().contains(pod)) {
                    //Event Schedule Pod - Und schedule Pod ist verantwortlich die podWaitingQueue zu füllen, wenn Pod nicht geschedulet
                    final IScheduler scheduler = schedulerMap.get(deployment.getSchedulerType());
                    if (scheduler != null) {
                        if (!scheduler.schedulePod(pod)) {
                            stillUnscheduledPods.add(pod);
                        }
                    } else {
                        getModel().sendTraceNote("Unknown scheduler type: " + deployment.getSchedulerType() + ". Cannot schedule pods of " + deployment);
                        stillUnscheduledPods.add(pod);
                    }
                    break;
                }
            }
        }
        podWaitingQueue.addAll(stillUnscheduledPods);
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
