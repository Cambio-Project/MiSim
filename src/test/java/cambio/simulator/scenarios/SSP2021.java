package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.testutils.FileLoaderUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test that execute the scenario and experiment version of the SSP2021 presentation example.
 *
 *
 *
 * @author Lion Wagner
 * @see <a href="https://www.performance-symposium.org/2021/">SSP2021</a>
 */
@Disabled
public class SSP2021 {

    @Test
    void ExecuteDemonstration_Scenario() {
        File architecture = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_architecture.json");
        File experiment = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_scenario.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", experiment.getAbsolutePath()};
        cambio.simulator.Main.main(args);
    }

    @Test
    void ExecuteDemonstration_Experiment() {
        File architecture = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_architecture.json");
        File experiment = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_experiment.json");
        String[] args = new String[] {"-a", architecture.getAbsolutePath(), "-e", experiment.getAbsolutePath()};
        cambio.simulator.Main.main(args);
    }
}
