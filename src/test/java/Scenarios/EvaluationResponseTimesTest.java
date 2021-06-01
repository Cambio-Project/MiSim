package Scenarios;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
@Disabled
public class EvaluationResponseTimesTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/Evaluation/loon_mock_architecture.json");
        File f2 = new File("./Examples/Evaluation/response_time_experiement.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d", "-p"};
        MainModel.main(args);
    }
}
