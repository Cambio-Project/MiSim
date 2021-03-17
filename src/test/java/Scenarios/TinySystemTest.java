package Scenarios;

import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import testutils.TestExperiment;
import testutils.TestModel;
import testutils.TestUtils;

/**
 * @author Lion Wagner
 */
public class TinySystemTest {

    @Test
    void retryTest() {
        TestModel model = new TestModel(null, null, false, false, () -> {
        }, () -> {
        });
        TestExperiment experiment = new TestExperiment();
        model.connectToExperiment(experiment);
        NumericalDist<Double> dist = new ContDistNormal(model, null, 1, 0, false, false);//std=0 produces distribution anyways?
        for (int i = 0; i < 100; i++) {
            System.out.println(dist.sample());
        }
    }

    @Test
    void name() {
        Pair<Model, Experiment> testpair = TestUtils.getExampleExperiment(5);

    }
}
