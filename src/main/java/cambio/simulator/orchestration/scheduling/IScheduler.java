package cambio.simulator.orchestration.scheduling;

import cambio.simulator.orchestration.environment.Pod;

import java.util.LinkedList;

public interface IScheduler {

//    When Pods are created, they go to a queue and wait to be scheduled. The scheduler picks a Pod from the queue and tries to schedule it on a Node.
//    https://kubernetes.io/docs/concepts/scheduling-eviction/_print/
//Widersprüchlich zu dem Fakt, dass man mehrere Scheduler gleichzeitig verwenden kann. Wer nimmt da dann die Pods aus der Queue?
//    https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/
//    SOLUTION: ManagementPlane übernimmt verantwortung


    boolean schedulePod();

    SchedulerType getSchedulerType();

    void schedulePods();

    Pod getNextPodFromWaitingQueue();

    LinkedList<Pod> getPodWaitingQueue();

}
