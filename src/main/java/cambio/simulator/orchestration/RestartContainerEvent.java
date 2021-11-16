package cambio.simulator.orchestration;

import cambio.simulator.misc.Priority;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class RestartContainerEvent extends Event<Pod> {

    public RestartContainerEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Pod pod) {
        pod.applyRestartPolicy();
    }
}
