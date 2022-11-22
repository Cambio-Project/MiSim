package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Test;

/**
 * Test that execute the scenario and experiment version of the SSP2021 presentation example.
 *
 * @author Lion Wagner
 * @see <a href="https://www.performance-symposium.org/2021/">SSP2021</a>
 */
public class QRS2022 extends TestBase {

    @Test
    void ExecuteDemonstration_Experiment() {
        File architecture = FileLoaderUtil.loadFromExampleResources("PaperExample", "QRSExample",
            "qrs_architecture.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("PaperExample", "QRSExample",
            "qrs_experiment.json");
        runSimulationCheckExitTempOutput(0, architecture, experiment);
    }
}
