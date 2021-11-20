package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.orchestration.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

public class StartPendingPodsEvent extends NamedExternalEvent {


    public StartPendingPodsEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        ManagementPlane.getInstance().checkForPendingPods();
    }
}
