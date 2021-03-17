package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.parsing.DependencyParser;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
    private DependencyParser[] dependenciesData = new DependencyParser[0]; //POJOs that hold the (json) data of the dependencies, used for parsing

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

    public void setDependenciesData(DependencyParser[] dependenciesData) {
        this.dependenciesData = dependenciesData;
    }

    public void initializeDependencies(List<Microservice> services) {
        dependencies = new Dependency[dependenciesData.length];
        for (int i = 0; i < dependenciesData.length; i++) {
            dependenciesData[i].setOwningOperation(this);
            dependencies[i] = this.dependenciesData[i].convertToObject(getModel(), new HashSet<>(services));
        }
    }

    public void applyDelay(NumericalDist<Double> dist, Operation operation_trg) {
        if (operation_trg == null) {
            for (Dependency dependency : dependencies) {
                dependency.setDelay(dist);
            }
        } else {
            Dependency target_dep = Arrays.stream(dependencies).filter(dependency -> dependency.getTargetOperation() == operation_trg).findAny().orElse(null);
            if (target_dep == null) {
                throw new IllegalStateException(String.format("Operation %s is not a dependency of %s", operation_trg.getQuotedName(), this.getQuotedName()));
            }
            target_dep.setDelay(dist);
        }
    }

    public void applyDelay(NumericalDist<Double> dist) {
        applyDelay(dist, null);
    }
}
