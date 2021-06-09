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

import de.unistuttgart.sqa.orcas.misim.entities.microservice.Microservice;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;
import de.unistuttgart.sqa.orcas.misim.entities.networking.NetworkRequestSendEvent;
import de.unistuttgart.sqa.orcas.misim.entities.patterns.CircuitBreaker;
import de.unistuttgart.sqa.orcas.misim.entities.patterns.PreemptiveAutoScaler;
import de.unistuttgart.sqa.orcas.misim.entities.patterns.RetryManager;
import de.unistuttgart.sqa.orcas.misim.export.CSVData;
import de.unistuttgart.sqa.orcas.misim.export.ReportCollector;
import de.unistuttgart.sqa.orcas.misim.parsing.PatternData;
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

    public static PatternData getRetryPatternMock(Model model) {
        PatternData data = mock(PatternData.class);
        Mockito.when(data.tryGetInstanceOwnedPatternOrNull(any(MicroserviceInstance.class)))
            .thenAnswer(invocationOnMock -> new RetryManager(model, "Retry", true, invocationOnMock.getArgument(0)));
        return data;
    }

    public static PatternData getCircuitBreaker(Model model) {
        PatternData data = mock(PatternData.class);
        Mockito.when(data.tryGetInstanceOwnedPatternOrNull(any(MicroserviceInstance.class)))
            .thenAnswer(
                invocationOnMock -> new CircuitBreaker(model, "CircuitBreaker", true, invocationOnMock.getArgument(0)));
        return data;
    }

    public static PatternData getAutoscaler(Model model) {
        PatternData data = mock(PatternData.class);
        Mockito.when(data.tryGetServiceOwnedPatternOrNull(any(Microservice.class)))
            .thenAnswer(invocationOnMock -> new PreemptiveAutoScaler(model, "AutoScaler", true,
                invocationOnMock.getArgument(0)));
        return data;
    }

    public static void resetModel(RandomTieredModel model) {
        ReportCollector.getInstance().reset(); //resetting static data point collection framework
        NetworkRequestSendEvent.resetCounterSendEvents();
        model.reset();

        //reset mocks to prevent Mockito from leaking
        try {
            Field f = Microservice.class.getDeclaredField("patternsData");
            f.setAccessible(true);
            for (Microservice microservice : model.getAllMicroservices()) {
                PatternData[] mocks = (PatternData[]) f.get(microservice);
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

    /**
     * Calculates a specific percentile of a collection of numbers.
     *
     * @param percentile        target percentile in [0:100)
     * @param number_collection collection containing the analysed dataset
     *
     * @return the asked percentile
     *
     * @throws org.apache.commons.math3.exception.MathIllegalArgumentException if percentile not in [0:100)
     * @see Percentile
     */
    public static double percentile(final double percentile, final Collection<? extends Number> number_collection) {
        double[] sortedData = number_collection.stream().mapToDouble(Number::doubleValue).sorted().toArray();
        return new Percentile(percentile).evaluate(sortedData);
    }

    public static double median(final Collection<? extends Number> number_collection) {
        return percentile(.5, number_collection);
    }

    public static double mean(final Collection<? extends Number> number_collection) {
        return number_collection.stream().mapToDouble(Number::doubleValue).average().orElse(-1);
    }

    public static double median(Number... number_collection) {
        return percentile(.5, Arrays.asList(number_collection));
    }

    public static double mean(Number... number_collection) {
        return Arrays.stream(number_collection).mapToDouble(Number::doubleValue).average().orElse(-1);
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
