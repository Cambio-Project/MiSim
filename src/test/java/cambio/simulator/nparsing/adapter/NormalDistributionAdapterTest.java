package cambio.simulator.nparsing.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.simulator.Experiment;
import org.junit.jupiter.api.Test;

class NormalDistributionAdapterTest {

    private static final MiSimModel model;

    private static class NormalDistTest {
        public ContDistNormal dist;
    }

    static {
        File test_experiment = new File("src/test/resources/test_experiment.json");
        File test_architecture = new File("src/test/resources/test_architecture.json");
        model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExp");
        model.connectToExperiment(expDummy);
    }

    @Test
    void CreatesConstantDistribution() {
        executeConstantDistTest("5", 5);
    }

    @Test
    void CreatesConstantDistribution_DotNotation() {
        executeConstantDistTest("5.0", 5);
    }

    @Test
    void CreatesConstantDistribution_DotNotationNoInt() {
        executeConstantDistTest(".5", 0.5);
    }

    @Test
    void CreatesConstantDistribution_NoDeviation() {
        executeConstantDistTest("5+-0", 5);
        executeConstantDistTest("5+0-0", 5);
    }

    private void executeConstantDistTest(String input, double expectedValue) {
        ContDistNormal dist = grabParsedValue(input);
        for (int i = 0; i < 20; i++) {
            assertEquals(expectedValue, dist.sample());
        }
    }


    @Test
    void failsOnNull() {
        assertThrows(ParsingException.class, () -> grabParsedValue(null));
    }

    @Test
    void failsOnEmptyString() {
        assertThrows(ParsingException.class, () -> grabParsedValue(""));
        assertThrows(ParsingException.class, () -> grabParsedValue(" "));
        assertThrows(ParsingException.class, () -> grabParsedValue("\t"));
        assertThrows(ParsingException.class, () -> grabParsedValue("\n"));
    }

    private ContDistNormal grabParsedValue(String inputValue) {
        String inputString = String.format("{dist : \"%s\" }", inputValue);

        NormalDistributionAdapter adapter = new NormalDistributionAdapter(model);
        Gson gson = new GsonBuilder().registerTypeAdapter(ContDistNormal.class, adapter).create();
        return gson.fromJson(inputString, NormalDistTest.class).dist;
    }

}