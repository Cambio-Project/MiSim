package de.rss.fachstudie.MiSim.entities;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.Stack;

public class MessageObject extends Entity {
    private String name;
    private Stack<Predecessor> dependency;

    public MessageObject(Model owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
        dependency = new Stack<>();
    }

    public Stack<Predecessor> getDependency() {
        return dependency;
    }

    public void addDependency(Predecessor dependency) {
        this.dependency.push(dependency);
    }

    public Predecessor removeDependency() {
        return this.dependency.pop();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void killDependencies() {
        while (!dependency.isEmpty()) {
            Predecessor pre = dependency.pop();
            pre.getStopEvent().schedule(pre.getEntity(), pre.getThread(), this);
        }
    }
}
