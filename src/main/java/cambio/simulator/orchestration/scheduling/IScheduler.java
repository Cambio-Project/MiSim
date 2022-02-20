package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.environment.Pod;

import java.util.Collection;

public interface IScheduler {

    SchedulerType getSchedulerType();

    void schedulePods();

    Pod getNextPodFromWaitingQueue();

    Collection<Pod> getPodWaitingQueue();

    int getPrio();

    void setPrio(int value);


}
