package Scenarios.calibration;

import java.io.File;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class CircuitbreakerConceptProofTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/Calibration/circuitbreaker_isolation_architecture.json");
        File f2 = new File("./Examples/Calibration/circuitbreaker_experiement.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }
}
