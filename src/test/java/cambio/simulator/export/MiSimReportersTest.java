package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.TestBase;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class MiSimReportersTest {

    private static MiSimModel model;
    private static Path dummyDir;

    @BeforeAll
    static void setUp() {
        dummyDir = TestBase.createTempOutputDir();
        model = mock(MiSimModel.class);
        ExperimentMetaData metaData = mock(ExperimentMetaData.class);
        Mockito.when(model.getExperimentMetaData()).thenReturn(metaData);
        Mockito.when(metaData.getReportLocation()).thenReturn(dummyDir);
    }


    @AfterAll
    static void tearDownAll() throws IOException {
        FileUtils.forceDelete(dummyDir.toFile());
    }


    @AfterEach
    void tearDown() {
        MiSimReporters.getReporters().forEach(MiSimReporter::finalizeReport);
    }


    @Test
    void has_correct_count() {
        new TestReporter("1");
        assertEquals(1, MiSimReporters.getReporters().size());
        new TestReporter("2");
        assertEquals(2, MiSimReporters.getReporters().size());
    }

    @Test
    void initializes_static_reporters_correctly() {
        MiSimReporters.finalizeReports();//clears exising reporters
        assertEquals(0, MiSimReporters.getReporters().size());

        MiSimReporters.initializeStaticReporters(model);
        assertEquals(5, MiSimReporters.getReporters().size());
    }

    @Test
    void can_finalize_twice() {
        TestReporter reporter = new TestReporter("1");
        MiSimReporters.getReporters().forEach(MiSimReporter::finalizeReport);
        //the reporter is now deregistered so we  have to register it again
        MiSimReporters.registerReporter(reporter);

        assertEquals(1, MiSimReporters.getReporters().size());
        MiSimReporters.getReporters().forEach(MiSimReporter::finalizeReport);
        assertEquals(0, MiSimReporters.getReporters().size());
    }

    @Test
    void deregisters_on_finalization() {
        new TestReporter("1");
        MiSimReporters.getReporters().forEach(MiSimReporter::finalizeReport);
        assertEquals(0, MiSimReporters.getReporters().size());


        new TestReporter("1");
        MiSimReporters.finalizeReports();
        assertEquals(0, MiSimReporters.getReporters().size());
    }


    @Test
    void writesOutput_correctly() throws IOException {
        new TestReporter("_test");
        MiSimReporters.getReporters().forEach(MiSimReporter::finalizeReport);

        File output = dummyDir.resolve("raw").resolve("dataset_test.csv").toFile();
        List<String> content = Files.readAllLines(output.toPath());

        Assertions.assertTrue(output.exists());
        Assertions.assertTrue(output.isFile());
        Assertions.assertTrue(output.length() > 0);
        Assertions.assertEquals(3, content.size());
        Assertions.assertEquals("0.0" + MiSimReporters.csvSeperator + "42", content.get(1));
        Assertions.assertEquals("1.0" + MiSimReporters.csvSeperator + "81", content.get(2));

    }

    private static class TestReporter extends MultiDataPointReporter {

        public TestReporter(String id) {
            super(MiSimReportersTest.model);
            addDatapoint("dataset" + id, new TimeInstant(0), 42);
            addDatapoint("dataset" + id, new TimeInstant(1), 81);
        }
    }
}
