package Scenarios.calibration;

import java.io.File;

import cambio.simulator.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class RetryConceptProofTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/Evaluation/retry_isolation_architecture.json");
        File f2 = new File("./Examples/Evaluation/retry_experiement.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }
}
