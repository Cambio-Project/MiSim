package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class TryToRestartContainerEvent extends Event<Container> {
    public static int counter = 0;

    public TryToRestartContainerEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(Container container) {

        if (container.getContainerState().equals(ContainerState.TERMINATED)) {
            container.incrementBackOffDelay();
            container.setLastRetry(presentTime());
            if (container.canRestartOtherwiseDecrease()) {
                RestartStartContainerAndMicroServiceInstanceEvent restartStartContainerAndMicroServiceInstanceEvent = new RestartStartContainerAndMicroServiceInstanceEvent(getModel(), "TryToRestartContainerEvent", traceIsOn());
                restartStartContainerAndMicroServiceInstanceEvent.schedule(container, presentTime());
            } else {
                container.restart();
            }
        } else {
            sendTraceNote("No need to restart " + container.getQuotedPlainName() + " because it is not terminated");
        }
    }
}

