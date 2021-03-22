package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class NetworkDependency extends Entity {

    private final Request parent_request;
    private final Microservice target_ms;
    private final Operation target_op;
    private final Dependency dependency_data;
    private boolean completed;
    private Request child_request;

    public NetworkDependency(Model model, Request parent_request, Operation target_op, Dependency dependency_data) {
        super(model, String.format("Dependency(%s)of[%s]", target_op.getName(), parent_request.getName()), false);
        this.parent_request = parent_request;
        this.target_op = target_op;
        this.target_ms = target_op.getOwner();
        this.dependency_data = dependency_data;
    }


    public Request getParent_request() {
        return parent_request;
    }

    public Microservice getTarget_Service() {
        return target_ms;
    }

    public Operation getTarget_op() {
        return target_op;
    }

    public boolean isCompleted() {
        return completed;
    }

    void setCompleted() {
        this.completed = true;
    }

    public Request getChild_request() {
        return child_request;
    }

    public void updateChild_request(Request child_request) {
        this.child_request = child_request;
        //TODO: log and check for child updates (e.g previous child request failed)
    }

    public double getNextExtraDelay() {
        return dependency_data.getNextExtraDelay();
    }

    public boolean hasCustomDelay() {
        return dependency_data.hasCustomDelay();
    }

    public double getNextCustomDelay() {
        return dependency_data.getNextCustomDelay();
    }
}
