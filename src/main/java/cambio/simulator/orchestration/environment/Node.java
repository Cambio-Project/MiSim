package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.InstanceState;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.events.StartPodEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.ArrayList;
import java.util.List;

public class Node extends NamedEntity {

    private static final int DEFAULT_CPU_CAPACITY = 1500;

    final int totalCPU;
    int reserved = 0;
    private List<Pod> pods;

    public Node(Model model, String name, boolean showInTrace) {
        this(model, name, showInTrace, DEFAULT_CPU_CAPACITY);
    }

    public Node(Model model, String name, boolean showInTrace, int totalCPU) {
        super(model, name, showInTrace);
        this.totalCPU = totalCPU;
        this.pods = new ArrayList<>();
    }

    public synchronized boolean addPod(Pod pod) {
        if (this.getReserved() + pod.getCPUDemand() <= this.getTotalCPU()) {
            this.reserved += pod.getCPUDemand();
            pods.add(pod);
            final StartPodEvent startPodEvent = new StartPodEvent(getModel(), "StartPodEvent", traceIsOn());
            startPodEvent.schedule(pod, presentTime());
            return true;
        }
        return false;
    }

    public void startRemoving(Pod pod){
        pod.setPodState(PodState.TERMINATING);
        pod.getContainers().forEach(container -> container.getMicroserviceInstance().startShutdown());
        final CheckPodRemovableEvent checkPodRemovableEvent = new CheckPodRemovableEvent(getModel(), "Check if pod can be removed", traceIsOn());
        checkPodRemovableEvent.schedule(pod, this, new TimeSpan(0));

    }

    public void removePod(Pod pod){
        pod.getContainers().forEach(container -> container.setContainerState(ContainerState.TERMINATED));
        pod.setPodState(PodState.SUCCEEDED);
        this.reserved -= pod.getCPUDemand();
        pods.remove(pod);
        sendTraceNote(pod.getQuotedName() + " was removed from " + this.getQuotedName());
    }


    public List<Pod> getPods() {
        return pods;
    }

    public void setPods(List<Pod> pods) {
        this.pods = pods;
    }

    public int getTotalCPU() {
        return totalCPU;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
}
