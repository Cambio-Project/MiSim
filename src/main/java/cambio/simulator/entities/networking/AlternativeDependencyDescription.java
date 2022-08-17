package cambio.simulator.entities.networking;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import desmoj.core.simulator.Model;

/**
 * An intermediate {@link DependencyDescription} that picks one child dependency
 * based on a probability for execution.
 * 
 * @author Sebastian Frank
 *
 */
public class AlternativeDependencyDescription extends IntermediateDependencyDescription {

	/**
	 * Creates a new alternative dependency. In order to work properly, the
	 * dependency expects at least one child dependency
	 * ({@link #setDependencies(DependencyDescription[])}) with
	 * {@link AbstractDependencyDescription#getAlternativeProbability()}.
	 * 
	 * @param model                  the MiSim model
	 * @param probability            Value between 0 and 1. Probability of this
	 *                               dependency to get executed. If not executed,
	 *                               also descendants are not executed.
	 * @param alternativeProbability Value between 0 and 1. Probability of this
	 *                               dependency to get executed. when being a direct
	 *                               child of an
	 *                               {@link AlternativeDependencyDescription}.
	 */
	public AlternativeDependencyDescription(final Model model, final double probability,
			final double alternativeProbability) {
		super(model, probability, alternativeProbability);
	}

	@Override
	public List<ServiceDependencyInstance> generateDependenciesForExecutions(final Request request,
			final Random random) {
		try {
			if (isExecuted(random)) {
				DependencyDescription child = selectOneChildForExecution(random);
				return child.generateDependenciesForExecutions(request, random);
			}
		} catch (IllegalWeightException e) {
			return Collections.emptyList();
		}
		return Collections.emptyList();
	}

	private DependencyDescription selectOneChildForExecution(final Random random) {
		final double probability = random.nextDouble();
		final double totalWeight = getChildrenTotalAlternativeWeight();
		if (totalWeight == 0.0 || getChildren().isEmpty()) {
			throw new IllegalWeightException();
		}

		/*
		 * Sum up the individual (weighted) probabilities until reaching the random
		 * number.
		 */
		DependencyDescription selectedDependencyDescription = null;
		double cumulatedWeightedSelectionProbability = 0;
		for (DependencyDescription child : this.getChildren()) {
			cumulatedWeightedSelectionProbability += child.getAlternativeProbability() / totalWeight;
			if (cumulatedWeightedSelectionProbability > probability) {
				selectedDependencyDescription = child;
				break;
			}
		}
		assert selectedDependencyDescription != null;
		return selectedDependencyDescription;
	}

	/**
	 * Gets the sum of all alternative probabilities of all children.
	 * 
	 * @return usually this value is 1.0. However, if not, then the alternative
	 *         probabilities should be considered as weights.
	 */
	private double getChildrenTotalAlternativeWeight() {
		double totalWeight = 0.0;
		for (DependencyDescription child : this.getChildren()) {
			totalWeight += child.getAlternativeProbability();
		}
		return totalWeight;
	}

	/**
	 * Thrown when the children of an alternative dependency do not have proper
	 * weights, making the decision of picking one dependency impossible.
	 */
	private class IllegalWeightException extends RuntimeException {
		private static final long serialVersionUID = -2150649655216028951L;
	}

}
