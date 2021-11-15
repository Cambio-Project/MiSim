package cambio.simulator.orchestration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pod {
    private PodState podState;

    private Set<Container> containers;

    public Pod(){
        this.containers = new HashSet<>();
        this.podState = PodState.PENDING;
    }

    public Set<Container> getContainers() {
        return containers;
    }

    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    public PodState getPodState() {
        return podState;
    }

    public void setPodState(PodState podState) {
        this.podState = podState;
    }


}
