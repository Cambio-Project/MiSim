package scenarios;

import java.io.File;

import cambio.simulator.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testutils.FileLoaderUtil;

/**
 * @author Lion Wagner
 */
@Disabled
public class DelayInjectionExperimentTest {

    @Test
    void ExampleExecutionRun() {

        File architecture = FileLoaderUtil.loadFromExampleResources("example_architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("example_experiment_delayInjection.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", experiment.getAbsolutePath(), "-d"};
        MainModel.main(args);
    }
}
