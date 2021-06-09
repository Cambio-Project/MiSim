package de.rss.fachstudie.MiSim.entities.networking;

import java.util.Objects;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

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

    private final Request parentRequest;
    private final Microservice targetMicroservice;
    private final Operation targetOp;
    private final Dependency dependencyData;
    private boolean completed;
    private Request childRequest;

    public NetworkDependency(Model model, Request parentRequest, Operation targetOp, Dependency dependencyData) {
        super(model, String.format("Dependency(%s)of[%s]", targetOp.getName(), parentRequest.getName()), false);
        this.parentRequest = parentRequest;
        this.targetOp = targetOp;
        this.targetMicroservice = targetOp.getOwnerMS();
        this.dependencyData = dependencyData;
    }


    public Request getParentRequest() {
        return parentRequest;
    }

    public Microservice getTarget_Service() {
        return targetMicroservice;
    }

    public Operation getTargetOp() {
        return targetOp;
    }

    public boolean isCompleted() {
        return completed;
    }

    void setCompleted() {
        this.completed = true;
    }

    public Request getChildRequest() {
        return childRequest;
    }

    /**
     * This method is used to overwrite the child request that was used to try to complete this dependency. For example,
     * if a retry creates a new request because the previous one timed out, it has to notify (update) the {@code
     * NetworkDependency} that a new child request for this dependency was created.
     *
     * @param childRequest new child request that overwrites the current one
     */
    public void updateChild_request(Request childRequest) {
        this.childRequest = childRequest;
        //TODO: log
    }

    public double getNextExtraDelay() {
        return dependencyData.getNextExtraDelay();
    }

    public boolean hasCustomDelay() {
        return dependencyData.hasCustomDelay();
    }

    public double getNextCustomDelay() {
        return dependencyData.getNextCustomDelay();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        NetworkDependency that = (NetworkDependency) other;
        return parentRequest.equals(that.parentRequest) && targetOp.equals(that.targetOp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentRequest, targetOp);
    }
}
