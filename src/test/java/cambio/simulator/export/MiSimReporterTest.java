package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;

import cambio.simulator.misc.RNGStorage;
import cambio.simulator.test.TestBase;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Lion Wagner
 */
public abstract class MiSimReporterTest<T extends MiSimReporter<?>> extends TestBase {
    protected final static Random RNG = new Random();
    protected T reporter;
    protected File outputFile;
    private Path outputDir;

    @BeforeEach
    protected void setUp() {
        outputDir = reporter.model.getExperimentMetaData().getReportLocation();
        outputFile = outputDir.resolve("raw").resolve("test.csv").toFile();
        RNGStorage.reset();
    }

    protected void registersDefaultHeader() {
        reporter.registerDefaultHeader("test", "test1", "test2", "test3");
        reporter.addDatapoint("test", new TimeInstant(0), "test1", "test2", "test3");
        reporter.finalizeReport();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
