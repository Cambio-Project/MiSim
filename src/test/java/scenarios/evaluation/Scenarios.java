package scenarios.evaluation;

import java.io.File;

import cambio.simulator.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
public class Scenarios {

    @Test
    void scenario1() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario1.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario1ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario1.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-s", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario2() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario2.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario2ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario2.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-s", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario3() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario3.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario3ScenarioDescription() {
        File f = new File("./Examples/Evaluation/calibrated_arch.json");
        File f2 = new File("./Examples/Evaluation/scenario3.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-s", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario4() {
        File f = new File("./Examples/Calibration/example_architecture.json");
        File f2 = new File("./Examples/Evaluation/ExperimentScenario4.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }

    @Test
    void scenario4ScenarioDescription() {
        File f = new File("./Examples/Calibration/example_architecture.json");
        File f2 = new File("./Examples/Evaluation/scenario4.json");
        String[] args = new String[] {"-a", f.getAbsolutePath(), "-s", f2.getAbsolutePath(), "-d", "-p"};
        MainModel.main(args);
    }
}
