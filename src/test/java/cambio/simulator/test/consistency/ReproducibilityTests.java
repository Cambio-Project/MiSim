package cambio.simulator.test.consistency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cambio.simulator.test.*;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;
import org.opentest4j.AssertionFailedError;

/**
 * @author Lion Wagner
 */
public class ReproducibilityTests extends TestBase {

    @BeforeAll
    static void setUp() throws IOException {
        FileUtils.deleteDirectory(FAILED_TESTS_OUTPUT_DIR);
        //noinspection ResultOfMethodCallIgnored
        FAILED_TESTS_OUTPUT_DIR.mkdirs();
    }

    @Test
    void ChaosMonkeyReproducibility() throws IOException {
        File exp = FileLoaderUtil.loadFromExampleResources("PaperExample", "paper_experiment.json");
        File arch = FileLoaderUtil.loadFromExampleResources("PaperExample", "paper_architecture.json");
        testReproducibility(exp, arch, true);
    }


    @Test
    void CircuitbreakerReproducibility() throws IOException {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_circuitbreaker_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_circuitbreaker_demonstration.json");
        testReproducibility(exp, arch);
    }

    @Test
    void RetryReproducibility() throws IOException {
        File arch = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_architecture.json");
        File exp = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_experiment.json");
        testReproducibility(exp, arch);
    }

    @Disabled //this test takes rather long, therefore it's disabled for now
    @Test
    void LongRunningReproducibilityTest() throws IOException {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_round" +
            "-robin_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_round" +
            "-robin_demonstration.json");

        //remember to remove the output manually from your systems temp folder after the test
        // (or flip the switch to false)
        testReproducibility(exp, arch, false, false); //create trace is set to false for speed reasons
    }


    //--------------------------------------------------Utility Methods-------------------------------------------------
    void testReproducibility(File scenario, File architecture) throws IOException {
        testReproducibility(scenario, architecture, false);
    }

    void testReproducibility(File scenario, File architecture, boolean keepOutput) throws IOException {
        testReproducibility(scenario, architecture, keepOutput, true);
    }

    void testReproducibility(File scenario, File architecture, boolean keepOutput, boolean createTrace)
        throws IOException {
        File output1 = runSimulationCheckExitTempOutput(0, architecture, scenario, createTrace ? "" : "-t");
        File output2 = runSimulationCheckExitTempOutput(0, architecture, scenario, createTrace ? "" : "-t");

        if (keepOutput) {
            tempDirs.remove(output1);
            tempDirs.remove(output2);
        }

        Path rawOutput1;
        Path rawOutput2;

        try (Stream<Path> s1 = Files.walk(output1.toPath(), 2)) {
            rawOutput1 = s1.filter(path -> path.endsWith("raw"))
                .collect(Collectors.toList())
                .get(0);
        }

        try (Stream<Path> s2 = Files.walk(output2.toPath(), 2)) {
            rawOutput2 = s2.filter(path -> path.endsWith("raw"))
                .collect(Collectors.toList())
                .get(0);
        }

        try {
            TestUtils.compareFileContentsOfDirectories(rawOutput1, rawOutput2);
        } catch (AssertionFailedError | junit.framework.AssertionFailedError e) {
            Path trace1;
            Path trace2;

            try (Stream<Path> s1 = Files.walk(output1.toPath(), 2)) {
                trace1 = s1.filter(path -> path.toString().endsWith("trace.html"))
                    .collect(Collectors.toList())
                    .get(0);
            }
            try (Stream<Path> s2 = Files.walk(output2.toPath(), 2)) {
                trace2 = s2.filter(path -> path.toString().endsWith("trace.html"))
                    .collect(Collectors.toList())
                    .get(0);
            }

            List<Pair<String, String>> comparison = TestUtils.compareTwoFiles(trace1.toFile(), trace2.toFile());

            System.out.println("\nTrace comparison, first 10 line difference:");
            // get(0) and (1) always contains the timestamp and different metadata
            for (int i = 2; i < Math.min(12, comparison.size()); i++) {
                System.out.println(comparison.get(i).getValue0());
                System.out.println(comparison.get(i).getValue1());
                System.out.println();
            }

            //create a directory to copy failed test results into
            FileUtils.copyDirectory(output1, FAILED_TESTS_OUTPUT_DIR.toPath().resolve(output1.getName()).toFile());
            FileUtils.copyDirectory(output2, FAILED_TESTS_OUTPUT_DIR.toPath().resolve(output2.getName()).toFile());

            Assertions.fail("Simulation output is not reproducible.", e);
        }
        System.out.println("Output is reproducible.");
    }
}
