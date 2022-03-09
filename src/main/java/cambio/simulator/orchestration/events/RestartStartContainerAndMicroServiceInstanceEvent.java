package cambio.simulator.orchestration.events;

import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.management.ManagementPlane;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.extensions.experimentation.util.Run;

import java.util.stream.Collectors;

public class RestartStartContainerAndMicroServiceInstanceEvent extends Event<Container> {
    public static int counter = 0;
    public RestartStartContainerAndMicroServiceInstanceEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
        counter++;
    }

    @Override
    public void eventRoutine(Container container) {
        container.restartContainer();
        Pod podForContainer = ManagementPlane.getInstance().getPodForContainer(container);
        if(podForContainer!=null){
            if(podForContainer.getContainers().stream().filter(container1 -> container1.getContainerState().equals(ContainerState.RUNNING)).collect(Collectors.toList()).size()==1){
                podForContainer.setPodState(PodState.RUNNING);
                sendTraceNote(podForContainer.getQuotedName() + " is RUNNING again. At least one container is running");
            }
            if(podForContainer.getContainers().stream().filter(container1 -> container1.getContainerState().equals(ContainerState.RUNNING)).collect(Collectors.toList()).size()==podForContainer.getContainers().size()){
                sendTraceNote(podForContainer.getQuotedName() + " was completely restarted. All containers are RUNNING");
            }
        }
    }
}
