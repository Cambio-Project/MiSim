package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Experiment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testutils.RandomTieredModel;
import testutils.TestExperiment;
import testutils.TestUtils;

import java.lang.reflect.Field;
import java.util.Set;

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

        CircuitBreaker cb = new CircuitBreaker(mockModel, "", false, instance);
        RetryManager retry = new RetryManager(mockModel, "", false, instance);


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