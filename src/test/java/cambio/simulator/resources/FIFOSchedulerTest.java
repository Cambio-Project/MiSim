package cambio.simulator.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cambio.simulator.resources.cpu.CPUProcess;
import cambio.simulator.resources.cpu.scheduling.FIFOScheduler;
import org.javatuples.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FIFOSchedulerTest extends CPUProcessSchedulerTest<FIFOScheduler> {

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
        processList.add(new Pair<>(new CPUProcess(20), 5));
        processList.add(new Pair<>(new CPUProcess(20), 10));
        processList.add(new Pair<>(new CPUProcess(20), 30));
        processList.add(new Pair<>(new CPUProcess(20), 105));
        processList.add(new Pair<>(new CPUProcess(20), 120));

        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(processList);

        Assertions.assertEquals(processList.size(), results.size());


        for (int i = 0; i < results.size(); i++) {
            Pair<CPUProcess, Integer> input = processList.get(i);
            Pair<CPUProcess, Integer> result = results.get(i);
            Assertions.assertEquals(input.getValue0(), result.getValue0());
            Assertions.assertEquals(input.getValue0().getDemandTotal(), result.getValue1());
        }
    }
}