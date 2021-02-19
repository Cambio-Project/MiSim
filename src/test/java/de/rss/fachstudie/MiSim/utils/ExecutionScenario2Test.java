package de.rss.fachstudie.MiSim.utils;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
public class ExecutionScenario2Test {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/loon_mock_model.json");
        File f2 = new File("./Examples/loon_mock_experiment2_model.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d"};
        MainModel.main(args);
    }
}
