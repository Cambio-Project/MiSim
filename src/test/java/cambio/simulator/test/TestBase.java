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

import cambio.simulator.Main;
import org.codehaus.plexus.util.FileUtils;
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
        File output1 = runSimulationCheckExitTempOutput(0, architecture, scenario, "-t");
        File output2 = runSimulationCheckExitTempOutput(0, architecture, scenario, "-t");

        if (keepOutput) {
            tempDirs.remove(output1);
            tempDirs.remove(output2);
        }

        Path rawOutput1 = Files.walk(output1.toPath(), 2)
            .filter(path -> path.endsWith("raw"))
            .collect(Collectors.toList())
            .get(0);
        Path rawOutput2 = Files.walk(output2.toPath(), 2)
            .filter(path -> path.endsWith("raw"))
            .collect(Collectors.toList())
            .get(0);

        TestUtils.compareFileContentsOfDirectories(rawOutput1, rawOutput2);
    }
}
