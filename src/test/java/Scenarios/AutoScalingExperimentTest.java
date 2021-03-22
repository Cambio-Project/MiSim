package Scenarios;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
public class AutoScalingExperimentTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/loon_mock_architecture_scaling.json");
        File f2 = new File("./Examples/loon_mock_experiment_autoscale.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d"};
        MainModel.main(args);
    }
}
