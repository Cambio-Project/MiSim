package cambio.simulator.export;

import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;


class ReportCollectorTest {

    @BeforeEach
    void setUp() {
        MiSimReporters.finalizeReports();
    }

    private static class TestReporter extends MultiDataPointReporter {
        public TestReporter(int id) {
            super(null);
            addDatapoint("dataset" + id, new TimeInstant(0), 42);
            addDatapoint("dataset" + id, new TimeInstant(1), 81);
        }
    }
}