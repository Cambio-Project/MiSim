package cambio.simulator.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.*;

class ReportCollectorTest {

    @AfterEach
    void tearDown() {
        //remove test reporters
        ReportCollector.getInstance().elements().forEach(reporter ->
            ReportCollector.getInstance().deRegister(reporter));
    }

    @AfterAll
    static void tearDownAll() {
        //reset ReportCollector to original state
        ReportCollector.getInstance().reset();
    }

    @Test
    void has_correct_count() {
        new TestReporter(1);
        assertEquals(1, ReportCollector.getInstance().elements().size());
        assertEquals(1, ReportCollector.getInstance().collectData().size());
        new TestReporter(2);
        assertEquals(2, ReportCollector.getInstance().elements().size());
        assertEquals(2, ReportCollector.getInstance().collectData().size());
    }

    @Test
    void collects_output_correctly() {
        new TestReporter(1);
        new TestReporter(2);
        Map<String, TreeMap<Double, Object>> out = ReportCollector.getInstance().collectData();
        TreeMap<Double, Object> dataset1 = out.get("dataset1");
        TreeMap<Double, Object> dataset2 = out.get("dataset2");
        assertTrue(dataset1.containsKey(0.0));
        assertTrue(dataset1.containsKey(1.0));
        assertTrue(dataset2.containsKey(0.0));
        assertTrue(dataset2.containsKey(1.0));
    }

    @Test
    void combines_correctly() {
        new TestReporter(1).addDatapoint("dataset2", new TimeInstant(3), 22); //adds 1 datapoint to dataset2
        new TestReporter(2); //adds 2 datapoints to dataset2
        Map<String, TreeMap<Double, Object>> out = ReportCollector.getInstance().collectData();
        Map<Double, Object> dataset2 = out.get("dataset2");
        assertEquals(3, dataset2.size());
    }

    private static class TestReporter extends MultiDataPointReporter {
        public TestReporter(int id) {
            super();
            addDatapoint("dataset" + id, new TimeInstant(0), 42);
            addDatapoint("dataset" + id, new TimeInstant(1), 81);
        }
    }
}
