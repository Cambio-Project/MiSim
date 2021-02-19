package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.Operation;

/**
 * @author Lion Wagner
 */
public class NetworkDependency {

    private final Request parent_request;
    private final Microservice target_ms;
    private final Operation target_op;
    private boolean completed;
    private Request child_request;

    public NetworkDependency(Request parent_request, Operation target_op) {
        this.parent_request = parent_request;
        this.target_op = target_op;
        this.target_ms = target_op.getOwner();
    }


    public Request getParent_request() {
        return parent_request;
    }

    public Microservice getTarget_ms() {
        return target_ms;
    }

    public Operation getTarget_op() {
        return target_op;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public Request getChild_request() {
        return child_request;
    }

    public void updateChild_request(Request child_request) {
        this.child_request = child_request;
        //TODO: log and check for child updates (e.g previous child request failed)
    }
}
