package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
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
public class SSP2021 extends TestBase {

    @Test
    void ExecuteDemonstration_Scenario() {
        File architecture = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_architecture.json");
        File experiment = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_scenario.json");
        runSimulationCheckExitTempOutput(0, architecture,experiment);
    }

    @Test
    void ExecuteDemonstration_Experiment() {
        File architecture = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_architecture.json");
        File experiment = FileLoaderUtil.loadFromTestResources("SSPExample", "ssp_experiment.json");
        runSimulationCheckExitTempOutput(0, architecture,experiment);
    }
}
