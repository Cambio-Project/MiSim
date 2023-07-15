package cambio.simulator.export;

import static cambio.simulator.test.TestUtils.invertAssertions;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.test.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class ExportUtilsTest extends TestBase {

    @Test
    void preparesReportFolder() throws IOException {

        File tmpOutput = super.createSelfDeletingTempOutputDir();
        Path tmpFile = Files.createTempFile("misim", ".json");

        assertTrue(tmpFile.toFile().exists());

        ExperimentMetaData metaData = new ExperimentMetaData();
        Util.injectField("reportBaseFolder", metaData, tmpOutput.toPath());
        Util.injectField("experimentName", metaData, "test");
        Util.injectField("archFileLocation", metaData, tmpFile.toFile());
        Util.injectField("expFileLocation", metaData, tmpFile.toFile());
        Util.injectField("duration", metaData, 1f);

        ExportUtils.prepareReportDirectory(null, metaData);

        Path reportLocation = metaData.getReportLocation();

        assertTrue(reportLocation.toFile().exists());
        assertTrue(reportLocation.toFile().isDirectory());
        assertNotNull(reportLocation.toFile().listFiles());

        File[] files = Objects.requireNonNull(reportLocation.toFile().listFiles());
        assertTrue(Arrays.stream(files).anyMatch(file -> file.getName().equals("experiment.json")));
        assertTrue(Arrays.stream(files).anyMatch(file -> file.getName().equals("architecture.json")));
        assertTrue(Arrays.stream(files).anyMatch(file -> file.getName().equals("metadata.json")));
    }


    @Test
    void overwritesReportDirectoryIfNecessary() throws IOException, InterruptedException {

        File architecture1 = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment1 = FileLoaderUtil.loadFromExampleResources("example_experiment_delayInjection.json");
        File experiment2 = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");

        File tmpDir = createSelfDeletingTempOutputDir();
        File output = runSimulationCheckExitTempOutput(0, architecture1, experiment1);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        //output and tmpDir have different experiment files
        invertAssertions(() -> {
            TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "experiment.json");
            TestUtils.compareFileContentsOfDirectories(output.toPath().resolve("raw"), tmpDir.toPath().resolve("raw"));
        }, "Expected content to differ.");


        Thread.sleep(1000);
        FileUtils.forceDelete(tmpDir);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        //test that metadata.json was overwritten
        invertAssertions(() -> TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "metadata.json"));

        Thread.sleep(1000);
        FileUtils.forceDelete(tmpDir);
        FileUtils.copyDirectory(output, tmpDir);
        runSimulationCheckExit(0, architecture1, experiment2, "-O", output.getAbsolutePath());

        invertAssertions(() -> TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "metadata.json"));


        TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "architecture.json");
        TestUtils.testFileEquality(output.toPath(), tmpDir.toPath(), "experiment.json");
        TestUtils.compareFileContentsOfDirectories(output.toPath().resolve("raw"), tmpDir.toPath().resolve("raw"));
    }
}
