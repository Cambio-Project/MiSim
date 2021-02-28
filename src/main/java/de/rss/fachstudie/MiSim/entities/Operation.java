package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * An operation connects two microservice instances. During a specified time interval the service performs operations
 * and uses a portion of the microservice's computing power.
 * <p>
 * model:           reference to the experiment model name:            the given name of the operation, defined by the
 * input service:         name of the the owning microservice pattern:         resilience pattern duration:        time
 * interval the operation needs to finish CPU:             the needed computing power probability:     the operation is
 * only executed if a certain probability is reached dependencies:    an array containing dependant operations of other
 * services
 */
public class Operation extends Entity {
    private int demand = 0;
    private CircuitBreaker circuitBreaker = null;
    private Dependency[] dependencies = new Dependency[0];
    private Microservice owner = null;

    public Operation(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }


    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public boolean hasCircuitBreaker() {
        return circuitBreaker != null;
    }

    public Dependency[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(Dependency[] operations) {
        this.dependencies = operations;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @Override
    public String getQuotedName() {
        return "'" + getName() + "'";
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setOwner(Microservice owner) {
        this.owner = owner;
    }

    public Microservice getOwner() {
        return owner;
    }
}
