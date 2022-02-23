package cambio.simulator.orchestration.scheduling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Model;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class Scheduler extends NamedEntity implements Comparable<Scheduler> {

    Cluster cluster;
    List<Pod> podWaitingQueue;
    int PRIO = Integer.MAX_VALUE;

    public Scheduler() {
        super(ManagementPlane.getInstance().getModel(), "Scheduler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.cluster = ManagementPlane.getInstance().getCluster();
        this.podWaitingQueue = new ArrayList<>();
    }

    public abstract SchedulerType getSchedulerType();

    public abstract void schedulePods();

    public Pod getNextPodFromWaitingQueue() {
        if (!podWaitingQueue.isEmpty()) {
            Pod pod = podWaitingQueue.get(0);
            podWaitingQueue.remove(pod);
            return pod;
        }
        return null;
    }


    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<Pod> getPodWaitingQueue() {
        return podWaitingQueue;
    }

    public void setPodWaitingQueue(List<Pod> podWaitingQueue) {
        this.podWaitingQueue = podWaitingQueue;
    }

    public int getPRIO() {
        return PRIO;
    }

    public void setPRIO(int PRIO) {
        this.PRIO = PRIO;
    }

    @Override
    public int compareTo(@NotNull Scheduler scheduler) {
        return this.getPRIO() < scheduler.getPRIO() ? -1 : 1;
    }


}
