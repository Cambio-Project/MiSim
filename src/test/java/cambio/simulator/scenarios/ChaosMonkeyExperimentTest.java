package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.testutils.FileLoaderUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class ChaosMonkeyExperimentTest {

    @Test
    void ExampleExecutionRun() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_chaosmonkey.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", experiment.getAbsolutePath(), "-d"
            , "-p"};
        cambio.simulator.Main.main(args);
    }
}
