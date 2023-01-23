package cambio.simulator.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.*;
import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class AsyncReportWriterTest extends TestBase {


    /**
     * This test tries to find errors in multi-threaded data writing code. Therefore it can by flakey if there is a bug.
     * Hence it is repeated multiple times.
     */
    @SuppressWarnings("ConstantConditions")
    @RepeatedTest(10)
    void hasWellFormedOutput() {
        File arch = FileLoaderUtil.loadFromTestResources("test_architecture.json");
        File exp = FileLoaderUtil.loadFromTestResources("test_experiment.json");
        Pair<MiSimModel, TestExperiment> mocks = getConnectedMockModel(arch, exp);
        MiSimModel model = mocks.getValue0();
        TestExperiment experiment = mocks.getValue1();

        experiment.stop(new TimeInstant(5));

        new SimulationEndEvent(model, "SimulationEnd", true).schedule(new TimeInstant(5));

        experiment.start();
        experiment.finish();

        ReportCollector.getInstance().printReport(model);

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