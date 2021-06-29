package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    @Disabled
    void sorts_dataset_correctly() {
//        reporter.addDatapoint("Test", new TimeInstant(1), "World");
//        reporter.addDatapoint("Test", new TimeInstant(0), "Hello");
//        HashMap<Double, ?> dataset = reporter.getDataSets().get("Test");
//        Assertions.assertEquals(dataset.pollFirstEntry().getKey(), 0.0, 0.0000001);
//        Assertions.assertEquals(dataset.pollFirstEntry().getKey(), 1.0, 0.0000001);
    }

    @Test
    @Disabled
    void creates_dataset_output_correctly() {
        reporter.addDatapoint("Test", new TimeInstant(1), "World");
        reporter.addDatapoint("Test", new TimeInstant(0), "Hello");
        String[] result = reporter.getEntries("Test");
        Assertions.assertEquals("Time;Value", result[0]);
        Assertions.assertTrue(result[1].matches("0\\.(0*);Hello"));
        Assertions.assertTrue(result[2].matches("1\\.(0*);World"));
    }

    @Test
    void multi_type_compatibility() {
        reporter.addDatapoint("Test", new TimeInstant(0), 42);
        reporter.addDatapoint("Test", new TimeInstant(1), "Hello");
        reporter.addDatapoint("Test2", new TimeInstant(1), 1337.2f);
        reporter.addDatapoint("Test3", new TimeInstant(1), new Pair<>(new TimeInstant(42), 54.3));
    }

}