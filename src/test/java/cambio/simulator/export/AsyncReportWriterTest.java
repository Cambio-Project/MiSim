package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.*;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.io.FileUtils;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;

@Timeout(value = 10)
abstract class AsyncReportWriterTest<T extends AsyncReportWriter<?>> extends TestBase {

    Random rng = new Random();
    Path tmpOut;
    Class<T> writerClass;
    private T writer;

    AsyncReportWriterTest() {
        //noinspection unchecked
        writerClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }


    @BeforeEach
    void setUp() throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException,
        InvocationTargetException {
        tmpOut = createSelfDeletingTempOutputDir().toPath();
        writer = writerClass.getConstructor(Path.class).newInstance(tmpOut.resolve("test.csv"));
    }

    @AfterEach
    protected void tearDown() throws IOException {
        writer.finalizeWriteout();
        FileUtils.forceDelete(tmpOut.toFile());
        super.tearDown();
    }

    @Test
    void writesDefaultHeader() throws IOException {
        writer.finalizeWriteout();
        File out = tmpOut.resolve("test.csv").toFile();
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals(MiSimReporters.DEFAULT_TIME_COLUMN_NAME
                + MiSimReporters.csvSeperator
                + MiSimReporters.DEFAULT_VALUE_COLUMN_NAME,
            Files.readAllLines(out.toPath()).get(0).trim());
    }

    @RepeatedTest(20)
    void writesCorrectNumberOfLines() throws IOException {
        int numLines = rng.nextInt(100) + 1;

        for (int i = 0; i < numLines; i++) {
            writer.addDataPoint(i, i);
        }

        writer.finalizeWriteout();
        File out = tmpOut.resolve("test.csv").toFile();
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals(Files.readAllLines(out.toPath()).size(), numLines + 1);
    }


    /**
     * This test tries to find errors in multi-threaded data writing code. Therefore it can by flakey if there is a bug.
     * Hence it is repeated multiple times.
     */
    @SuppressWarnings("ConstantConditions")
    @RepeatedTest(20)
    void hasWellFormedOutput() throws IOException {
        File arch = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File exp = FileLoaderUtil.loadFromTestResources("test_experiment.json");
        Pair<MiSimModel, TestExperiment> mocks = getConnectedMockModel(arch, exp);
        MiSimModel model = mocks.getValue0();
        TestExperiment experiment = mocks.getValue1();

        experiment.stop(new TimeInstant(1));

        new SimulationEndEvent(model, "SimulationEnd", true).schedule(new TimeInstant(1));

        experiment.start();
        experiment.finish();


        File dataLocation = model.getExperimentMetaData().getReportLocation().resolve("raw").toFile();
        Assertions.assertTrue(dataLocation.exists());
        Assertions.assertTrue(dataLocation.isDirectory());
        Assertions.assertNotNull(dataLocation.listFiles());
        Assertions.assertTrue(dataLocation.listFiles().length > 0);


        boolean anyFailures = Arrays.stream(dataLocation.listFiles())
            .parallel()
            .filter(File::isFile)
            .filter(file -> file.getPath().endsWith(".csv"))
            .filter(file -> !assertIsCSVFormat(file))
            .anyMatch(file -> true);

        Assertions.assertFalse(anyFailures);
    }

    private boolean assertIsCSVFormat(File file) {
        //check whether file has csv format
        String sep = MiSimReporters.csvSeperator;
        AtomicInteger lineCount = new AtomicInteger();
        try {
            return Files.readAllLines(file.toPath()).stream().map(String::trim).allMatch(line -> {
                boolean matches = true;
                lineCount.getAndIncrement();

                if (!line.isEmpty()) {
                    String[] split = line.split(sep);
                    if (split.length < 2 || Arrays.stream(split).anyMatch(String::isEmpty)) {
                        matches = false;
                    }
                }

                if (!matches) {
                    System.out.println(
                        "File " + file.getName() + " has invalid line " + lineCount.get() + ": \"" + line + "\"");
                }

                return matches;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
