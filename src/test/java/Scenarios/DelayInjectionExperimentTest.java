package Scenarios;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
@Disabled
public class DelayInjectionExperimentTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/example_architecture_model.json");
        File f2 = new File("./Examples/example_experiment_delayInjection.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d"};
        MainModel.main(args);
    }
}
