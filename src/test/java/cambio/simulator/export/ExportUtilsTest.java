package cambio.simulator.export;

import static cambio.simulator.test.TestUtils.invertAssertions;

import java.io.File;
import java.io.IOException;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class ExportUtilsTest extends TestBase {

    @Test
    void prepareReportFolder() throws IOException {

        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_experiment.json");

        MiSimModel model = new MiSimModel(test_architecture, test_experiment);

        ExportUtils.prepareReportDirectory(null, model);

        FileUtils.forceDeleteOnExit(model.getExperimentMetaData().getReportBaseDirectory().toFile());
    }


    @Test
    void overwritesReportDirectoryIfNecessary() throws IOException {

        File architecture1 = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment1 = FileLoaderUtil.loadFromExampleResources("example_experiment_delayInjection.json");
        File experiment2 = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");

        File tmpDir = createTempOutputDir();
        File output = runSimulationCheckExitTempOutput(0, architecture1, experiment1);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        //output and tmpDir have different experiment files
        invertAssertions(() -> {
            TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "experiment.json");
            TestUtils.compareFileContentsOfDirectories(output.toPath().resolve("raw"), tmpDir.toPath().resolve("raw"));
        }, "Expected content to differ.");


        FileUtils.forceDelete(tmpDir);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        invertAssertions(() -> {
            TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "metadata.json");
        });

        FileUtils.forceDelete(tmpDir);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        invertAssertions(() -> TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "metadata.json"));


        TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "architecture.json");
        TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "experiment.json");
        TestUtils.compareFileContentsOfDirectories(output.toPath().resolve("raw"), tmpDir.toPath().resolve("raw"));
    }
}