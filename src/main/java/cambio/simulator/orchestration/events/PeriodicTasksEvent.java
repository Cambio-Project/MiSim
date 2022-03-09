package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.Stats;
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
        //changes desired state
        ManagementPlane.getInstance().checkForScaling();
        //check if current state equals desired state and take actions (create or delete pods)
        ManagementPlane.getInstance().maintainDeployments();
        //go to scheduler and assign new created or pending pods to nodes
        ManagementPlane.getInstance().checkForPendingPods();
    }
}
