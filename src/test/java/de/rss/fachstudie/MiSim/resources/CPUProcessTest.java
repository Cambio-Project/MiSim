package de.rss.fachstudie.MiSim.resources;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPUProcessTest {


    @BeforeEach
    void setUp() {
    }

    @Test
    void test_getDemandRemainderAndTotalOnCreation() {
        CPUProcess process = new CPUProcess(42);
        assertEquals(42, process.getDemandRemainder());
        assertEquals(42, process.getDemandTotal());
    }


    @Test
    void test_getDemandRemainder_TimeDependent() {
        CPUProcess process = new CPUProcess(42);
        process.stampCurrentBurstStarted(new TimeInstant(0));
        assertEquals(32, process.getDemandRemainder(new TimeInstant(1), 10));
        assertEquals(22, process.getDemandRemainder(new TimeInstant(2), 10));
        assertEquals(42, process.getDemandTotal());
    }

    @Test
    void test_throwsExceptionOnNonPositiveDemand() {
        assertThrows(IllegalArgumentException.class, () -> new CPUProcess(-1));
    }

    @Test
    void test_throwsExceptionOn0Demand() {
        assertDoesNotThrow(() -> new CPUProcess(0));
    }

    @Test
    void test_throwsExceptionOnTooMuchReduction() {
        final CPUProcess process = new CPUProcess(50);
        assertThrows(IllegalArgumentException.class, () -> process.reduceDemandRemainder(51));
    }

    @Test
    void test_requestIsNullOnArtificialLoadProcess() {
        final CPUProcess process = new CPUProcess(50);
        assertNull(process.getRequest());
    }

    @Test
    void test_reducesDemandCorrectly() {
        final CPUProcess process = new CPUProcess(50);
        process.reduceDemandRemainder(25);
        assertEquals(25, process.getDemandRemainder());
        assertEquals(50, process.getDemandTotal());
    }
}