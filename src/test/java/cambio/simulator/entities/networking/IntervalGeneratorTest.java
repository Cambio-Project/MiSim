package cambio.simulator.entities.networking;

import cambio.simulator.entities.generator.IntervalGenerator;
import cambio.simulator.models.MainModel;
import desmoj.core.simulator.Experiment;
import org.junit.jupiter.api.BeforeAll;

class IntervalGeneratorTest {

    private static IntervalGenerator generator = null;

    @BeforeAll
    public static void before_all() {
        MainModel model = new MainModel(null, "TestModel", true, true);
        Experiment exp = new Experiment("Test Experiment");
        model.connectToExperiment(exp);

        generator = new IntervalGenerator(model, "Generator X", true, null, 100);

    }


}