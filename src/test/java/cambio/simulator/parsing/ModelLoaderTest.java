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
import cambio.simulator.testutils.FileLoaderUtil;

class ModelLoaderTest {


    @Test
    void loads_MetaDataFromScenario() {
        File scenarioFile = FileLoaderUtil.loadFromTestResources("test_metadata_scenario.json");
        testMetaDataParsing(scenarioFile);
    }

    @Test
    void loads_MetaDataFromExperimentNested() {
        File experimentFile = FileLoaderUtil.loadFromTestResources("test_metadata_experiment_nested.json");
        testMetaDataParsing(experimentFile);
    }

    @Test
    void loads_MetaDataFromExperiment() {
        File experimentFile = FileLoaderUtil.loadFromTestResources("test_metadata_experiment.json");
        testMetaDataParsing(experimentFile);
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
    void parsesTestModels() {
        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_experiment.json");

        MiSimModel model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExperiment");
        model.connectToExperiment(expDummy);
        expDummy.stop(new TimeInstant(0.000001));//lets the experiment start itself for a very short amount of time
        expDummy.setShowProgressBar(false); //enforces headless mode
        
        expDummy.start();
        expDummy.finish();

        assertEquals(5, model.getExperimentModel().getAllSelfSchedulesEntities().size());
    }

    @Test
    void parsesTestModelsWithScenario() {
        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_scenario.json");

        MiSimModel model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExperiment");
        model.connectToExperiment(expDummy);
        expDummy.stop(new TimeInstant(0.000001));//lets the experiment start itself for a very short amount of time
        expDummy.setShowProgressBar(false);

        expDummy.start();
        expDummy.finish();


        assertEquals(7, model.getExperimentModel().getAllSelfSchedulesEntities().size());

    }
}
