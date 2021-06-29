package de.rss.fachstudie.MiSim.entities.microservice;

import de.rss.fachstudie.MiSim.entities.networking.Dependency;
import de.rss.fachstudie.MiSim.parsing.DependencyParser;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

import java.util.Arrays;

/**
 * An {@code Operation} represents an endpoint of a service. It has a specific computational demand and may have
 * dependencies.
 */
public class Operation extends Entity {
    private final int demand;
    private Dependency[] dependencies = new Dependency[0];
    private final Microservice ownerMS;
    private DependencyParser[] dependenciesData = new DependencyParser[0]; //POJOs that hold the (json) data of the dependencies, used for parsing

    public Operation(Model model, String name, boolean showInTrace, Microservice ownerMS, int demand) {
        super(model, name, showInTrace);
        this.demand = demand;
        this.ownerMS = ownerMS;
    }

    public void setDependenciesData(DependencyParser[] dependenciesData) {
        this.dependenciesData = dependenciesData;
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

    public Microservice getOwnerMS() {
        return ownerMS;
    }

    @Override
    public String getQuotedName() {
        return "'" + getName() + "'";
    }

    @Override
    public String toString() {
        return getName();
    }


    public void initializeDependencies() {
        dependencies = new Dependency[dependenciesData.length];
        for (int i = 0; i < dependenciesData.length; i++) {
            dependenciesData[i].setOwningOperation(this);
            dependencies[i] = this.dependenciesData[i].convertToObject(getModel());
        }
    }

    public void applyExtraDelay(NumericalDist<Double> dist, Operation operation_trg) {
        if (operation_trg == null) {
            for (Dependency dependency : dependencies) {
                dependency.setExtraDelay(dist);
            }
        } else {
            Dependency target_dep = Arrays.stream(dependencies).filter(dependency -> dependency.getTargetOperation() == operation_trg).findAny().orElse(null);
            if (target_dep == null) {
                throw new IllegalStateException(String.format("Operation %s is not a dependency of %s", operation_trg.getQuotedName(), this.getQuotedName()));
            }
            target_dep.setExtraDelay(dist);
        }
    }

    public void applyExtraDelay(NumericalDist<Double> dist) {
        applyExtraDelay(dist, null);
    }
}
