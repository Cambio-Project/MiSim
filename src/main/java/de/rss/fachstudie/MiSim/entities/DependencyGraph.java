package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.resources.Thread;

public class DependencyGraph {

    private DependencyNode root = null;

    public DependencyGraph(Microservice rootService, Operation rootOperation, Thread thread) {
        this.root = new DependencyNode(rootService, rootOperation, thread);
    }

    public DependencyGraph() {

    }

    public void insertDependency(Microservice s1, Operation o1, Microservice s2, Operation o2, Thread thread) {
        if (root == null) {
            root = new DependencyNode(s1, o1, thread);
        }
        insertDependency(s1, o1, s2, o2, thread, root);
    }

    private boolean insertDependency(Microservice s1, Operation o1, Microservice s2, Operation o2, Thread thread, DependencyNode node) {
        if (node.getService().getName().equals(s1.getName()) && node.getOperation().getName().equals(o1.getName())) {
            // This is the node to which the dependency should be added which has the dependency
            DependencyNode newNode = new DependencyNode(s2, o2, null);
            newNode.addDependingNode(node);
            node.addNextNode(newNode);
            node.setThread(thread);
            return true;
        } else {
            // Look in nextNodes
            for (DependencyNode nextNode : node.getNextNodes()) {
                boolean inserted = insertDependency(s1, o1, s2, o2, thread, nextNode);

                if (inserted) {
                    return true;
                }
            }
        }
        return false;
    }

    public DependencyNode getNode(Microservice service, Operation operation) {
        return findNode(service, operation, root);
    }

    private DependencyNode findNode(Microservice service, Operation operation, DependencyNode node) {
        if (node.getService().getName().equals(service.getName()) && node.getOperation().getName().equals(operation.getName())) {
            // This is the node
            return node;
        }
        for (DependencyNode nextNode : node.getNextNodes()) {
            DependencyNode targetNode = findNode(service, operation, nextNode);
            if (targetNode != null) {
                return targetNode;
            }
        }
        return null;
    }

    public DependencyNode removeNode(Microservice service, Operation operation) {
        DependencyNode node = findNode(service, operation, root);

        if (node != null) {
            for (DependencyNode dependingNode : node.getDependingNodes()) {
                for (DependencyNode nextNode : node.getNextNodes()) {
                    dependingNode.addNextNode(nextNode);
                }
                dependingNode.removeNextNode(node);
            }
        }
        return node;
    }

    public boolean hasNodes() {
        if (root.hasNextNodes()) {
            return true;
        }
        return false;
    }
}
