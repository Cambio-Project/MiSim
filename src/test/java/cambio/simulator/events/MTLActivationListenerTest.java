package cambio.simulator.events;

import java.io.File;
import java.nio.file.Paths;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Test;

class MTLActivationListenerTest extends TestBase {

    @Test
    void test_scaling_load() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");
        String pathToMTL = Paths.get("src", "test", "resources", "MTL", "load_scaling.mtl").toAbsolutePath().toString();

        File out =  runSimulationCheckExitTempOutput(0, architecture, experiment, "-d", "-m", pathToMTL);
        System.out.println(out.getAbsolutePath());
    }

    @Test
    void test_scaling_additive() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");
        String pathToMTL =
            Paths.get("src", "test", "resources", "MTL", "load_additive.mtl").toAbsolutePath().toString();

        File out =  runSimulationCheckExitTempOutput(0, architecture, experiment, "-d", "-m", pathToMTL);
        System.out.println(out.getAbsolutePath());
    }
}