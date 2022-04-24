package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.stream.Collectors;

public class RoundRobinScheduler extends Scheduler {

    private static final RoundRobinScheduler instance = new RoundRobinScheduler();

    //private constructor to avoid client applications to use constructor
    private RoundRobinScheduler() {
        this.rename("RoundRobinScheduler");
    }

    public static RoundRobinScheduler getInstance() {
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
        String plainName = pod.getOwner().getPlainName();

        if (pod != null) {
            Node candidateNode = null;
            int candidateNodeSize = Integer.MAX_VALUE;
            int cpuDemand = pod.getCPUDemand();
            for (Node node : cluster.getNodes()) {
                int size = node.getPods().stream().filter(pod1 -> pod1.getOwner().getPlainName().equals(plainName)).collect(Collectors.toList()).size();
                if (size < candidateNodeSize) {
                    if (node.getReserved() + cpuDemand <= node.getTotalCPU()) {
                        candidateNodeSize = size;
                        candidateNode = node;
                    }
                }

            }
            if (candidateNode != null) {
                candidateNode.addPod(pod);
                sendTraceNote(this.getQuotedName() + " has scheduled " + pod.getQuotedName() + " on node " + candidateNode);
                return true;
            } else {
                podWaitingQueue.add(pod);
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
        return SchedulerType.ROUNDROBIN;
    }


}
