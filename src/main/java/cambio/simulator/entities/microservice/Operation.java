package cambio.simulator.entities.microservice;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.ServiceDependencyInstance;
import com.google.gson.annotations.Expose;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

/**
 * An {@code Operation} represents an endpoint of a service. It has a specific
 * computational demand and may have dependencies.
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
	 * @param operationTrg target {@link Operation} of this that should be affected,
	 *                     can be set to {@code null} to affect all outgoing
	 *                     {@link ServiceDependencyInstance}s
	 */
	public void applyExtraDelay(NumericalDist<Double> dist, Operation operationTrg) {
		if (operationTrg == null) {
			applyExtraDelayToAllDependencies(dist);
		} else {
			applyExtraDelayToSpecificDependencies(dist, operationTrg);
		}
	}

	/**
	 * Add additional delay to all dependencies.
	 * 
	 * @param dist {@link NumericalDist} of the delay.
	 */
	private void applyExtraDelayToAllDependencies(NumericalDist<Double> dist) {
		assert dist != null;
		for (DependencyDescription dependencyDescription : dependencies) {
			dependencyDescription.applyExtraDelay(dist);
		}
	}

	/**
	 * Add additional delay to the dependencies found with the given target.
	 * 
	 * @param dist         {@link NumericalDist} of the delay.
	 * @param operationTrg target {@link Operation} of this that should be affected.
	 *                     Must not be null.
	 */
	private void applyExtraDelayToSpecificDependencies(NumericalDist<Double> dist, Operation operationTrg) {
		assert operationTrg != null;
		for (DependencyDescription dependencyDescription : dependencies) {
			dependencyDescription.applyExtraDelay(dist, operationTrg);
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
