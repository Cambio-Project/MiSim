package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.environment.Pod;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class StartPodEvent extends Event<Pod> {
    public static int counter = 0;
    public StartPodEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(Pod pod) throws SuspendExecution {
        pod.startAllContainers();
    }
}
