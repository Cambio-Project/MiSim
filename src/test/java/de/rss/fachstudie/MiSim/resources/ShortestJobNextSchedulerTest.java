package de.rss.fachstudie.MiSim.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import de.rss.fachstudie.MiSim.resources.cpu.scheduling.ShortestJobNextScheduler;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
public class ShortestJobNextSchedulerTest extends CPUProcessSchedulerTest<ShortestJobNextScheduler> {

    @Test
    void simultaneous_schedules() {
        List<CPUProcess> processList = new ArrayList<>();
        processList.add(new CPUProcess(10));
        processList.add(new CPUProcess(20));
        processList.add(new CPUProcess(20));
        processList.add(new CPUProcess(30));
        processList.add(new CPUProcess(40));

//        Collections.shuffle(processList);

        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(processList);

        assertEquals(processList.size(), results.size());

        for (int i = 0; i < results.size() - 1; i++) {
            Pair<CPUProcess, Integer> result1 = results.get(i);
            Pair<CPUProcess, Integer> result_next = results.get(i + 1);
            Assertions.assertTrue(result1.getValue1() <= result_next.getValue1());
        }
    }


    @Test
    void overlapping_schedules() {

        int[] expectationList = new int[] {20, 8, 9, 10, 20, 10, 1, 2};

        List<Pair<CPUProcess, Integer>> processList = new ArrayList<>();
        processList.add(new Pair<>(new CPUProcess(expectationList[0]), 0));
        processList.add(new Pair<>(new CPUProcess(expectationList[1]), 10));
        processList.add(new Pair<>(new CPUProcess(expectationList[2]), 15));
        processList.add(new Pair<>(new CPUProcess(expectationList[3]), 5));
        processList.add(new Pair<>(new CPUProcess(expectationList[4]), 30));
        processList.add(new Pair<>(new CPUProcess(expectationList[5]), 100));
        processList.add(new Pair<>(new CPUProcess(expectationList[6]), 110));
        processList.add(new Pair<>(new CPUProcess(expectationList[7]), 110));


        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(processList);

        assertEquals(processList.size(), results.size());


        for (int i = 0; i < results.size(); i++) {
            Pair<CPUProcess, Integer> result = results.get(i);
            assertEquals(expectationList[i], result.getValue1());
        }
    }
}
