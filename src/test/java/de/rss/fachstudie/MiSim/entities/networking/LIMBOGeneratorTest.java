package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.generator.LIMBOGenerator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testutils.TestModel;

import java.io.File;
import java.util.concurrent.TimeUnit;

class LIMBOGeneratorTest {
    private static final Experiment exp = new Experiment("Test Experiment");
    ;
    private static File arrivalRates;
    private static TestModel model;
    private static Microservice service;

    private static final File testFile = new File("src/test/resources/test_loon_mock_arrival_rates.csv");


    @BeforeAll
    static void beforeAll() {
        model = new TestModel(null, "TestModel", true, true, () -> {
            service.updateInstancesCount(1);
            LIMBOGenerator gen = new LIMBOGenerator(model, "Generator", true, service.getOperation("TestOP"), arrivalRates);

        }, () -> {
            service = new Microservice(model, "TestMS1", true);
            service.setCapacity(5);
//            service.setPatterns(new Pattern[]{new Pattern(){{setName("Thread Pool");setArguments(new Integer[]{1});}}});
            service.setOperations(new Operation[]{new Operation(model, "TestOP", true) {{
                setDemand(1);
            }}});
        });

        arrivalRates = testFile;
        model.connectToExperiment(exp);
        exp.setSeedGenerator((long) (Math.random() * Long.MAX_VALUE));
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(false);
        exp.stop(new TimeInstant(10, TimeUnit.SECONDS));
        exp.tracePeriod(new TimeInstant(0, TimeUnit.SECONDS), new TimeInstant(10, TimeUnit.SECONDS));
        exp.debugPeriod(new TimeInstant(0, TimeUnit.SECONDS), new TimeInstant(10, TimeUnit.SECONDS));
    }

    @Test
    void name() {
        exp.start();
        exp.finish();
    }
}