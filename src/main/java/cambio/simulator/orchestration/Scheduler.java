package cambio.simulator.orchestration;

import java.util.List;

public class Scheduler {
    ISchedulingStrategy schedulingStrategy;
    Cluster cluster;

    public Scheduler(ISchedulingStrategy schedulingStrategy, Cluster cluster) {
        this.schedulingStrategy = schedulingStrategy;
        this.cluster = cluster;
    }

    public void schedulePod(Pod pod){
        schedulingStrategy.schedulePod(pod, this.cluster);
        pod.getContainers().forEach(container -> container.setContainerState(ContainerState.RUNNING));
        pod.setPodState(PodState.RUNNING);
    }
}
