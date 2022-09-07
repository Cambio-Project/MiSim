package cambio.simulator.scenarios;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
public class Teastore extends TestBase {

    @Test
    public void simulateTeastore() {
        File arch = FileLoaderUtil.loadFromExampleResources("Teastore", "architecture_model.json");
        File experiment = FileLoaderUtil.loadFromExampleResources("Teastore", "experiment_load.json");
        runSimulationCheckExit(0, arch, experiment, "-p", "-t");
    }
}
