package cambio.simulator.test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cambio.simulator.Main;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

/**
 * @author Lion Wagner
 */
public class TestBase {

    private List<File> tempDirs = new ArrayList<>();

    @AfterEach
    void tearDown() throws IOException {
        for (File file : tempDirs) {
            FileUtils.deleteDirectory(file);
        }
    }

    protected void runSimulationCheckExit(int expectedExitCode, File arch, File exp, String... additionalArgs) {
        try {
            int code = catchSystemExit(() -> {
                String[] fileLocations = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-d"};
                String[] allArgs = new String[additionalArgs.length + fileLocations.length];
                System.arraycopy(fileLocations, 0, allArgs, 0, fileLocations.length);
                System.arraycopy(additionalArgs, 0, allArgs, fileLocations.length, additionalArgs.length);
                Main.main(allArgs);
            });
            assertEquals(expectedExitCode, code);
        } catch (Exception e) {
            Assertions.fail("Simulation failed.", e);
        }
    }

    protected File runSimulationCheckExitTempOutput(int expectedExitCode, File arch, File exp,
                                                    String... additionalArgs) {
        File dir = createTempOutputDir();
        String[] allArgs = new String[additionalArgs.length + 2];
        allArgs[0] = "-o";
        allArgs[1] = dir.getAbsolutePath();
        System.arraycopy(additionalArgs, 0, allArgs, 2, additionalArgs.length);
        runSimulationCheckExit(expectedExitCode, arch, exp, allArgs);
        return dir;
    }

    protected File createTempOutputDir() {
        File dir = null;
        try {
            dir = Files.createTempDirectory("misim-test-").toFile();
            tempDirs.add(dir);
        } catch (IOException e) {
            Assertions.fail("Could not create temporary output directory.");
        }
        return dir;
    }


    protected void testReproducibility(File scenario, File architecture) throws IOException {
        testReproducibility(scenario, architecture, false);
    }

    protected void testReproducibility(File scenario, File architecture, boolean keepOutput) throws IOException {
        File output1 = runSimulationCheckExitTempOutput(0, architecture, scenario, keepOutput ? "" : "-t");
        File output2 = runSimulationCheckExitTempOutput(0, architecture, scenario, keepOutput ? "" : "-t");

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
        }
        catch (AssertionError e) {
            if(keepOutput){
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

                List<Pair<String,String>> comparisson = TestUtils.compareTwoFiles(trace1.toFile(), trace2.toFile());

                System.out.println("\nTrace comparisson, first line difference:");
                System.out.println(comparisson.get(1).getValue0()); // get(0) always contains the timestamp which
            }
            Assertions.fail("Simulation output is not reproducible.", e);
        }

    }
}
