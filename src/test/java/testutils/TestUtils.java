package testutils;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.entities.patterns.PreemptiveAutoScaler;
import de.rss.fachstudie.MiSim.entities.patterns.RetryManager;
import de.rss.fachstudie.MiSim.parsing.PatternData;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.javatuples.Pair;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

/**
 * @author Lion Wagner
 */
public class TestUtils {
    private static final Random rng = new Random();

    public static Pair<Model, Experiment> getExampleExperiment(int max_service_count_per_tier, int tier_count) {

        RandomTestModel currentModel = new RandomTestModel(null, "TestModel" + nextNonNegative(), max_service_count_per_tier, tier_count);
        TestExperiment currentExperiment = new TestExperiment();
        currentModel.connectToExperiment(currentExperiment);


        currentExperiment.stop(new TimeInstant(RandomTestModel.duration, TimeUnit.SECONDS));
//        currentExperiment.tracePeriod(new TimeInstant(0, TimeUnit.SECONDS), new TimeInstant(RandomTestModel.duration, TimeUnit.SECONDS));
//        currentExperiment.debugPeriod(new TimeInstant(0, TimeUnit.SECONDS), new TimeInstant(RandomTestModel.duration, TimeUnit.SECONDS));
        currentExperiment.setShowProgressBar(false);
        currentExperiment.traceOff(new TimeInstant(0));
        currentExperiment.debugOff(new TimeInstant(0));
        currentExperiment.setSilent(true);

        return new Pair<>(currentModel, currentExperiment);
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
                .thenAnswer(invocationOnMock -> new CircuitBreaker(model, "CircuitBreaker", true, invocationOnMock.getArgument(0)));
        return data;
    }

    public static PatternData getAutoscaler(Model model) {
        PatternData data = mock(PatternData.class);
        Mockito.when(data.tryGetServiceOwnedPatternOrNull(any(Microservice.class)))
                .thenAnswer(invocationOnMock -> new PreemptiveAutoScaler(model, "AutoScaler", true, invocationOnMock.getArgument(0)));
        return data;
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
     * @return the asked percentile
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

}
