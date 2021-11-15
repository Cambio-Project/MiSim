package cambio.simulator.orchestration;

import java.util.List;

public class Cluster {
    private List<Node> nodes;
    private List<Node> extraNodes = null;

    public Cluster(List<Node> nodes) {
        this.nodes = nodes;
    }

    public Cluster(List<Node> nodes, List<Node> extraNodes ) {
        this.nodes = nodes;
        this.extraNodes = extraNodes;
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
