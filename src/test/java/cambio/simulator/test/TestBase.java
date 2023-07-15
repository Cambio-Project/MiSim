package cambio.simulator.test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import cambio.simulator.Main;
import cambio.simulator.export.MiSimReporters;
import cambio.simulator.misc.RNGStorage;
import cambio.simulator.models.MiSimModel;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;

/**
 * @author Lion Wagner
 */
public class TestBase {

    public static final File FAILED_TESTS_OUTPUT_DIR = new File("failed_test_results");

    public List<File> tempDirs = new ArrayList<>();


    @AfterEach
    protected void tearDown() throws IOException {
        MiSimReporters.finalizeReports(); //closes open file handles
        for (File file : tempDirs) {
            FileUtils.deleteDirectory(file);
        }
        RNGStorage.reset();
    }

    public MiSimModel getMockModel() {
        File architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File scenario = FileLoaderUtil.loadFromTestResources("test_empty_experiment.json");
        return getMockModel(architecture, scenario);
    }

    public MiSimModel getMockModel(File architecture, File scenario) {
        MiSimModel mockModel = new MiSimModel(architecture, scenario);
        mockModel.getExperimentMetaData().setReportLocation(createSelfDeletingTempOutputDir().toPath());
        return mockModel;
    }

    public Pair<MiSimModel, TestExperiment> getConnectedMockModel() {
        File architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File scenario = FileLoaderUtil.loadFromTestResources("test_empty_experiment.json");
        return getConnectedMockModel(architecture, scenario);
    }

    public Pair<MiSimModel, TestExperiment> getConnectedMockModel(File architecture, File scenario) {
        MiSimModel mockModel = getMockModel(architecture, scenario);
        TestExperiment testExperiment = new TestExperiment();
        mockModel.connectToExperiment(testExperiment);
        return new Pair<>(mockModel, testExperiment);
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
        File dir = createSelfDeletingTempOutputDir();
        String[] allArgs = new String[additionalArgs.length + 2];
        allArgs[0] = "-O";
        allArgs[1] = dir.getAbsolutePath();
        System.arraycopy(additionalArgs, 0, allArgs, 2, additionalArgs.length);
        runSimulationCheckExit(expectedExitCode, arch, exp, allArgs);
        return dir;
    }

    protected File createSelfDeletingTempOutputDir() {
        File dir = createTempOutputDir().toFile();
        tempDirs.add(dir);
        return dir;
    }

    public static Path createTempOutputDir() {
        try {
            return Files.createTempDirectory("misim-test-");
        } catch (IOException e) {
            Assertions.fail("Could not create temporary output directory.");
            throw new RuntimeException(e); //please compiler
        }
    }
}
