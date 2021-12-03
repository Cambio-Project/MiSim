package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.management.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

public class PeriodicTasksEvent extends NamedExternalEvent {


    public PeriodicTasksEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        ManagementPlane.getInstance().checkForScaling();
        ManagementPlane.getInstance().checkForPendingPods();
    }
}
