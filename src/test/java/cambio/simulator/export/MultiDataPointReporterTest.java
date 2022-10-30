package cambio.simulator.export;

import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class MultiDataPointReporterTest {

    private MultiDataPointReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new MultiDataPointReporter(null);
    }


    @Test
    void multi_type_compatibility() {
        reporter.addDatapoint("Test", new TimeInstant(0), 42);
        reporter.addDatapoint("Test", new TimeInstant(1), "Hello");
        reporter.addDatapoint("Test2", new TimeInstant(1), 1337.2f);
        reporter.addDatapoint("Test3", new TimeInstant(1), new Pair<>(new TimeInstant(42), 54.3));
    }

}