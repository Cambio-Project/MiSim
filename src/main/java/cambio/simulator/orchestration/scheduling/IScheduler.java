package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.environment.Pod;

import java.util.LinkedList;

public interface IScheduler {

    SchedulerType getSchedulerType();

    void schedulePods();

    Pod getNextPodFromWaitingQueue();

    LinkedList<Pod> getPodWaitingQueue();


}
