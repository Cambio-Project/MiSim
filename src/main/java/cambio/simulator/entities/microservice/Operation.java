package cambio.simulator.entities.microservice;

import java.util.Arrays;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.NetworkDependency;
import com.google.gson.annotations.Expose;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

/**
 * An {@code Operation} represents an endpoint of a service. It has a specific computational demand and may have
 * dependencies.
 */
public class Operation extends NamedEntity {

    private final transient Microservice ownerMS;

    @Expose
    private int demand;

    @Expose
    private DependencyDescription[] dependencies = new DependencyDescription[0];

    /**
     * Constructs a new endpoint for a microservice.
     *
     * @param ownerMS {@link Microservice} that owns this operation.
     * @param demand  CPU demand of this operation.
     */
    public Operation(Model model, String name, boolean showInTrace, Microservice ownerMS, int demand) {
        super(model, (ownerMS == null ? "" : ownerMS.getPlainName() + ".") + name, showInTrace);
        this.demand = demand;
        this.ownerMS = ownerMS;
    }

    public DependencyDescription[] getDependencyDescriptions() {
        return dependencies;
    }

    public int getDemand() {
        return demand;
    }

    public Microservice getOwnerMS() {
        return ownerMS;
    }

    @Override
    public String getQuotedName() {
        return "'" + getPlainName() + "'";
    }

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }

    public String getFullyQualifiedName() {
        return ownerMS.getPlainName() + "." + getName();
    }

    public String getFullyQualifiedPlainName() {
        return ownerMS.getPlainName() + "." + getPlainName();
    }

    public String getQuotedFullyQualifiedName() {
        return "'" + getFullyQualifiedName() + "'";
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
