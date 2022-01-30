package cambio.simulator.orchestration.environment;

import java.util.List;
import java.util.Optional;

public class Cluster {
    private List<Node> nodes;
    // Future: for cluster autoscaling
    private List<Node> extraNodes = null;

    // Future: Might include network information

    public Cluster(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Cluster(List<Node> nodes, List<Node> extraNodes ) {
        this.nodes = nodes;
        this.extraNodes = extraNodes;
    }

    public Node getNodeByName(String name){
        Optional<Node> first = nodes.stream().filter(node -> node.getName().equals(name)).findFirst();
        if(first.isPresent()){
            return first.get();
        }
        return null;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getExtraNodes() {
        return extraNodes;
    }

    public void setExtraNodes(List<Node> extraNodes) {
        this.extraNodes = extraNodes;
    }
}
