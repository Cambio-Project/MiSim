package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

class MultiDataPointReporterTest {

    private MultiDataPointReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new MultiDataPointReporter();
    }

    @Test
    void registersReport() {
        reporter.addDatapoint("Hallo", new TimeInstant(0), 42);
        Assertions.assertTrue(reporter.getDataSets().size() > 0);
    }

    @Test
    void sorts_dataset_correctly() {
        reporter.addDatapoint("Test", new TimeInstant(1), "World");
        reporter.addDatapoint("Test", new TimeInstant(0), "Hello");
        TreeMap<Double, ?> dataset = reporter.getDataSets().get("Test");
        Assertions.assertEquals(dataset.pollFirstEntry().getKey(), new TimeInstant(0));
        Assertions.assertEquals(dataset.pollFirstEntry().getKey(), new TimeInstant(1));
    }

    @Test
    void creates_dataset_output_correctly() {
        reporter.addDatapoint("Test", new TimeInstant(1), "World");
        reporter.addDatapoint("Test", new TimeInstant(0), "Hello");
        String[] result = reporter.getEntries("Test");
        Assertions.assertEquals("Time;Value", result[0]);
        Assertions.assertEquals(TimeOperations.getTimeFormatter().buildTimeString(new TimeInstant(0)) + ";Hello", result[1]);
        Assertions.assertEquals(TimeOperations.getTimeFormatter().buildTimeString(new TimeInstant(1)) + ";World", result[2]);
    }

    @Test
    void multi_type_compatibility() {
        reporter.addDatapoint("Test", new TimeInstant(0), 42);
        reporter.addDatapoint("Test", new TimeInstant(1), "Hello");
        reporter.addDatapoint("Test2", new TimeInstant(1), 1337.2f);
        reporter.addDatapoint("Test3", new TimeInstant(1), new Pair<>(new TimeInstant(42), 54.3));
    }

}