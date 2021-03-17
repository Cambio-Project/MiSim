package Scenarios;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
public class ChaosMonkeyExperimentTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/loon_mock_architecture_model.json");
        File f2 = new File("./Examples/loon_mock_experiment_chaosmonkey.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d"};
        MainModel.main(args);
    }
}
