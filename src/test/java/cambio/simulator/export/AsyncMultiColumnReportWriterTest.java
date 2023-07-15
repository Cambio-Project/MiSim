package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class AsyncMultiColumnReportWriterTest extends AsyncReportWriterTest<AsyncMultiColumnReportWriter> {

    @Test
    public void writesHeaders() throws IOException {
        String datasetName = "writesHeaders.csv";
        AsyncReportWriter<?> writer =
            new AsyncMultiColumnReportWriter(tmpOut.resolve(datasetName), "a", "b", "c");
        writer.finalizeWriteout();

        File out = tmpOut.resolve(datasetName).toFile();
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals(MiSimReporters.DEFAULT_TIME_COLUMN_NAME
                + MiSimReporters.csvSeperator + "a"
                + MiSimReporters.csvSeperator + "b"
                + MiSimReporters.csvSeperator + "c",
            Files.readAllLines(out.toPath()).get(0).trim());
    }


    @Test
    public void createsCorrectFormat() throws IOException {
        String datasetName = "createsCorrectFormat.csv";
        AsyncMultiColumnReportWriter writer =
            new AsyncMultiColumnReportWriter(tmpOut.resolve(datasetName));
        writer.addDataPoint(0, 42);
        writer.addDataPoint(1, new int[] {1, 1, 1});
        writer.addDataPoint(2.3, new double[] {2, 2, 2});
        writer.addDataPoint(3.55, new float[] {3, 3, 3});
        writer.addDataPoint(5.55, Arrays.asList(5, 5, 5));
        writer.addDataPoint(6.22, new ArrayList<>(Arrays.asList(6, 6, 6)));
        writer.addDataPoint(7.22, new long[] {7, 7, 7});
        writer.addDataPoint(8.22, new String[] {"8", "8", "8"});
        writer.addDataPoint(9.22, new Object[] {9, 9, 9});
        writer.addDataPoint(10.22, new char[] {'1', '0', '0'});
        writer.addDataPoint(11.22, new byte[] {1, 127, 42});
        writer.addDataPoint(12.22, new short[] {1, 2, 3});
        writer.addDataPoint(13.22, new boolean[] {true, false, true});
        writer.finalizeWriteout();

        checkFormat(datasetName);
    }


    private void checkFormat(String datasetName) throws IOException {
        File out = tmpOut.resolve(datasetName).toFile();

        for (String line : Files.readAllLines(out.toPath())) {
            if (line.startsWith(MiSimReporters.DEFAULT_TIME_COLUMN_NAME)) {
                continue;
            }
            //line should have the form of "time(;<number>|true|false)*"
            Pattern p = Pattern.compile("\\d+(\\.\\d+)?(;(\\d+(\\.\\d+)?|true|false))*");
            assertTrue(p.matcher(line).matches(), "Line '" + line + "' does not match pattern" + p.pattern());
        }
    }

    @Test
    public void createsStringEntriesCorrectly() throws IOException {
        String datasetName = "createsStringEntriesCorrectly.csv";
        AsyncReportWriter<?> writer =
            new AsyncMultiColumnReportWriter(tmpOut.resolve(datasetName));
        writer.addDataPoint(1.0, new String[] {"a", "b", "c"});
        writer.addDataPoint(2, "HelloWorld");
        writer.addDataPoint(3, new Object[] {"abc", "def", "ghi"});
        writer.finalizeWriteout();

        String expected =
            MiSimReporters.DEFAULT_TIME_COLUMN_NAME + MiSimReporters.csvSeperator +
                MiSimReporters.DEFAULT_VALUE_COLUMN_NAME + System.lineSeparator()
                + "1.0" + MiSimReporters.csvSeperator + "a" + MiSimReporters.csvSeperator + "b" +
                MiSimReporters.csvSeperator + "c" + System.lineSeparator()
                + "2.0" + MiSimReporters.csvSeperator + "HelloWorld" + System.lineSeparator()
                + "3.0" + MiSimReporters.csvSeperator + "abc" + MiSimReporters.csvSeperator + "def" +
                MiSimReporters.csvSeperator + "ghi" + System.lineSeparator();

        File out = tmpOut.resolve(datasetName).toFile();
        List<String> lines = Files.readAllLines(out.toPath());
        for (int i = 0; i < lines.size(); i++) {
            assertEquals(expected.split(System.lineSeparator())[i], lines.get(i));
        }
    }
}
