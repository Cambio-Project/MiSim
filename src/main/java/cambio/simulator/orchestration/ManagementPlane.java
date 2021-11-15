package cambio.simulator.orchestration;


import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.orchestration.deprecated.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManagementPlane {
    List<Deployment> deployments = new ArrayList<>();
    Cluster cluster;

    private static final ManagementPlane instance = new ManagementPlane();

    //private constructor to avoid client applications to use constructor
    private ManagementPlane(){}

    public static ManagementPlane getInstance(){
        return instance;
    }

    public void buildDeploymentScheme(ArchitectureModel architectureModel){
        final Set<Service> services = architectureModel.getServices();
        for(Service service : services){
            Set<Service> deploymentServices = new HashSet<>();
            deploymentServices.add(service);
            deployments.add(new Deployment(deploymentServices, service.getStartingInstanceCount()));
        }
        connectSchedulersToDeployments();
    }

    public void applyDeploymentScheme(){
        deployments.forEach(Deployment::deploy);
    }

    public void connectSchedulersToDeployments(){
        for(Deployment deployment : this.deployments){
            connectSchedulerToDeployment(deployment);
        }
    }

    public void connectSchedulerToDeployment(Deployment deployment){
        deployment.setScheduler(new Scheduler(new FirstFitSchedulingStrategy(), this.cluster));
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
}
