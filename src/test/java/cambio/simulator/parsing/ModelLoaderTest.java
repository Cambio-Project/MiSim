package cambio.simulator.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelLoaderTest {


    @Test
    void loads_MetaDataFromScenario() {
        testMetaDataParsing(new File("src/test/resources/test_metadata_scenario.json"));
    }

    @Test
    void loads_MetaDataFromExperimentNested() {
        testMetaDataParsing(new File("src/test/resources/test_metadata_experiment_nested.json"));
    }

    @Test
    void loads_MetaDataFromExperiment() {
        testMetaDataParsing(new File("src/test/resources/test_metadata_experiment.json"));
    }

    private void testMetaDataParsing(File experimentFileLocation) {
        File archFileLocation = new File("derp/derp/derp");
        long currTime = System.currentTimeMillis();
        ExperimentMetaData data = ModelLoader.loadExperimentMetaData(experimentFileLocation, archFileLocation);
        data.markStartOfSetup(currTime);
        long currTime2 = System.currentTimeMillis();
        data.markEndOfSetup(currTime2);

        assertNull(data.getStartTimestamp());
        assertEquals(archFileLocation.getAbsolutePath(), data.getArchFileLocation().getAbsolutePath());
        assertEquals(experimentFileLocation.getAbsolutePath(), data.getExpFileLocation().getAbsolutePath());
        assertEquals("New Experiment", data.getExperimentName());
        assertEquals("Contains examples for the new Experiment format", data.getDescription());
        assertEquals(42, data.getSeed());
        assertEquals(180, data.getDuration());
        assertEquals(currTime2 - currTime, data.getDurationOfSetupMS());
        assertEquals(new File("/Report_42/").getAbsolutePath(), data.getReportLocation().getAbsolutePath());
        assertEquals("continuous", data.getReportType());
    }


    @Test
    void failsOnNullExperimentMetaData_Test() {
        Assertions.assertThrows(ParsingException.class, () -> ModelLoader.loadExperimentMetaData(null, null));
    }

    @Test
    void failsOnNonExistingExperimentMetaData_Test() {
        Assertions
            .assertThrows(ParsingException.class,
                () -> ModelLoader.loadExperimentMetaData(new File("/noneExistingFile.nonefile"), null));
    }


    @Test
    void parsesModels_Test() {
        File test_experiment = new File("src/test/resources/test_experiment.json");
        File test_architecture = new File("src/test/resources/test_architecture.json");
        MiSimModel model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExperiment");
        expDummy.setShowProgressBar(true);
        model.connectToExperiment(expDummy);
        expDummy.stop(new TimeInstant(0.1));

        expDummy.start();
        expDummy.finish();

    }
}