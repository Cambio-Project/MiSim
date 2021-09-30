package cambio.simulator.entities.patterns;

import java.lang.reflect.Field;
import java.util.Set;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.testutils.RandomTieredModel;
import cambio.simulator.testutils.TestExperiment;
import cambio.simulator.testutils.TestUtils;
import desmoj.core.simulator.Experiment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    @Test
    void test_has_lower_priority_then_retry() throws IllegalAccessException, NoSuchFieldException {
        RandomTieredModel mockModel = new RandomTieredModel("", 2, 2);
        TestExperiment experiment = new TestExperiment();
        mockModel.connectToExperiment(experiment);

        Microservice service = mockModel.getAllMicroservices().get(0);

        Field f = Microservice.class.getDeclaredField("instancesSet");
        f.setAccessible(true);
        Set<MicroserviceInstance> instances = (Set<MicroserviceInstance>) f.get(service);
        MicroserviceInstance instance = instances.stream().findAny().orElse(null);

        CircuitBreaker cb = new CircuitBreaker(mockModel, "", false);
        Retry retry = new Retry(mockModel, "", false);


        Assertions.assertTrue(cb.getListeningPriority() < retry.getListeningPriority());

    }

    @Test
    void general_functionality_test() {
        RandomTieredModel mockModel = new RandomTieredModel("", 2, 20);
        Experiment experiment = TestUtils.getExampleExperiment(mockModel, 100);
        mockModel.connectToExperiment(experiment);

        experiment.start();
        experiment.finish();
    }


}