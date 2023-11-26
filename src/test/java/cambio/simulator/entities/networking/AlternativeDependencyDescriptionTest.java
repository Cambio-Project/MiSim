package cambio.simulator.entities.networking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;


import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.dist.ContDistConstant;
import org.junit.jupiter.api.Test;

/**
 * Runs unit tests for {@link AlternativeDependencyDescription}.
 *
 * @author Sebastian Frank
 */
public class AlternativeDependencyDescriptionTest extends AbstractDependencyDescriptionTest {
    private ServiceDependencyInstance child1Instance;
    private ServiceDependencyInstance child2Instance;
    private SimpleDependencyDescription child1Description;
    private SimpleDependencyDescription child2Description;

    /**
     * Fills the alternative dependency with two (mocked) children. They return
     * {@link #child1Instance} and {@link #child2Instance} when executed.
     *
     * @param dependency                       the dependency to fill
     * @param operation1Probability            probability of child1 to be executed
     * @param operation1AlternativeProbability probability of child1 to get picked
     *                                         by the alternative
     * @param operation2Probability            probability of child2 to be executed
     * @param operation2AlternativeProbability probability of child2 to be picked by
     *                                         the alternative
     * @return the given dependency with two more children
     */
    private AlternativeDependencyDescription setupAlternativeDependencyWithTwoChildren(
        AlternativeDependencyDescription dependency, double operation1Probability,
        double operation1AlternativeProbability, double operation2Probability,
        double operation2AlternativeProbability) {
        final Microservice parentMicroservice = new Microservice(model, "test", false);
        final Operation operation1 = new Operation(model, "child1", false, parentMicroservice, 1);
        final Operation operation2 = new Operation(model, "child2", false, parentMicroservice, 1);
        final SimpleDependencyDescription child1 =
            spy(new SimpleDependencyDescription(startOperation, operation1, operation1Probability, 0.0,
                operation1AlternativeProbability));
        final SimpleDependencyDescription child2 =
            spy(new SimpleDependencyDescription(startOperation, operation2, operation2Probability, 0.0,
                operation2AlternativeProbability));
        this.child1Description = child1;
        this.child2Description = child2;
        this.child1Instance = mock(ServiceDependencyInstance.class);
        this.child2Instance = mock(ServiceDependencyInstance.class);
        final List<ServiceDependencyInstance> child1Instances = Collections.singletonList(child1Instance);
        final List<ServiceDependencyInstance> child2Instances = Collections.singletonList(child2Instance);
        doReturn(child1Instances).when(child1).generateDependenciesForExecutions(request, random);
        doReturn(child2Instances).when(child2).generateDependenciesForExecutions(request, random);
        dependency.setDependencies(new DependencyDescription[] {child1, child2});
        return dependency;
    }

    @Test
    public void testTakeAlternative1Normal() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.0);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child1Instance);
    }

    @Test
    public void testTakeAlternative2Normal() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.99999);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child2Instance);
    }

    @Test
    public void testTakeAlternative1AtBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.49);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child1Instance);
    }

    @Test
    public void testTakeAlternative2AtBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.50);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child2Instance);
    }

    @Test
    public void testTakeAlternative1AtWeightedBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.125, 1, 0.375);

        when(random.nextDouble()).thenReturn(0.24);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child1Instance);
    }

    @Test
    public void testTakeAlternative2AtWeightedBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.125, 1, 0.375);

        when(random.nextDouble()).thenReturn(0.25);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertEquals(1, result.size());
        assertEquals(result.get(0), child2Instance);
    }

    @Test
    public void testNoAlternative() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);

        when(random.nextDouble()).thenReturn(0.0);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testZeroWeight() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.0, 1, 0.0);

        when(random.nextDouble()).thenReturn(0.0);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testProbabilityLowerBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 0.4, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.39);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testProbabilityUpperBorder() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 0.4, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);

        when(random.nextDouble()).thenReturn(0.4);
        final List<ServiceDependencyInstance> result = dep.generateDependenciesForExecutions(request, random);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testExtraDelayIsSet() {
        final AlternativeDependencyDescription dep = new AlternativeDependencyDescription(model, 1, 1);
        setupAlternativeDependencyWithTwoChildren(dep, 1, 0.5, 1, 0.5);
        when(random.nextDouble()).thenReturn(0.4).thenReturn(0.6);
        assertEquals(0, child1Description.getNextExtraDelay());
        assertEquals(0, child2Description.getNextExtraDelay());
        dep.applyExtraDelay(new ContDistConstant(model, "test", 1, false, false));
        assertEquals(1, child1Description.getNextExtraDelay());
        assertEquals(1, child2Description.getNextExtraDelay());
    }

}
