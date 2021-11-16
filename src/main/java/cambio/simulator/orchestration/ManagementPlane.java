package cambio.simulator.orchestration;


import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.deprecated.Service;
import desmoj.core.simulator.Model;

import java.util.*;

public class ManagementPlane {
    List<Deployment> deployments = new ArrayList<>();
    Cluster cluster;
    Model model;

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
            deployments.add(new Deployment(getModel(), "Deployment_" + ++deploymentCounter, false, deploymentServices, service.getStartingInstanceCount()));
        }
        connectSchedulersToDeployments();
    }

    public void applyDeploymentScheme() {

        for (Deployment deployment : deployments) {
            final DeploymentEvent deploymentEvent = new DeploymentEvent(getModel(), String.format("Starting with deployment of %s ", deployment.getQuotedName()), getModel().traceIsOn());
            deploymentEvent.schedule(deployment, getModel().presentTime());
        }

//        deployments.forEach(Deployment::deploy);
    }

    public void connectSchedulersToDeployments() {
        for (Deployment deployment : this.deployments) {
            connectSchedulerToDeployment(deployment);
        }
    }

    public void connectSchedulerToDeployment(Deployment deployment) {
        deployment.setScheduler(new Scheduler(getModel(), "StandardScheduler", getModel().traceIsOn(), new FirstFitSchedulingStrategy(), this.cluster));
    }

    public void connectLoadBalancersToServices(Set<Service> services) {
        for (Service service : services) {
            connectLoadBalancerToService(service);
        }
    }

    public void connectLoadBalancerToService(Service service) {
        service.setLoadBalancer(new LoadBalancerOrchestration(getModel(), "RandomLoadBalancer", getModel().traceIsOn(), new RandomLoadBalanceStrategyOrchestration(), service));
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
