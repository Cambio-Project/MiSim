package cambio.simulator.entities.networking;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Random;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.TestBase;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.BeforeEach;

/**
 * A base class that provides basic mocks for setting up unit tests on
 * {@link DependencyDescription} implementations.
 *
 * @author Sebastian Frank
 */
public abstract class AbstractDependencyDescriptionTest extends TestBase {
    protected Random random;
    protected Model model;
    protected Operation startOperation;
    protected Request request;

    @BeforeEach
    public void setup() {
        this.random = mock(Random.class);

        // Setup mocked model
        this.model = getConnectedMockModel().getValue0();

        // setup mocked start operation
        final Microservice parentMicroservice = new Microservice(model, "test", false);
        this.startOperation = spy(new Operation(model, "test", false, parentMicroservice, 1));
        when(startOperation.getModel()).thenReturn(model);

        // setup mocked request
        this.request = mock(Request.class);
        when(request.getModel()).thenReturn(model);
    }

}
