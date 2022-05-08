package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.Main;
import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
public class ExampleExperiments extends TestBase {

    @Test
    void Example_ChaosMonkey() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");
        runSimulationCheckExitTempOutput(0, architecture, experiment, "-d");
    }

    @Test
    void Example_Autoscaling() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_scaling.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_autoscale.json");
        runSimulationCheckExitTempOutput(0, architecture, experiment, "-d");
    }


    @Test
    void Example_DelayInjection() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_delayInjection.json");
        runSimulationCheckExitTempOutput(0, architecture, experiment, "-d");
    }
}
