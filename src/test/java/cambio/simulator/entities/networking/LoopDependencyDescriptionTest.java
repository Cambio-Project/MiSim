package cambio.simulator.entities.networking;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.dist.ContDistConstant;

/**
 * Runs unit tests for {@link LoopDependencyDescription}.
 * 
 * @author Sebastian Frank
 *
 */
public class LoopDependencyDescriptionTest extends AbstractDependencyDescriptionTest {
	private SimpleDependencyDescription childDescription;

	/**
	 * Fills the alternative dependency with two (mocked) children. They return
	 * {@link #child1Instance} and {@link #child2Instance} when executed.
	 * 
	 * @param dependency           the dependency to fill
	 * @param operationProbability probability of child1 to be executed
	 * @return the given dependency with two more children
	 */
	private LoopDependencyDescription setupLoopDependencyWithOneChild(LoopDependencyDescription dependency,
			double operationProbability) {
		final Microservice parentMicroservice = new Microservice(model, "test", false);
		final Operation operation = new Operation(model, "child1", false, parentMicroservice, 1);
		final SimpleDependencyDescription child = spy(
				new SimpleDependencyDescription(startOperation, operation, operationProbability, 0.0, 1.0));
		this.childDescription = child;
		ServiceDependencyInstance childInstance = mock(ServiceDependencyInstance.class);
		final List<ServiceDependencyInstance> childInstances = Collections.singletonList(childInstance);
		doReturn(childInstances).when(child).generateDependenciesForExecutions(request, random);
		dependency.setDependencies(new DependencyDescription[] { child });
		return dependency;
	}

	@Test
	public void testNoChildren() {
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 1, 1, 1);
		when(random.nextDouble()).thenReturn(0.5);
		final List<ServiceDependencyInstance> result = dependency.generateDependenciesForExecutions(request, random);

		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeroIterations() {
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 1, 1, 0);
		setupLoopDependencyWithOneChild(dependency, 1);
		when(random.nextDouble()).thenReturn(0.5);
		dependency.generateDependenciesForExecutions(request, random);
	}

	@Test
	public void testOneIteration() {
		final int numberOfIterations = 1;
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 1, 1, numberOfIterations);
		setupLoopDependencyWithOneChild(dependency, 1);

		when(random.nextDouble()).thenReturn(0.4);
		final List<ServiceDependencyInstance> result = dependency.generateDependenciesForExecutions(request, random);

		assertNotNull(result);
		assertEquals(numberOfIterations, result.size());
	}

	@Test
	public void testTwoIterations() {
		final int numberOfIterations = 2;
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 1, 1, numberOfIterations);
		setupLoopDependencyWithOneChild(dependency, 1);

		when(random.nextDouble()).thenReturn(0.4);
		final List<ServiceDependencyInstance> result = dependency.generateDependenciesForExecutions(request, random);

		assertNotNull(result);
		assertEquals(numberOfIterations, result.size());
	}

	@Test
	public void testProbabilityLowerBorder() {
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 0.4, 1, 1);
		setupLoopDependencyWithOneChild(dependency, 1);

		when(random.nextDouble()).thenReturn(0.39);
		final List<ServiceDependencyInstance> result = dependency.generateDependenciesForExecutions(request, random);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	public void testProbabilityUpperBorder() {
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 0.4, 1, 1);
		setupLoopDependencyWithOneChild(dependency, 1);

		when(random.nextDouble()).thenReturn(0.40);
		final List<ServiceDependencyInstance> result = dependency.generateDependenciesForExecutions(request, random);

		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test
	public void testExtraDelayIsSet() {
		final LoopDependencyDescription dependency = new LoopDependencyDescription(model, 1, 1, 1);
		setupLoopDependencyWithOneChild(dependency, 1);
		when(random.nextDouble()).thenReturn(0.4).thenReturn(0.6);
		assertEquals(0, childDescription.getNextExtraDelay());
		dependency.applyExtraDelay(new ContDistConstant(model, "test", 1, false, false));
		assertEquals(1, childDescription.getNextExtraDelay());
	}
}
