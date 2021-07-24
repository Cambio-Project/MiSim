package scenarios;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cambio.simulator.entities.networking.NetworkRequestSendEvent;
import cambio.simulator.export.CSVData;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.NameCatalog;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testutils.RandomTieredModel;
import testutils.TestUtils;

/**
 * @author Lion Wagner
 */
@Disabled
public class PerformanceTests {
    static List<TestResult> performanceTest(int max_service_count, int tier_count, int iterations) {
        List<TestResult> testResults = new LinkedList<>();

        System.out.printf("Running %s tiers, %s services%n", tier_count, max_service_count);
        System.out.printf("Running %s iterations.%n", iterations);
        for (int i = 0; i < iterations; i++) {
            System.out.printf("Run #%s%n", i + 1);

            RandomTieredModel model = new RandomTieredModel("TestModel", max_service_count, tier_count);
            int simulatedDuration =
                TestUtils.nextNonNegative(3401) + 200; //3600 is 1h of realtime with the default unit SECONDS
            Experiment exp = TestUtils.getExampleExperiment(model, simulatedDuration);

            SimProcess memoryFree = new SimProcess(model, "memoryFreeEvent", false, false) {
                @Override
                public void lifeCycle() throws SuspendExecution {
                    long totalMem = Runtime.getRuntime().totalMemory();
                    long freeMem = Runtime.getRuntime().freeMemory();
                    double free = (double) freeMem / totalMem;
                    if (free < 0.2) {
                        try {
                            Field f = Experiment.class.getDeclaredField("_nameCatalog");
                            f.setAccessible(true);
                            NameCatalog nameCatalog = (NameCatalog) f.get(this.getModel().getExperiment());
                            Field f2 = NameCatalog.class.getDeclaredField("_catalog");
                            f2.setAccessible(true);
                            f2.set(nameCatalog, new HashMap<String, Integer>());
//                            HashMap<String, Integer> catalog = (HashMap<String, Integer>) f2.get(nameCatalog);
//                            List<String> toRemove = new LinkedList<>();
//                            for (Map.Entry<String, Integer> entry : catalog.entrySet()) {
//                                if (entry.getValue() == 1) {
//                                    toRemove.add(entry.getKey());
//                                }
//                            }
//                            toRemove.forEach(catalog::remove);
//                            System.out.println(catalog.size());

                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    this.hold(new TimeSpan(1, TimeUnit.SECONDS));
                }
            };

            memoryFree.activate(new TimeInstant(0));

            long start = System.currentTimeMillis();
            exp.start();
            exp.finish();
            long duration = System.currentTimeMillis() - start;

            TestResult result = new TestResult();
            result.simulated_duration_ms = simulatedDuration * 1000;
            result.execution_duration_ms = duration;
            result.number_of_services = model.getAllMicroservices().size();
            result.number_of_dependencies = model.getAllOperations().size();
            result.number_of_sendEvents = NetworkRequestSendEvent.getCounterSendEvents();
            NetworkRequestSendEvent.resetCounterSendEvents();

            testResults.add(result);
            TestUtils.resetModel(model);
        }
//        System.out.println(times);
        return testResults;
    }

    @Test
    void SystemScalingPerformanceTest() {

        List<TestResult> data = new LinkedList<>();
        //warmup
        System.out.println("Warmup");
        performanceTest(10, 3, 5);
        System.out.println("Warmup Done");

        for (int tierCount = 1; tierCount <= 6; tierCount++) {
            for (int max_service_per_tier = 3; max_service_per_tier <= 10; max_service_per_tier++) {
                List<TestResult> result = performanceTest(max_service_per_tier, tierCount, 5);
                data.addAll(result);
                TestUtils.writeOutput(data, "./performance_test_result_partial.csv");
            }
        }

        TestUtils.writeOutput(data, "./performance_test_result.csv");
    }

    @Test
    void SystemGeneratorImpactTest() {
        List<TestResult> testResults = new LinkedList<>();

        //warmup
        System.out.println("Warmup");
        performanceTest(15, 15, 3);
        System.out.println("Warmup Done");

        double maxMsgPerSec = 2000.0;

        for (int msgPerSec = 1; msgPerSec <= maxMsgPerSec; msgPerSec += 10) {
            System.out.printf("progress %s%n", msgPerSec / maxMsgPerSec);

            RandomTieredModel model = new RandomTieredModel("Large TestModel", 500, 15);
            int genCount = Math.max(1, TestUtils.nextNonNegative(msgPerSec));
            double interval = msgPerSec == 1 ? (double) msgPerSec / genCount : 1;
            model.setGenerator_count(genCount);
            model.setGenerator_interval(interval);
            int simulatedDuration =
                TestUtils.nextNonNegative(10801); //10800 are 3h of realtime with the default unit SECONDS
            Experiment exp = TestUtils.getExampleExperiment(model, simulatedDuration);

            long start = System.currentTimeMillis();
            exp.start();
            exp.finish();
            long duration = System.currentTimeMillis() - start;


            TestResult result = new TestResult();
            result.simulated_duration_ms = simulatedDuration * 1000;
            result.execution_duration_ms = duration;
            result.number_of_services = model.getAllMicroservices().size();
            result.number_of_dependencies =
                model.getAllMicroservices().stream().mapToInt(ms -> ms.getOperations().length).sum();
            result.number_of_sendEvents = NetworkRequestSendEvent.getCounterSendEvents();

            testResults.add(result);
            TestUtils.resetModel(model);

        }

        TestUtils.writeOutput(testResults, "./message_scale_test.csv");
    }

    static class TestResult implements CSVData {
        public long execution_duration_ms;
        public int simulated_duration_ms;
        public int number_of_services;
        public int number_of_dependencies;
        public long number_of_sendEvents;
    }
}
