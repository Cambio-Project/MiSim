package cambio.simulator.orchestration;

import cambio.simulator.entities.NamedEntity;
import desmoj.core.simulator.Model;


public class Scheduler extends NamedEntity {
    ISchedulingStrategy schedulingStrategy;
    Cluster cluster;

    public Scheduler(Model model, String name, boolean showInTrace, ISchedulingStrategy schedulingStrategy, Cluster cluster) {
        super(model,name,showInTrace);
        this.schedulingStrategy = schedulingStrategy;
        this.cluster = cluster;
    }

    public void schedulePod(Pod pod){
        Node node = schedulingStrategy.getNode(pod, this.cluster);
        if(node != null){
            node.addPod(pod);
            pod.getContainers().forEach(container -> container.setContainerState(ContainerState.RUNNING));
            pod.getContainers().forEach(container -> container.getMicroserviceInstance().start());
            pod.setPodState(PodState.RUNNING);
            sendTraceNote(this.getQuotedName() + " has deployed " + pod.getQuotedName() + " on node " + node);
        } else {
            sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Unsufficient resources!");
        }
    }
}
