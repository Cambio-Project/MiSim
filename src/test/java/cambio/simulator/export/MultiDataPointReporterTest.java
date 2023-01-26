package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;

class MultiDataPointReporterTest extends MiSimReporterTest<MultiDataPointReporter> {

    @BeforeEach
    void setUp() {
        reporter = new MultiDataPointReporter(getMockModel());
        super.setUp();
    }

    @Test
    void multi_type_compatibility() {
        reporter.addDatapoint("Test", new TimeInstant(0), 42);
        reporter.addDatapoint("Test", new TimeInstant(1), "Hello");
        reporter.addDatapoint("Test2", new TimeInstant(1), 1337.2f);
        reporter.addDatapoint("Test3", new TimeInstant(1), new Pair<>(new TimeInstant(42), 54.3));
    }

    @Test
    void doesWriteCorrectly() throws IOException {
        List<Integer> entries = new ArrayList<>();

        for (int i = 0; i < RNG.nextInt(1000) + 1; i++) {
            int value = RNG.nextInt();
            reporter.addDatapoint("test", new TimeInstant(i), value);
            entries.add(value);
        }
        reporter.finalizeReport();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        List<String> lines = Files.readAllLines(outputFile.toPath());
        for (int i = 1; i < lines.size(); i++) {
            String[] split = lines.get(i).split(MiSimReporters.csvSeperator, 2);
            assertEquals(String.valueOf((double) (i - 1)), split[0], "Line " + i + " has wrong time");
            assertEquals(entries.get(i - 1), Integer.parseInt(split[1]), "Line " + i + " has wrong value");
        }
    }

    @Test
    void registersHeaders() throws IOException {
        super.registersDefaultHeader();

        List<String> lines = Files.readAllLines(outputFile.toPath());
        String[] split = lines.get(0).split(MiSimReporters.csvSeperator);
        assertEquals("test1", split[1]);
        assertEquals("test2", split[2]);
        assertEquals("test3", split[3]);
    }
}
