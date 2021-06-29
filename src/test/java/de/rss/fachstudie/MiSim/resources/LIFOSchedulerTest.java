package de.rss.fachstudie.MiSim.resources;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import de.rss.fachstudie.MiSim.resources.cpu.scheduling.LIFOScheduler;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LIFOSchedulerTest extends CPUProcessSchedulerTest<LIFOScheduler> {


    @Test
    void simultaneous_schedules() {
        ArrayList<CPUProcess> processList = new ArrayList<>();
        processList.add(new CPUProcess(10));
        processList.add(new CPUProcess(20));
        processList.add(new CPUProcess(30));
        processList.add(new CPUProcess(40));

        Collections.shuffle(processList);

        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(processList);

        Assertions.assertEquals(processList.size(), results.size());

        Collections.reverse(processList);

        for (int i = 0; i < results.size(); i++) {
            Pair<CPUProcess, Integer> result = results.get(i);
            CPUProcess input = processList.get(i);

            Assertions.assertEquals(input, result.getValue0());
            Assertions.assertEquals(input.getDemandTotal(), result.getValue1());
        }
    }


    @Test
    void overlapping_schedules() {
        List<Pair<CPUProcess, Integer>> processList = new ArrayList<>();
        processList.add(new Pair<>(new CPUProcess(20), 0));
        processList.add(new Pair<>(new CPUProcess(21), 5));
        processList.add(new Pair<>(new CPUProcess(22), 10));
        processList.add(new Pair<>(new CPUProcess(23), 30));
        processList.add(new Pair<>(new CPUProcess(24), 105));
        processList.add(new Pair<>(new CPUProcess(25), 105));

        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(processList);

        Assertions.assertEquals(processList.size(), results.size());

        int[] expectedOrder = new int[]{20, 22, 23, 21, 25, 24};

        for (int i = 0; i < results.size(); i++) {
            Pair<CPUProcess, Integer> result = results.get(i);

            Assertions.assertEquals(expectedOrder[i], result.getValue0().getDemandTotal());
        }
    }
}