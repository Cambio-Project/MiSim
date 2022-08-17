package cambio.simulator.entities.networking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cambio.simulator.misc.Util;
import com.google.gson.annotations.Expose;
import desmoj.core.dist.DiscreteDist;
import desmoj.core.dist.DiscreteDistConstant;
import desmoj.core.simulator.Model;

/**
 * An intermediate {@link DependencyDescription} that repeats the execution of its child
 * dependencies based on a number.
 * 
 * @author Sebastian Frank
 *
 */
public class LoopDependencyDescription extends IntermediateDependencyDescription {

    @Expose
    private final DiscreteDist<Integer> iterations;

    /**
     * Creates a new loop dependency that is always executed once by default. Consider to use the
     * {@link LoopDependencyDescription#LoopDependencyDescription(Model, int) instead!}
     * 
     * @param model the MiSim model
     */
    public LoopDependencyDescription(Model model) {
        this(model, 1, 1, 1);
    }

    /**
     * Creates a new loop dependency that is always executed. Should have at least one child
     * dependency. See {@link IntermediateDependencyDescription} for setting them.
     * 
     * @param model the MiSim model
     * @param iterations the number of repetitions for the execution of this dependency's
     *        descendants.
     */
    public LoopDependencyDescription(Model model, int iterations) {
        this(model, 1, 1, iterations);
    }

    /**
     * Creates a new loop dependency. Should have at least one child dependency. See
     * {@link IntermediateDependencyDescription} for setting them.
     * 
     * @param model the MiSim model
     * @param probability probability with which the dependency - and its descendants - are
     *        executed.
     * @param alternativeProbability probability at which the dependency - and its descendants - is
     *        selected from a group of dependencies for execution when inside of an
     *        {@link AlternativeDependencyDescription}
     * @param iterations the number of repetitions for the execution of this dependency's
     *        descendants.
     */
    public LoopDependencyDescription(Model model, double probability, double alternativeProbability,
            int iterations) {
        super(model, probability, alternativeProbability);
        Util.requireGreaterZero(iterations,
                "Number of Iterations must be greater than zero, but was " + iterations);
        this.iterations = new DiscreteDistConstant<>(model, "LoopIterationDistribution", iterations,
                false, false);
    }

    @Override
    public List<ServiceDependencyInstance> generateDependenciesForExecutions(Request request,
            Random random) {
        if (isExecuted(random)) {
            return generateChildrenDependenciesForExecutions(request, random);
        }
        return Collections.emptyList();
    }

    private List<ServiceDependencyInstance> generateChildrenDependenciesForExecutions(
            Request request, Random random) {
        List<ServiceDependencyInstance> generatedDependencies = new ArrayList<>();
        final int numberOfIterations = iterations.sample();
        for (int i = 0; i < numberOfIterations; i++) {
            for (final DependencyDescription child : getChildren()) {
                generatedDependencies
                        .addAll(child.generateDependenciesForExecutions(request, random));
            }
        }
        return generatedDependencies;
    }
}
