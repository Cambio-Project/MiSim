package cambio.simulator.scenarios.calibration;

import java.io.File;

import cambio.simulator.Main;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class AutoscalingConceptProofTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/Calibration/autoscale_isolation_architecture.json");
        File f2 = new File("./Examples/Calibration/autoscale_experiement.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d"};
        Main.main(args);
    }
}
