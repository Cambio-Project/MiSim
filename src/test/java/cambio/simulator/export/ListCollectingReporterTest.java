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

class ListCollectingReporterTest extends MiSimReporterTest<ListCollectingReporter> {

    @BeforeEach
    void setUp() {
        reporter = new ListCollectingReporter(getMockModel());
        super.setUp();
    }

    @Test
    void doesCollectCorrectly() throws IOException {
        List<List<Integer>> entries = new ArrayList<>();

        for (int i = 0; i < RNG.nextInt(1000) + 1; i++) {

            List<Integer> entries_tmp = new ArrayList<>();
            for (int j = 0; j < RNG.nextInt(1000) + 1; j++) {
                int value = RNG.nextInt();
                reporter.addDatapoint("test", new TimeInstant(i), value);
                entries_tmp.add(value);
            }
            entries.add(entries_tmp);
        }
        reporter.finalizeReport();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        List<String> lines = Files.readAllLines(outputFile.toPath());
        for (int i = 1; i < lines.size(); i++) {
            String[] split = lines.get(i).split(MiSimReporters.csvSeperator, 2);
            assertEquals(String.valueOf((double) (i - 1)), split[0], "Line " + i + " has wrong time");
            String[] values = split[1].substring(1, split[1].length() - 1).split(MiSimReporters.csvSeperator);
            for (int j = 0; j < values.length; j++) {
                assertEquals(entries.get(i - 1).get(j), Integer.parseInt(values[j]), "Line " + i + " has wrong value");
            }
        }
    }
}
