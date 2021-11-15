package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.resources.cpu.CPU;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private static final int DEFAULT_CPU_CAPACITY = 400;

    final int totalCPU;
    int reserved = 0;
    private List<Pod> pods;

    public Node(){
        this(DEFAULT_CPU_CAPACITY);
    }

    public Node(int totalCPU) {
        this.totalCPU= totalCPU;
        this.pods = new ArrayList<>();
    }

    public void addPod(Pod pod, int cpuDemand){
        this.reserved += cpuDemand;
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
