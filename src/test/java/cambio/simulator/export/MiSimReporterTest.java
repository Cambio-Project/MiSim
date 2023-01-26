package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import cambio.simulator.test.TestBase;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
public abstract class MiSimReporterTest<T extends MiSimReporter<?>> extends TestBase {
    protected final static Random RNG = new Random(157);
    protected T reporter;
    protected File outputFile;
    private Path outputDir;

    @BeforeEach
    void setUp() {
        outputDir = reporter.model.getExperimentMetaData().getReportLocation();
        outputFile = outputDir.resolve("raw").resolve("test.csv").toFile();
    }

    protected void registersDefaultHeader() {
        reporter.registerDefaultHeader("test", "test1", "test2", "test3");
        reporter.addDatapoint("test", new TimeInstant(0), "test1", "test2", "test3");
        reporter.finalizeReport();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
