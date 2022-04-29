package cambio.simulator.scenarios.evaluation;

import java.io.File;

import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Evaluation experiments form the bachelors' thesis that developed MiSim 3.0.
 * However, keep in mind that the results shown in the thesis were created by exactly version 3.0.
 *
 * @author Lion Wagner
 */
@Disabled //these tests run quite long, so they are disabled by default
public class Scenarios extends TestBase {

    @Test
    @Deprecated
    void scenario1() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario1.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    void scenario1ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario1.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    @Deprecated
    void scenario2() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario2.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    void scenario2ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario2.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    @Deprecated
    void scenario3() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario3.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    void scenario3ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario3.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    @Deprecated
    void scenario4() {
        File f = new File("./Examples/ProofOfConcept/example_architecture.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario4.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }

    @Test
    void scenario4ScenarioDescription() {
        File f = new File("./Examples/ProofOfConcept/example_architecture.json");
        File f2 = new File("./Examples/Evaluation/scenario4.json");
        runSimulationCheckExit(0, f, f2, "-t");
    }
}
