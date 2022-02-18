package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RandomScheduler extends NamedEntity implements IScheduler {
    Cluster cluster;
    LinkedList<Pod> podWaitingQueue = new LinkedList<>();

    private static final RandomScheduler instance = new RandomScheduler();

    //private constructor to avoid client applications to use constructor
    private RandomScheduler() {
        super(ManagementPlane.getInstance().getModel(), "RandomScheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();
    }

    public static RandomScheduler getInstance() {
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
            int cpuDemand = pod.getCPUDemand();
            //Doing the same like FirstFitScheduler but nodes are shuffled.
            List<Node> nodes = new ArrayList<>(cluster.getNodes());
            Collections.shuffle(nodes);
            for (Node node : nodes) {
                if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                    candidateNote = node;
                    break;
                }
            }
            if (candidateNote != null) {
                candidateNote.addPod(pod);
                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNote);
                return true;
            } else {
                podWaitingQueue.add(pod);
                sendTraceNote(this.getQuotedName() + " was not able to schedule pod " + pod + ". Unsufficient resources!");
                sendTraceNote(this.getQuotedName() + " has send " + pod + " back to the Pod Waiting Queue");
                return false;
            }
        }
        sendTraceNote(this.getQuotedName() + " has no pods left for scheduling");
        return false;
    }

    @Override
    public SchedulerType getSchedulerType() {
        return SchedulerType.RANDOM;
    }
    @Override
    public Pod getNextPodFromWaitingQueue() {
        if (podWaitingQueue.isEmpty()) {
            return null;
        }
        return podWaitingQueue.poll();
    }
    @Override
    public LinkedList<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }
}
