package cambio.simulator.entities.microservice;

import java.util.Arrays;

import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.NetworkDependency;
import cambio.simulator.parsing.DependencyParser;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * An {@code Operation} represents an endpoint of a service. It has a specific computational demand and may have
 * dependencies.
 */
public class Operation extends Entity {
    private final int demand;
    private final Microservice ownerMS;
    private DependencyDescription[] dependencies = new DependencyDescription[0];
    //POJOs that hold the (json) data of the dependencies, used for parsing
    private DependencyParser[] dependenciesData = new DependencyParser[0];

    /**
     * Constructs a new endpoint for a microservice.
     *
     * @param ownerMS {@link Microservice} that owns this operation.
     * @param demand  CPU demand of this operation.
     */
    public Operation(Model model, String name, boolean showInTrace, Microservice ownerMS, int demand) {
        super(model, name, showInTrace);
        this.demand = demand;
        this.ownerMS = ownerMS;
    }

    public void setDependenciesData(DependencyParser[] dependenciesData) {
        this.dependenciesData = dependenciesData;
    }

    public DependencyDescription[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(DependencyDescription[] operations) {
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

    /**
     * A call of this method is needed for proper usage.<br> Parses the set {@link DependencyParser}s into {@link
     * DependencyDescription} objects.
     */
    public void initializeDependencies() {
        dependencies = new DependencyDescription[dependenciesData.length];
        for (int i = 0; i < dependenciesData.length; i++) {
            dependenciesData[i].setOwningOperation(this);
            dependencies[i] = this.dependenciesData[i].convertToObject(getModel());
        }
    }

    /**
     * Add additional delay to this operation.
     *
     * @param dist         {@link NumericalDist} of the delay.
     * @param operationTrg target {@link Operation} of this that should be affected, can be set to {@code null} to
     *                     affect all outgoing {@link NetworkDependency}s
     */
    public void applyExtraDelay(NumericalDist<Double> dist, Operation operationTrg) {
        if (operationTrg == null) {
            for (DependencyDescription dependencyDescription : dependencies) {
                dependencyDescription.setExtraDelay(dist);
            }
        } else {
            DependencyDescription targetDependency =
                Arrays.stream(dependencies).filter(dependency -> dependency.getTargetOperation() == operationTrg)
                    .findAny().orElse(null);
            if (targetDependency == null) {
                throw new IllegalStateException(String
                    .format("Operation %s is not a dependency of %s", operationTrg.getQuotedName(),
                        this.getQuotedName()));
            }
            targetDependency.setExtraDelay(dist);
        }
    }

    /**
     * Add extra delay to every dependency of this operation.
     *
     * @param dist {@link NumericalDist} of the delay.
     */
    public void applyExtraDelay(NumericalDist<Double> dist) {
        applyExtraDelay(dist, null);
    }
}
