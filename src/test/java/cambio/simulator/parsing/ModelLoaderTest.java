package cambio.simulator.parsing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Path;

import cambio.simulator.export.ExportUtils;
import cambio.simulator.misc.RNGStorage;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class ModelLoaderTest extends TestBase {

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
        ExperimentMetaData data = ModelLoader.loadExperimentMetaData(experimentFileLocation, archFileLocation);

        assertNull(data.getStartTimestamp());
        assertEquals(archFileLocation.getAbsolutePath(), data.getArchitectureDescriptionLocation().getAbsolutePath());
        assertEquals(experimentFileLocation.getAbsolutePath(),
            data.getExperimentDescriptionLocation().getAbsolutePath());
        assertEquals("New Experiment", data.getExperimentName());
        assertEquals("Contains examples for the new Experiment format", data.getDescription());
        assertEquals(42, data.getSeed());
        assertEquals(180, data.getDuration());
        assertEquals(new File("./Report_42/").getAbsolutePath(),
            data.getReportBaseDirectory().toAbsolutePath().toString());
        assertEquals("continuous", data.getReportType());
    }


    @Test
    void failsOnNullExperimentMetaData_Test() {
        assertThrows(ParsingException.class, () -> ModelLoader.loadExperimentMetaData(null, null));
    }

    @Test
    void failsOnNonExistingExperimentMetaData_Test() {
        assertThrows(ParsingException.class,
            () -> ModelLoader.loadExperimentMetaData(new File("/noneExistingFile.nonefile"), null));
    }


    @Test
    void parsesTestModelsWithExperiment() throws IOException {
        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_experiment.json");

        MiSimModel model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExperiment");

        Path reportLocation = TestBase.createTempOutputDir();
        ExperimentMetaData metaData = model.getExperimentMetaData();
        metaData.setReportLocation(reportLocation);
        ExportUtils.prepareReportDirectory(null, model.getExperimentMetaData());

        model.connectToExperiment(expDummy);
        expDummy.stop(new TimeInstant(1));//lets the experiment start itself for a very short amount of time
        expDummy.setShowProgressBar(false); //enforces headless mode

        expDummy.start();
        expDummy.finish();

        assertEquals(5, model.getExperimentModel().getAllSelfSchedulesEntities().size());
        assertFalse(expDummy.hasError());

        RNGStorage.reset();
        FileUtils.forceDelete(reportLocation.toFile());
    }

    @Test
    void parsesTestModelsWithScenario() throws IOException {
        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_scenario.json");


        MiSimModel model = new MiSimModel(test_architecture, test_experiment);
        Experiment expDummy = new Experiment("TestExperiment");

        Path reportLocation = TestBase.createTempOutputDir();
        ExperimentMetaData metaData = model.getExperimentMetaData();
        metaData.setReportLocation(reportLocation);
        ExportUtils.prepareReportDirectory(null, model.getExperimentMetaData());

        model.connectToExperiment(expDummy);
        expDummy.stop(new TimeInstant(1));//lets the experiment start itself for a very short amount of time
        expDummy.setShowProgressBar(false);

        expDummy.start();
        expDummy.finish();

        assertEquals(7, model.getExperimentModel().getAllSelfSchedulesEntities().size());
        assertFalse(expDummy.hasError());

        RNGStorage.reset();
        FileUtils.forceDelete(reportLocation.toFile());
    }

    @Test
    void throwsWarningWhenSimulationDurationIsMissing() throws UnsupportedEncodingException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(out, false, "UTF-8");

        System.setOut(newOut);

        File test_architecture = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File test_experiment = FileLoaderUtil.loadFromTestResources("test_scenario_infinite.json");
        getConnectedMockModel(test_architecture, test_experiment);

        newOut.flush();
        System.setOut(originalOut);

        assertTrue(out.toString("UTF-8")
            .contains("[Warning] Simulation duration is not set or infinite. The simulation may runs infinitely."));

    }
}
