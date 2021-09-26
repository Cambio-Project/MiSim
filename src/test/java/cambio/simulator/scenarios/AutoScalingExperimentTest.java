package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import cambio.simulator.testutils.FileLoaderUtil;

/**
 * @author Lion Wagner
 */
@Disabled
public class AutoScalingExperimentTest {

    @Test
    void ExampleExecutionRun() {
        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_scaling.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_autoscale.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", experiment.getAbsolutePath(), "-d"
            , "-p"};
        MainModel.main(args);
    }
}
