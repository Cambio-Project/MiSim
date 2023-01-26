package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccumulativeDataPointReporterTest extends MiSimReporterTest<AccumulativeDataPointReporter> {

    @BeforeEach
    void setUp() {
        reporter = new AccumulativeDataPointReporter(getMockModel());
        super.setUp();
    }

    @Test
    void doesAccumulateCorrectly() throws IOException {
        List<Double> sums = new ArrayList<>();

        for (int i = 0; i < RNG.nextInt(1000) + 1; i++) {
            double sum = 0;
            for (int j = 0; j < RNG.nextInt(1000) + 1; j++) {
                double value = RNG.nextDouble();
                reporter.addDatapoint("test", new TimeInstant(i), value);
                sum += value;
            }
            sums.add(sum);
        }
        reporter.finalizeReport();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        List<String> lines = Files.readAllLines(outputFile.toPath());
        for (int i = 1; i < lines.size(); i++) {
            String[] split = lines.get(i).split(MiSimReporters.csvSeperator, 2);
            assertEquals(String.valueOf((double) (i - 1)), split[0], "Line " + i + " has wrong time");
            assertEquals(sums.get(i - 1), Double.parseDouble(split[1]), 0.00000001, "Line " + i + " has wrong sum");
        }
    }

    @Test
    void writesASingleHeader() throws IOException {
        reporter.registerDefaultHeader("test", "test1", "test2", "test3");
        reporter.addDatapoint("test", new TimeInstant(0), 1);
        reporter.finalizeReport();

        List<String> lines = Files.readAllLines(outputFile.toPath());
        String[] split = lines.get(0).split(MiSimReporters.csvSeperator);
        assertEquals("test1", split[1]);

        assertEquals(split.length, 2); //no other headers
    }

    @Test
    void throwsOperationUnsupportedExceptionOnNonNumber() {
        assertThrows(UnsupportedOperationException.class, () -> reporter.addDatapoint("test", new TimeInstant(0),
            "test1", "test2", "test3"));
        assertThrows(UnsupportedOperationException.class, () -> reporter.addDatapoint("test", new TimeInstant(0),
            "test1", "test2", 3));
    }

    @Test
    void acceptsNumbersOnDefaultAddDatapoint(){
        assertDoesNotThrow(() -> reporter.addDatapoint("test", new TimeInstant(0), 2, 3, 5));
        assertDoesNotThrow(() -> reporter.addDatapoint("test", new TimeInstant(0), 2, 3.0, 5));
        assertDoesNotThrow(() -> reporter.addDatapoint("test", new TimeInstant(0), 2.0, 3.0, 5.0));
        assertDoesNotThrow(() -> reporter.addDatapoint("test", new TimeInstant(0), 2.0f, 3.0f, 5.0));
    }
}
