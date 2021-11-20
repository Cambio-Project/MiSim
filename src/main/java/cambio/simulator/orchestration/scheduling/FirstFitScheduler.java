package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.*;
import cambio.simulator.orchestration.environment.*;
import desmoj.core.simulator.Model;

public class FirstFitScheduler extends NamedEntity implements IScheduler {
    Cluster cluster;


    private static final FirstFitScheduler instance = new FirstFitScheduler();

    //private constructor to avoid client applications to use constructor
    private FirstFitScheduler() {
        super(ManagementPlane.getInstance().getModel(), "FirstFitScheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();
    }

    public static FirstFitScheduler getInstance() {
        return instance;
    }

    @Override
    public boolean schedulePod(Pod pod) {
        Node candidateNote = null;
        int cpuDemand = pod.getCPUDemand();
        for (Node node : cluster.getNodes()) {
            if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                candidateNote = node;
                break;
            }
        }
        if (candidateNote != null) {
            candidateNote.addPod(pod);
            pod.getContainers().forEach(container -> container.setContainerState(ContainerState.RUNNING));
            pod.getContainers().forEach(container -> container.getMicroserviceInstance().start());
            pod.setPodState(PodState.RUNNING);
            sendTraceNote(this.getQuotedName() + " has deployed " + pod.getQuotedName() + " on node " + candidateNote);
            return true;
        } else {
            sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Unsufficient resources!");
            return false;
        }
    }


}
