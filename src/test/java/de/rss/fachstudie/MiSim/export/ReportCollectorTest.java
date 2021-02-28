package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportCollectorTest {

    private static class TestReporter extends MultiDataPointReporter {
        public TestReporter(int id) {
            super();
            addDatapoint("dataset" + id, new TimeInstant(0), 42);
            addDatapoint("dataset" + id, new TimeInstant(1), 81);
        }
    }


    @BeforeEach
    void setUp() {
        ReportCollector.getInstance().elements().forEach(reporter ->
                ReportCollector.getInstance().deRegister(reporter));
    }

    @Test
    void has_correct_count() {
        new TestReporter(1);
        assertEquals(1, ReportCollector.getInstance().elements().size());
        assertEquals(1, ReportCollector.getInstance().collect_data().size());
        new TestReporter(2);
        assertEquals(2, ReportCollector.getInstance().elements().size());
        assertEquals(2, ReportCollector.getInstance().collect_data().size());
    }

    @Test
    void collects_output_correctly() {
        new TestReporter(1);
        new TestReporter(2);
        HashMap<String, TreeMap<TimeInstant, Object>> out = ReportCollector.getInstance().collect_data();
        Map<TimeInstant, Object> dataset1 = out.get("dataset1");
        Map<TimeInstant, Object> dataset2 = out.get("dataset2");
        assertTrue(dataset1.containsKey(new TimeInstant(0)));
        assertTrue(dataset1.containsKey(new TimeInstant(1)));
        assertTrue(dataset2.containsKey(new TimeInstant(0)));
        assertTrue(dataset2.containsKey(new TimeInstant(1)));
    }

    @Test
    void combines_correctly() {
        new TestReporter(1).addDatapoint("dataset2", new TimeInstant(3), 22); //adds 1 datapoint to dataset2
        new TestReporter(2); //adds 2 datapoints to dataset2
        HashMap<String, TreeMap<TimeInstant, Object>> out = ReportCollector.getInstance().collect_data();
        Map<TimeInstant, Object> dataset2 = out.get("dataset2");
        assertEquals(3, dataset2.size());
    }
}