package Scenarios;

import de.rss.fachstudie.MiSim.models.MainModel;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Lion Wagner
 */
public class RealLoonTest {

    @Test
    void ExampleExecutionRun() {
        File f = new File("./Examples/architecutre_real_loon.json");
        File f2 = new File("./Examples/loon_experiment.json");
        String[] args = new String[]{"-a", f.getAbsolutePath(), "-e", f2.getAbsolutePath() , "-d"};
        MainModel.main(args);
    }
}
