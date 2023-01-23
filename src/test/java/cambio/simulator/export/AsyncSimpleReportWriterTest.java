package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.*;

class AsyncSimpleReportWriterTest extends TestBase {

    Random rng = new Random(42);
    Path tmpOut;
    AsyncReportWriter<?> writer;

    @BeforeEach
    void setUp() throws IOException {
        tmpOut = createTempOutputDir().toPath();
        writer = new AsyncSimpleReportWriter(tmpOut.resolve("test.csv"), "Value");
    }

    @AfterEach
    void tearDown() {
        try {
            writer.finalizeWriteout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void fixesFileExtension() throws IOException {
        writer = new AsyncSimpleReportWriter(tmpOut.resolve("test"), "Value");
        Assertions.assertTrue(writer.datasetPath.toString().endsWith(".csv"));
    }

    @Test
    void throwsExceptionOnInvalidPath() {
        assertThrows(IOException.class,
            () -> new AsyncSimpleReportWriter(tmpOut.resolve(Paths.get("test", "test.csv")), "Value"));
    }

    @Test
    void writesHeader() throws IOException {
        writer.finalizeWriteout();
        File out = tmpOut.resolve("test.csv").toFile();
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals("SimulationTime" + MiSimReporters.csvSeperator + "Value",
            Files.readAllLines(out.toPath()).get(0).trim());
    }

    @RepeatedTest(10)
    void writesCorrectNumberOfLines() throws IOException {
        int numLines = rng.nextInt(101);
        for (int i = 0; i < numLines; i++) {
            writer.addDataPoint(i, i);
        }
        writer.finalizeWriteout();
        File out = tmpOut.resolve("test.csv").toFile();
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
        assertEquals(Files.readAllLines(out.toPath()).size(), numLines + 1);
    }
}