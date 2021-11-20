package cambio.simulator.orchestration.environment;

import cambio.simulator.entities.NamedEntity;
import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;

public class Node extends NamedEntity {

    private static final int DEFAULT_CPU_CAPACITY = 400;

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

    public void addPod(Pod pod) {
        this.reserved += pod.getCPUDemand();
        pods.add(pod);

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
