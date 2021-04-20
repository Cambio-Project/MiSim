package Scenarios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.rss.fachstudie.MiSim.export.ReportCollector;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testutils.TestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Lion Wagner
 */
@Disabled
public class PerformanceTests {
    static class Dataset {
        public double mean;
        public double median;
        public double percentile_99;

        public Dataset(Collection<? extends Number> data) {
            mean = TestUtils.mean(data);
            median = TestUtils.median(data);
            percentile_99 = TestUtils.percentile(99, data);
        }
    }

    Dataset performanceTest(int max_service_count, int tier_count, int iterations) {
        List<Long> times = new LinkedList<>();
        System.out.printf("Running %s tiers, %s services%n", tier_count, max_service_count);
        System.out.printf("Running %s iterations.%n", iterations);
        for (int i = 0; i < iterations; i++) {
            System.out.printf("Run #%s%n", i + 1);
            Pair<Model, Experiment> p = TestUtils.getExampleExperiment(max_service_count, tier_count);
            p.getValue0().traceOff();
            p.getValue1().traceOff(new TimeInstant(0));

            long start = System.currentTimeMillis();
            p.getValue1().start();
            p.getValue1().finish();
            long duration = System.currentTimeMillis() - start;

            times.add(duration);
            System.gc();
            ReportCollector.getInstance().reset(); //resetting static data point collection framework
        }
//        System.out.println(times);
        return new Dataset(times);
    }

    @Test
    void SystemScalingPerformanceTest() {
        HashMap<Pair<Integer, Integer>, Dataset> data = new HashMap<>();
        for (int tierCount = 1; tierCount <= 10; tierCount++) {
            for (int max_service_per_tier = 1; max_service_per_tier <= 10; max_service_per_tier++) {
                Dataset result = performanceTest(max_service_per_tier, tierCount, 5);
                data.put(new Pair<>(tierCount, max_service_per_tier), result);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonResult = gson.toJson(data);

        StringBuilder builder = new StringBuilder("tiers,max_services_per_tier,mean,median,percentile_99\n");
        for (Map.Entry<Pair<Integer, Integer>, Dataset> entry : data.entrySet()) {
            builder.append(String.format("%s,%s,%s,%s,%s\n", entry.getKey().getValue0(), entry.getKey().getValue1(), entry.getValue().mean, entry.getValue().median, entry.getValue().percentile_99));
        }

        try {
            Files.write(Paths.get("./performance_test_result.json"), jsonResult.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            Files.write(Paths.get("./performance_test_result.csv"), builder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
