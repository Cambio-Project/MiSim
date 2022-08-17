package cambio.simulator.entities.networking;

import java.util.List;
import java.util.Random;
import java.util.Set;

import cambio.simulator.entities.microservice.Operation;
import desmoj.core.dist.NumericalDist;

/**
 * Represents a dependency of an {@code Operation} to other {@code Operation}s.
 * Dependencies can form a hierarchy with only the leaf dependencies being
 * actually executed.
 *
 * @author Lion Wagner, Sebastian Frank
 */
public interface DependencyDescription {
	/**
	 * Gets all subordinate dependencies.
	 * 
	 * @return all dependencies that are children of this dependency. Empty if there
	 *         is none. The returned list can be immutable.
	 */
	public List<DependencyDescription> getChildren();

	/**
	 * Gets all subordinate dependencies that are leafs in the hierarchy. Only these
	 * dependencies should be actually executed.
	 * 
	 * @return all leaf dependencies that are descendants of this dependency. Does
	 *         include this dependency if it is a leaf dependency. Empty if there is
	 *         none, but should never happen in proper hierarchy.
	 */
	public List<SimpleDependencyDescription> getLeafDescendants();

	/**
	 * Evaluates dependency and its descendants to generate a concrete order of
	 * dependency executions. The execution is randomized, thus, delivering
	 * different results after every call!
	 * 
	 * @param request the request that triggered the dependency.
	 * @param random  a generator for random numbers.
	 * @return the ordered list of executions.
	 */
	public List<ServiceDependencyInstance> generateDependenciesForExecutions(final Request request,
			final Random random);

	/**
	 * Add additional delay to all leaf descendants with the specified target.
	 *
	 * @see #getLeafDescendants()
	 * @param dist         {@link NumericalDist} of the delay.
	 * @param operationTrg target {@link Operation} of this that should be affected.
	 */
	public void applyExtraDelay(final NumericalDist<Double> dist, final Operation operationTrg);

	/**
	 * Add additional delay to all leaf descendants.
	 *
	 * @see #getLeafDescendants()
	 * @param dist {@link NumericalDist} of the delay.
	 */
	public void applyExtraDelay(final NumericalDist<Double> dist);

	/**
	 * Return all target operations of the leaf descendants.
	 * 
	 * @see #getLeafDescendants()
	 * @return all operations that serve as targets within all the dependencies.
	 */
	public Set<Operation> getAllTargetOperations();

	/**
	 * The probability of executing this dependency. For intermediate dependencies
	 * in a hierarchy, it decides whether the descendants are executed.
	 * 
	 * @return the probability which is a value between 0 and 1.
	 */
	public double getProbability();

	/**
	 * The probability of selecting this dependency for execution when being the
	 * child of an {@link AlternativeDependencyDescription}. Note that being
	 * selected is a precondition for being executed. In fact, depending on
	 * {@link #getProbability()}, the execution of this dependency (or its
	 * descendants) can still be prevented.
	 * 
	 * @return the alternative probability which is a value between 0 and 1.
	 */
	public double getAlternativeProbability();

}
