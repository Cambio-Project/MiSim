package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.networking.MainModelAwareEntity;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.Thread;

public class MessageObject extends MainModelAwareEntity {
    private String name;
    private DependencyGraph dependencyGraph = null;

    public MessageObject(MainModel owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        dependencyGraph = new DependencyGraph();
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
