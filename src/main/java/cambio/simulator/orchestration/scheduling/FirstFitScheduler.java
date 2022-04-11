package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.environment.*;
import cambio.simulator.orchestration.management.ManagementPlane;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class FirstFitScheduler extends Scheduler {

    private static final FirstFitScheduler instance = new FirstFitScheduler();

    //private constructor to avoid client applications to use constructor
    private FirstFitScheduler() {
        this.rename("FirstFitScheduler");
    }

    public static FirstFitScheduler getInstance() {
        return instance;
    }

    @Override
    public void schedulePods() {
        if (podWaitingQueue.isEmpty()) {
            getModel().sendTraceNote(this.getQuotedName() + " 's Waiting Queue is empty.");
            return;
        }
        int i = 0;
        final int podWaitingQueueInitSize = podWaitingQueue.size();
        while (i < podWaitingQueueInitSize) {
            i++;
            schedulePod();
        }
    }

    public boolean schedulePod() {

        final Pod pod = getNextPodFromWaitingQueue();

        if (pod != null) {
            Node candidateNote = null;
            double cpuDemand = pod.getCPUDemand();
            for (Node node : cluster.getNodes()) {
                if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                    candidateNote = node;
                    break;
                }
            }
            if (candidateNote != null) {
                candidateNote.addPod(pod);

                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(pod.getName());
                record.setNodeName(candidateNote.getPlainName());
                record.setScheduler("firstFit");
                record.setEvent("Binding");
                record.setOutcome("Success");
                record.setInfo("N/A");
                record.setDesiredState(ManagementPlane.getInstance().getDeploymentForPod(pod).getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(ManagementPlane.getInstance().getDeploymentForPod(pod)));
                Stats.getInstance().getNodePodEventRecords().add(record);

                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNote);
                return true;
            } else {
                podWaitingQueue.add(pod);


                //only for reporting
                Stats.NodePodEventRecord record = new Stats.NodePodEventRecord();
                record.setTime((int) presentTime().getTimeAsDouble());
                record.setPodName(pod.getName());
                record.setNodeName("N/A");
                record.setScheduler("firstFit");
                record.setEvent("Binding");
                record.setOutcome("Failed");
                record.setDesiredState(ManagementPlane.getInstance().getDeploymentForPod(pod).getDesiredReplicaCount());
                record.setCurrentState(ManagementPlane.getInstance().getAmountOfPodsOnNodes(ManagementPlane.getInstance().getDeploymentForPod(pod)));
                record.setInfo("Insufficient resources");
                Stats.getInstance().getNodePodEventRecords().add(record);

                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Insufficient resources!");
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
                return false;
            }
        }
        sendTraceNote(this.getQuotedName() + " has no pods left for scheduling");
        return false;
    }

    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.FIRSTFIT;
    }


}
