package cambio.simulator.entities.networking;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Before;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;

/**
 * A base class that provides basic mocks for setting up unit tests on
 * {@link DependencyDescription} implementations.
 *
 * @author Sebastian Frank
 */
public abstract class AbstractDependencyDescriptionTest {
    protected Random random;
    protected Model model;
    protected Operation startOperation;
    protected Request request;

    @Before
    public void setup() {
        this.random = mock(Random.class);

        // Setup mocked model
        this.model = mock(Model.class);
        Experiment experiment = new Experiment("Test");
        when(model.getExperiment()).thenReturn(experiment);

        // setup mocked start operation
        final Microservice parentMicroservice = new Microservice(model, "test", false);
        this.startOperation = spy(new Operation(model, "test", false, parentMicroservice, 1));
        when(startOperation.getModel()).thenReturn(model);

        // setup mocked request
        this.request = mock(Request.class);
        when(request.getModel()).thenReturn(model);
    }

}
