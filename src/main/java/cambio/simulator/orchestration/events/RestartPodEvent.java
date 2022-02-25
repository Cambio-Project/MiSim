package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.environment.Pod;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

//TODO Existiert eigentlich gar nicht https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/ -Garbage collection of failed Pods
public class RestartPodEvent extends Event<Pod> {

    public RestartPodEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Pod pod) throws SuspendExecution {
//        pod.restartAllContainers();
    }
}
