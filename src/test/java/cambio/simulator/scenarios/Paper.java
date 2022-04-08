package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Executes the scenario and experiment for the running example of our paper.
 *
 * @author Lion Wagner
 */
@Disabled
public class Paper {

    @Test
    void MinimalScenario() {
        File scenario = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_scenario.json");
        File architecture = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_architecture.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", scenario.getAbsolutePath() };
        cambio.simulator.Main.main(args);
    }

    @Test
    void MinimalExperiment() {
        File scenario = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_experiment.json");
        File architecture = FileLoaderUtil.loadFromExampleResources("PaperExample/paper_architecture.json");
        String[] args =
            new String[] {"-a", architecture.getAbsolutePath(), "-e", scenario.getAbsolutePath(), "-n", "1"};
        cambio.simulator.Main.main(args);
    }
}
