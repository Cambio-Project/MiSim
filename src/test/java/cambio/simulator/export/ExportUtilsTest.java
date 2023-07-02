package cambio.simulator.export;

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
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Test;

class ExportUtilsTest extends TestBase {

    @Test
    void preparesReportFolder() throws IOException {

        File tmpOutput = super.createTempOutputDir();
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
}