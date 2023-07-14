package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class AsyncListReportWriterTest extends AsyncReportWriterTest<AsyncListReportWriter> {

    @Test
    void hasCorrectFormatWithSingleEntry() throws IOException {
        writesCorrectNumberOfLines();
        checkFormat("test.csv");
    }

    @RepeatedTest(20)
    void hasCorrectFormatWithMultipleEntries() throws IOException {
        String datasetName = "hasCorrectFormatWithMultipleEntries.csv";
        AsyncReportWriter<?> writer = new AsyncListReportWriter(tmpOut.resolve(datasetName));

        int numLines = rng.nextInt(100) + 1;
        List<Integer> entryLengths = new ArrayList<>();
        for (int i = 0; i < numLines; i++) {
            int numEntries = Math.max(1, rng.nextInt(100));
            entryLengths.add(numEntries);

            for (int j = 0; j < numEntries; j++) {
                writer.addDataPoint(i, j);
            }
        }

        writer.finalizeWriteout();

        File out = tmpOut.resolve(datasetName).toFile();

        List<String> lines = Files.readAllLines(out.toPath());

        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals(lines.size(), numLines + 1);

        checkFormat(datasetName);


        for (int i = 1; i < lines.size(); i++) {
            int expectedEntries = entryLengths.get(i - 1);
            int actualEntryCount = lines.get(i).split(MiSimReporters.csvSeperator, 2)[1].split(";").length;
            assertEquals(expectedEntries, actualEntryCount,
                "Line " + i + " has wrong number of entries. Expected: " + expectedEntries + " Actual: " +
                    actualEntryCount);

        }
    }


    private void checkFormat(String datasetName) throws IOException {
        File out = tmpOut.resolve(datasetName).toFile();

        for (String line : Files.readAllLines(out.toPath())) {
            if (line.startsWith("SimulationTime")) {
                continue;
            }
            //line should have the form of "time; [value(;value)*]"
            Pattern p = Pattern.compile("\\d+(\\.\\d+)?;\\[[\\d;]*]");
            assertTrue(p.matcher(line).matches(), "Line '" + line + "' does not match pattern" + p.pattern());
        }
    }
}
