package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.orchestration.deprecated.Service;

public class FirstFitSchedulingStrategy implements ISchedulingStrategy{
    @Override
    public void schedulePod(Pod pod, Cluster cluster) {
        int cpuDemand = 0;
        for(Container container : pod.getContainers()){
            final Service microservice = (Service) container.getMicroserviceInstance().getOwner();
            cpuDemand += microservice.getCapacity();
        }

        for(Node node : cluster.getNodes()){
            if(node.getReserved() + cpuDemand <= node.getTotalCPU()){
                //schedule pod to node - EVENT
                node.addPod(pod, cpuDemand);
                return;
            }
        }
        System.out.println("Could not schedule pod because there were not enough resources");
    }
}
