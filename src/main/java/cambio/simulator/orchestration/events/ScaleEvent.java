package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.management.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

public class ScaleEvent extends NamedExternalEvent {
    public static int counter = 0;

    public ScaleEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        //changes desired state
        ManagementPlane.getInstance().checkForScaling();
        HealthCheckEvent healthCheckEvent = new HealthCheckEvent(getModel(), "HealthCheckEvent - After Scaling", traceIsOn());
        healthCheckEvent.schedule(new TimeSpan(HealthCheckEvent.delay));
    }
}
