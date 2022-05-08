package cambio.simulator.scenarios.evaluation;

import static cambio.simulator.test.FileLoaderUtil.loadFromExampleResources;

import java.io.File;

import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
@Deprecated
public class EvaluationResponseTimesTest extends TestBase {

    @Test
    void ExampleExecutionRunExperiment() {
        File f = loadFromExampleResources("ProofOfConcept", "example_architecture.json");
        File f2 = loadFromExampleResources("ProofOfConcept", "response_time_experiment.json");
        runSimulationCheckExitTempOutput(0, f, f2, "-d");
    }

    @Test
    void ExampleExecutionRunOnCalibration() {
        File f = loadFromExampleResources("ProofOfConcept", "example_architecture.json");
        File f2 = loadFromExampleResources("ProofOfConcept", "response_time_calibration.json");
        runSimulationCheckExitTempOutput(0, f, f2, "-d");
    }
}
