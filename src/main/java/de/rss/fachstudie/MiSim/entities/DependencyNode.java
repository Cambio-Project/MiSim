package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.resources.Thread;

import java.util.ArrayList;
import java.util.List;

public class DependencyNode {

    private Microservice service;
    private Operation operation;
    private Thread thread;

    private List<DependencyNode> nextNodes;
    private List<DependencyNode> dependingNodes;

    public DependencyNode(Microservice service, Operation operation, Thread thread) {
        this.service = service;
        this.operation = operation;
        this.thread = thread;
        nextNodes = new ArrayList<DependencyNode>();
        dependingNodes = new ArrayList<DependencyNode>();
    }

    public void addNextNode(DependencyNode node) {
        nextNodes.add(node);
    }

    public void addDependingNode(DependencyNode node) {
        dependingNodes.add(node);
    }

    public boolean hasNextNodes() {
        if (nextNodes.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean hasDependingNodes() {
        if (dependingNodes.isEmpty()) {
            return false;
        }
        return true;
    }

    public DependencyNode removeNextNode() {
        if (!nextNodes.isEmpty()) {
            return nextNodes.remove(nextNodes.size() - 1);
        }
        return null;
    }

    public DependencyNode removeDependingNode() {
        if (!dependingNodes.isEmpty()) {
            return dependingNodes.remove(dependingNodes.size() - 1);
        }
        return null;
    }

    public void removeNextNode(DependencyNode node) {
        if (!nextNodes.isEmpty()) {
            nextNodes.remove(node);
        }
    }

    public void removeDependingNode(DependencyNode node) {
        if (!dependingNodes.isEmpty()) {
            dependingNodes.remove(node);
        }
    }

    public void emptyNextNodes() {
        if (!nextNodes.isEmpty()) {
            nextNodes = new ArrayList<DependencyNode>();
        }
    }

    public Microservice getService() {
        return service;
    }

    public void setService(Microservice service) {
        this.service = service;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public List<DependencyNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<DependencyNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public List<DependencyNode> getDependingNodes() {
        return dependingNodes;
    }

    public void setDependingNodes(List<DependencyNode> dependingNodes) {
        this.dependingNodes = dependingNodes;
    }

    public boolean equals(DependencyNode node) {
        String s1 = this.getService().getName();
        String o1 = this.getOperation().getName();
        String s2 = node.getService().getName();
        String o2 = node.getService().getName();

        if (s1.equals(s2) && o1.equals(o2)) {
            return true;
        }

        return false;
    }
}
