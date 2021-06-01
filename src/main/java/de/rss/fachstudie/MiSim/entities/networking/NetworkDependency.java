package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.Objects;

/**
 * This is an instance of a {@link Dependency}. It describes an actual existing dependency of a {@code Request}, that
 * arrived at a {@code MicroserviceInstance}.
 *
 * @author Lion Wagner
 * @see Dependency
 * @see Request
 * @see de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance
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
        this.target_ms = target_op.getOwnerMS();
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

    /**
     * This method is used to overwrite the child request that was used to try to complete this dependency. For example,
     * if a retry creates a new request because the previous one timed out, it has to notify (update) the {@code
     * NetworkDependency} that a new child request for this dependency was created.
     *
     * @param child_request new child request that overwrites the current one
     */
    public void updateChild_request(Request child_request) {
        this.child_request = child_request;
        //TODO: log
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkDependency that = (NetworkDependency) o;
        return parent_request.equals(that.parent_request) && target_op.equals(that.target_op);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent_request, target_op);
    }
}
