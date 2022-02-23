package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.events.CheckPodRemovableEvent;
import cambio.simulator.orchestration.events.StartPodEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.ArrayList;
import java.util.List;

public class Node extends NamedEntity {

    private static final String BASE_IP_ADDRESS = "192.168.49.";
    private String nodeIpAddress;
    private static int IP_ADDRESS_COUNTER = 1;

    private final int totalCPU;
    private int reserved = 0;
    private List<Pod> pods;

    public Node(Model model, String name, boolean showInTrace, int totalCPU) {
        super(model, name, showInTrace);
        this.totalCPU = totalCPU;
        this.pods = new ArrayList<>();
        this.nodeIpAddress = this.BASE_IP_ADDRESS + this.IP_ADDRESS_COUNTER++;
    }

    public synchronized boolean addPod(Pod pod) {
        if (this.getReserved() + pod.getCPUDemand() <= this.getTotalCPU()) {
            this.reserved += pod.getCPUDemand();
            pods.add(pod);
            final StartPodEvent startPodEvent = new StartPodEvent(getModel(), "StartPodEvent", traceIsOn());
            startPodEvent.schedule(pod, presentTime());
            pod.setLastKnownNode(this);
            return true;
        }
        return false;
    }

    public void startRemovingPod(Pod pod){
        pod.setPodStateAndApplyEffects(PodState.TERMINATING);
        final CheckPodRemovableEvent checkPodRemovableEvent = new CheckPodRemovableEvent(getModel(), "Check if pod can be removed", traceIsOn());
        checkPodRemovableEvent.schedule(pod, this, new TimeSpan(0));
    }

    public void removePod(Pod pod){
        pod.setPodStateAndApplyEffects(PodState.SUCCEEDED);
        this.reserved -= pod.getCPUDemand();
        if (pods.remove(pod)) {
            sendTraceNote(pod.getQuotedName() + " was removed from " + this.getQuotedName());
        } else {
            throw new IllegalArgumentException("Pod " + pod.getQuotedPlainName() + " does not belong to this node");
        }
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

    public String getNodeIpAddress() {
        return nodeIpAddress;
    }
}
