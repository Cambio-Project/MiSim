package testutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.export.CSVData;
import cambio.simulator.export.ReportCollector;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.mockito.Mockito;

/**
 * @author Lion Wagner
 */
public class TestUtils {
    private static final Random rng = new Random();


    public static Experiment getExampleExperiment(final Model currentModel, final double duration) {

        //RandomTestModel currentModel= new RandomTestModel(null, "TestModel" + nextNonNegative(), max_service_count_per_tier, tier_count);
        currentModel.traceOff();
        currentModel.debugOff();
        TestExperiment currentExperiment = new TestExperiment();
        currentModel.connectToExperiment(currentExperiment);


        currentExperiment.stop(new TimeInstant(duration, TimeUnit.SECONDS));
        currentExperiment.setShowProgressBar(false);
        currentExperiment.traceOff(new TimeInstant(0));
        currentExperiment.debugOff(new TimeInstant(0));
        currentExperiment.setSilent(true);

        return currentExperiment;
    }

//    public static InstanceOwnedPatternConfiguration getRetryPatternMock(Model model) {
//        InstanceOwnedPatternConfiguration data = mock(InstanceOwnedPatternConfiguration.class);
//        Mockito.when(data.getPatternInstance(any(MicroserviceInstance.class)))
//            .thenAnswer(invocationOnMock -> new Retry(model, "Retry", true));
//        return data;
//    }
//
//    public static InstanceOwnedPatternConfiguration getCircuitBreaker(Model model) {
//        InstanceOwnedPatternConfiguration data = mock(InstanceOwnedPatternConfiguration.class);
//        Mockito.when(data.getPatternInstance(any(MicroserviceInstance.class)))
//            .thenAnswer(invocationOnMock -> new CircuitBreaker(model, "CircuitBreaker", true));
//        return data;
//    }
//
//    public static ServiceOwnedPattern getAutoscaler(Model model) {
//        InstanceOwnedPatternConfiguration data = mock(InstanceOwnedPatternConfiguration.class);
//        Mockito.when(data.getPatternInstance(any(Microservice.class)))
//            .thenAnswer(invocationOnMock -> new BasicAutoscalingStrategyProxy(model, "AutoScaler", true));
//        return data;
//    }

    public static void resetModel(RandomTieredModel model) {
        ReportCollector.getInstance().reset(); //resetting static data point collection framework
        NetworkRequestSendEvent.resetCounterSendEvents();
        model.reset();

        //reset mocks to prevent Mockito from leaking
        try {
            Field f = Microservice.class.getDeclaredField("patternsData");
            f.setAccessible(true);
            for (Microservice microservice : model.getAllMicroservices()) {
                ServiceOwnedPattern[] mocks = (ServiceOwnedPattern[]) f.get(microservice);
                Mockito.reset(mocks);

            }
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
        System.gc();
    }

    public static int nextNonNegative() {
        return nextNonNegative(Integer.MAX_VALUE);
    }

    public static int nextNonNegative(int exclusive_bound) {
        return rng.nextInt(exclusive_bound) & Integer.MAX_VALUE;
    }


    public static void writeOutput(List<? extends CSVData> testResults, String fileName) {
        Path filePath = Paths.get(fileName);

        StringBuilder builder = new StringBuilder(testResults.get(0).toCSVHeader()).append('\n');
        for (CSVData csvData : testResults) {
            builder.append(csvData.toCSVData()).append('\n');
        }

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            Files.write(filePath, builder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
