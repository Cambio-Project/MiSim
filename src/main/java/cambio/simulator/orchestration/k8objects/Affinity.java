package cambio.simulator.orchestration.k8objects;

import java.util.HashSet;
import java.util.Set;

public class Affinity {
    private String key;
    private Set<String> nodeAffinities;

    public Affinity(){
        nodeAffinities = new HashSet<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Set<String> getNodeAffinities() {
        return nodeAffinities;
    }

    public void setNodeAffinities(Set<String> nodeAffinities) {
        this.nodeAffinities = nodeAffinities;
    }
}
