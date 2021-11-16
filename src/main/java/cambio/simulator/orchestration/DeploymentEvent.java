package cambio.simulator.orchestration;

import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class DeploymentEvent extends Event<Deployment> {

    public DeploymentEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Deployment deployment) throws SuspendExecution {
        deployment.deploy();
    }
}