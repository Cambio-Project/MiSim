package Scenarios.calibration;

import java.io.File;

import de.unistuttgart.sqa.orcas.misim.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class EvaluationResponseTimesTest {

    @Test
    void ExampleExecutionRunExperiment() {
        File f = new File("./Examples/Calibration/example_architecture_architecture.json");
        File f2 = new File("./Examples/Calibration/response_time_experiement.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void ExampleExecutionRunOnCalibration() {
        File f = new File("./Examples/Calibration/example_architecture_architecture.json");
        File f2 = new File("./Examples/Calibration/response_time_calibration.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }
}
