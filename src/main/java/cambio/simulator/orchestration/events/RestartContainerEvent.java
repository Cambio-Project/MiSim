package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.environment.Pod;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

public class RestartContainerEvent extends Event<Deployment> {

    public RestartContainerEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Deployment deployment) {
        for (Pod pod : deployment.getReplicaSet()) {
            pod.applyRestartPolicy();
        }
    }
}
