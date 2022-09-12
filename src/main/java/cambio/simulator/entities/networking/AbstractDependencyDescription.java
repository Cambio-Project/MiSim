package cambio.simulator.entities.networking;

import java.util.Objects;
import java.util.Random;

import cambio.simulator.misc.Util;
import com.google.gson.annotations.Expose;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.Model;

/**
 * An abstract implementation of {@link DependencyDescription} taking care of the probabilities and
 * alternative probabilities.
 *
 * @author Sebastian Frank
 */
public abstract class AbstractDependencyDescription implements DependencyDescription {

    @Expose
    private final ContDistNormal probability;

    @Expose
    private final ContDistNormal alternativeProbability;

    /**
     * Constructs a new Dependency object.
     *
     * @param model       the MiSim model
     * @param probability probability with which the dependency - and its descendants - are
     *                    executed.
     */
    public AbstractDependencyDescription(final Model model, final double probability) {
        this(model, probability, 1);
    }

    /**
     * Constructs a new Dependency object.
     *
     * @param model                  the MiSim model
     * @param probability            probability with which the dependency - and its descendants - are
     *                               executed.
     * @param alternativeProbability probability at which the dependency - and its descendants - is
     *                               selected from a group of dependencies for execution when inside of an
     *                               {@link AlternativeDependencyDescription}
     */
    public AbstractDependencyDescription(final Model model, final double probability,
                                         final double alternativeProbability) {
        super();
        Objects.requireNonNull(model, "Model must not be null!");
        Util.requirePercentage(probability, "Probability hast to be a value between 0 and 1 (inclusive)");
        Util.requirePercentage(alternativeProbability, "Probability has to be a value between 0 and 1 (inclusive)");
        this.alternativeProbability =
            new ContDistNormal(model, "DependencyAlternativeDistribution", alternativeProbability, 0, false, false);
        this.probability = new ContDistNormal(model, "DependencyDistribution", probability, 0, false, false);
    }

    @Override
    public double getProbability() {
        return probability.sample();
    }

    @Override
    public double getAlternativeProbability() {
        return alternativeProbability.sample();
    }

    /**
     * Determines whether this dependency gets executed based on the {@link #probability}. Note that
     * this operation can provide different results when calling it multiple times since it is
     * random.
     *
     * @param random A random number generator.
     * @return true if it gets executed.
     */
    protected boolean isExecuted(final Random random) {
        double probability = getProbability();
        double sample = random.nextDouble();
        return sample < probability;
    }

}
