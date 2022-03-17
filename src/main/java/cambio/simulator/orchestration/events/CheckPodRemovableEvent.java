package cambio.simulator.orchestration.events;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.environment.Pod;
import desmoj.core.simulator.EventOf2Entities;
import desmoj.core.simulator.Model;

public class CheckPodRemovableEvent  extends EventOf2Entities<Pod, Node> {

    public CheckPodRemovableEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
//        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine(Pod pod, Node node) {
        ManagementPlane.getInstance().checkIfPodRemovableFromNode(pod, node);
    }


}
