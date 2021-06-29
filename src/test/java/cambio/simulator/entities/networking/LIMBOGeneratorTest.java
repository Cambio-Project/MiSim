package cambio.simulator.entities.networking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import cambio.simulator.entities.generator.GeneratorStopException;
import cambio.simulator.entities.generator.LIMBOGenerator;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testutils.TestExperiment;
import testutils.TestModel;


class LIMBOGeneratorTest {

    /**
     * Test class that exposes the important next TargetTime for testing
     */
    private static class ExposingLIMBOGenerator extends LIMBOGenerator {
        public ExposingLIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation,
                                      File limboModel) {
            super(model, name, showInTrace, operation, limboModel);
        }

        public ExposingLIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation,
                                      File limboModel, double repetition_skip) {
            super(model, name, showInTrace, operation, limboModel, repetition_skip);
        }

        public ExposingLIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation,
                                      File limboModel, boolean repeating) {
            super(model, name, showInTrace, operation, limboModel, repeating);
        }

        public ExposingLIMBOGenerator(Model model, String name, boolean showInTrace, Operation operation,
                                      File LimboProfile, boolean repeating, double repetition_skip) {
            super(model, name, showInTrace, operation, LimboProfile, repeating, repetition_skip);
        }

        public TimeInstant getNextTargetTime(TimeInstant lastTargetTime) {
            return super.getNextTargetTime(lastTargetTime);
        }

        public TimeInstant getFirstTargetTime() {
            return super.getFirstTargetTime();
        }
    }


    private static TestExperiment testExperiment;
    private static TestModel mod;

    private static final File testFile = new File("src/test/resources/example_arrival_rates.csv");
    private static final File testFile2 = new File("Examples/example_arrival_rates_simple.csv");


    @BeforeAll
    static void beforeAll() {
        testExperiment = new TestExperiment();
        mod = new TestModel(null, "TestModel", false, false, () -> {
        }, () -> {
        });
        mod.connectToExperiment(testExperiment);
    }

    /**
     * Provide a function to generate the load based on the current simulation time. Throw any kind of exception (except
     * for an IOException) to stop the generation.
     *
     * @return the temporary File that contains the load model
     */
    private File createTestLoadModel(Function<Integer, Double> loadFunction) {
        File f = null;
        int time = 0;
        try {
            f = File.createTempFile("tmp_load_", ".csv");

            while (true) {
                double nextLoad = loadFunction.apply(time);
                Files.write(f.toPath(), String.format("%s;%s\n", time, nextLoad).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
                time++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Stopped generating the load Function at time " + time);
        }

        return f;
    }

    @Test
    void loadsFirstTargetTimeCorrectly() {
        File testLoadmodel = createTestLoadModel(integer -> (integer <= 2) ? (double) integer :
            0 / 0); //throwing an exception on purpose to stop generation
        ExposingLIMBOGenerator gen =
            new ExposingLIMBOGenerator(mod, "TestGenerator", false, getMockOperation(), testLoadmodel);
        TimeInstant nextTargetTime = gen.getFirstTargetTime();
        assertEquals(1, nextTargetTime.getTimeAsDouble(), 0.000001);
    }


    @Test
    void repeatsCorrectly() {
        Function<Integer, Double> func = integer -> {
            if (integer > 200) {
                throw new RuntimeException();
            }
            return integer.doubleValue();
        };
        File testLoadmodel = createTestLoadModel(func);
        ExposingLIMBOGenerator gen =
            new ExposingLIMBOGenerator(mod, "TestGenerator", false, getMockOperation(), testLoadmodel, false);

        int maxTime = 200;
        Map<TimeInstant, Integer> targetTimes = getGeneratorResults(gen, new TimeInstant(maxTime));
        for (Map.Entry<TimeInstant, Integer> entry : targetTimes.entrySet()) {
            assertEquals(entry.getKey().getTimeAsDouble(), (double) entry.getValue(), 0.000001);
        }
    }

    @Test
    void ArrivalRateTest() {
        int maxTime = 10;
        Function<Integer, Double> func = integer -> (integer <= maxTime) ? (double) (integer * integer) :
            0 / 0;
        File testLoadmodel = createTestLoadModel(func); //throwing an exception on purpose to stop generation
        ExposingLIMBOGenerator gen =
            new ExposingLIMBOGenerator(mod, "TestGenerator", false, getMockOperation(), testLoadmodel, false);

        Map<TimeInstant, Integer> targetTimes = getGeneratorResults(gen, new TimeInstant(10));
        assertEquals(maxTime, targetTimes.size());
        for (Map.Entry<TimeInstant, Integer> entry : targetTimes.entrySet()) {
            double targetValue = entry.getKey().getTimeAsDouble() * entry.getKey().getTimeAsDouble();
            assertEquals(targetValue, (double) entry.getValue(), 0.000001);
        }

    }

    private Map<TimeInstant, Integer> getGeneratorResults(ExposingLIMBOGenerator generator) {
        return getGeneratorResults(generator, new TimeInstant(Double.MAX_VALUE));
    }

    private Map<TimeInstant, Integer> getGeneratorResults(ExposingLIMBOGenerator generator, TimeInstant maxTime) {
        Map<TimeInstant, Integer> targetTimes = new HashMap<>();
        try {
            targetTimes.put(generator.getFirstTargetTime(), 1);
            while (targetTimes.keySet().stream().min(TimeInstant::compareTo).get().compareTo(maxTime) < 0) {
                targetTimes
                    .merge(generator.getNextTargetTime(targetTimes.keySet().stream().max(TimeInstant::compareTo).get()),
                        1, Integer::sum);
            }
        } catch (GeneratorStopException ignored) {
        }
        return targetTimes;
    }


    private Operation getMockOperation() {
        Operation op = Mockito.mock(Operation.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(op.getOwnerMS().getName()).thenReturn("TestMS");
        Mockito.when(op.getName()).thenReturn("TestOP");
        return op;
    }
}