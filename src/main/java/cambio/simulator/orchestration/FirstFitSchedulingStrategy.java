package cambio.simulator.orchestration;

public class FirstFitSchedulingStrategy implements ISchedulingStrategy {
    @Override
    public Node getNode(Pod pod, Cluster cluster) {
        int cpuDemand = pod.getCPUDemand();
        for (Node node : cluster.getNodes()) {
            if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                //schedule pod to node - EVENT
                return node;
            }
        }
        System.out.println("Could not schedule pod because there were not enough resources");
        return null;
    }
}
