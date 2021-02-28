package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.resources.Thread;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

public class MessageObject extends Entity {
    private String name;
    private DependencyGraph dependencyGraph = null;

    public MessageObject(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        dependencyGraph = new DependencyGraph();
        this.name = name;
    }

    public boolean hasDependencies() {
        return dependencyGraph.hasNodes();
    }

    public DependencyNode getDependency(Microservice service, Operation operation) {
        return dependencyGraph.getNode(service, operation);
    }

    public void addDependency(Microservice s1, Operation o1, Microservice s2, Operation o2, Thread thread) {
        dependencyGraph.insertDependency(s1, o1, s2, o2, thread);
    }

    public DependencyNode removeDependency(Microservice service, Operation operation) {
        return dependencyGraph.removeNode(service, operation);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
