package cambio.simulator.orchestration.events;

import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

public class FinishPodShutdown extends EventOf2Entities<Pod, Node> {
    public FinishPodShutdown(Model model, String name, boolean traceIsOn) {
        super(model, name, traceIsOn);
    }

    @Override
    public void eventRoutine(Pod pod, Node node) {
        ManagementPlane.getInstance().removePodFromNode(pod, node);
    }
}
