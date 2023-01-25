package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
