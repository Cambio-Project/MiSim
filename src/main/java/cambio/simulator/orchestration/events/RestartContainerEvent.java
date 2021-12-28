package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.Pod;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class RestartContainerEvent extends Event<Container> {

    public RestartContainerEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Container container) {
            container.restartTerminatedContainer();
    }
}
