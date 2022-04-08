package cambio.simulator.test.consistency;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import cambio.simulator.test.*;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
public class Reproducibility extends TestBase {


    @Test
    void SimpleReproducibility() throws IOException {
        File scenario = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_experiment.json");
        File architecture = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_architecture.json");

        File output1 = runSimulationCheckExitTempOutput(0, architecture, scenario);
        File output2 = runSimulationCheckExitTempOutput(0, architecture, scenario);

        Path rawOutput1 =
            Files.walk(output1.toPath(), 2).filter(path -> path.endsWith("raw")).collect(Collectors.toList()).get(0);
        Path rawOutput2 =
            Files.walk(output2.toPath(), 2).filter(path -> path.endsWith("raw")).collect(Collectors.toList()).get(0);

        TestUtils.compareFileContentsOfDirectories(rawOutput1, rawOutput2);
    }
}
