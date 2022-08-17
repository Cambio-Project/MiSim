package cambio.simulator.entities.networking;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.annotations.Expose;

import cambio.simulator.entities.microservice.Operation;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;

/**
 * These dependencies act (only) as intermediate dependencies inside a hierarchy
 * of {@link DependencyDescription}. They have at least one child dependency.
 * 
 * @author Sebastian Frank
 */
public abstract class IntermediateDependencyDescription extends AbstractDependencyDescription {

	@Expose
	private DependencyDescription[] dependencies = new DependencyDescription[0];

	/**
	 * Creates a new intermediate dependency. You should use {@link #getChildren()}
	 * to modify the children.
	 * 
	 * @param model                  the MiSim model
	 * @param probability            probability with which the dependency - and its
	 *                               descendants - are executed.
	 * @param alternativeProbability probability at which the dependency - and its
	 *                               descendants - is selected from a group of
	 *                               dependencies for execution when inside of an
	 *                               {@link AlternativeDependencyDescription}
	 */
	public IntermediateDependencyDescription(Model model, double probability, double alternativeProbability) {
		this(model, probability, alternativeProbability, new DependencyDescription[0]);
	}

	/**
	 * Creates a new intermediate dependency.
	 * 
	 * @param model                  the MiSim model
	 * @param probability            probability with which the dependency - and its
	 *                               descendants - are executed.
	 * @param alternativeProbability probability at which the dependency - and its
	 *                               descendants - is selected from a group of
	 *                               dependencies for execution when inside of an
	 *                               {@link AlternativeDependencyDescription}
	 * @param dependencies           the children of this intermediate dependency.
	 *                               There must be at least one.
	 */
	public IntermediateDependencyDescription(Model model, double probability, double alternativeProbability,
			DependencyDescription[] dependencies) {
		super(model, probability, alternativeProbability);
		this.dependencies = dependencies;
	}

	@Override
	public List<DependencyDescription> getChildren() {
		return Arrays.asList(dependencies);
	}

	@Override
	public void applyExtraDelay(final NumericalDist<Double> dist) {
		for (DependencyDescription dependencyDescription : dependencies) {
			dependencyDescription.applyExtraDelay(dist);
		}
	}

	@Override
	public void applyExtraDelay(final NumericalDist<Double> dist, final Operation operationTrg) {
		if (operationTrg == null) {
			throw new IllegalStateException("Target operation must not be null");
		}
		for (DependencyDescription dependencyDescription : dependencies) {
			dependencyDescription.applyExtraDelay(dist, operationTrg);
		}
	}

	@Override
	public Set<Operation> getAllTargetOperations() {
		return Stream.of(dependencies).flatMap(depdendency -> depdendency.getAllTargetOperations().stream())
				.collect(Collectors.toSet());
	}

	@Override
	public List<SimpleDependencyDescription> getLeafDescendants() {
		return Stream.of(dependencies).flatMap(depdendency -> depdendency.getLeafDescendants().stream()).toList();
	}

	/**
	 * Sets the child dependencies of this dependency.
	 * 
	 * @param dependencies should not be empty.
	 */
	protected void setDependencies(final DependencyDescription[] dependencies) {
		if (dependencies.length == 0) {
			throw new IllegalArgumentException("There must be at least one child dependency!");
		}
		this.dependencies = dependencies;
	}
}
