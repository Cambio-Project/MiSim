package de.rss.fachstudie.MiSim.resources;

import java.util.ArrayList;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import de.rss.fachstudie.MiSim.resources.cpu.scheduling.MultiLevelFeedbackQueueScheduler;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

class MLFQSchedulerTest extends CPUProcessSchedulerTest<MultiLevelFeedbackQueueScheduler> {

    @Test
    void test_simultaneous_schedules() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(20), 0));
        arrivalList.add(new Pair<>(new CPUProcess(40), 0));
        arrivalList.add(new Pair<>(new CPUProcess(60), 0));
        arrivalList.add(new Pair<>(new CPUProcess(80), 0));

        int[] expectedResult = new int[] {1, 2, 3, 4, 3, 4, 4};

        testProcessOrder(arrivalList, expectedResult);
    }

    @Test
    void test_LIFO_BehaviorForShortProcesses() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(20), 0)); //P1
        arrivalList.add(new Pair<>(new CPUProcess(40), 0)); //P2
        arrivalList.add(new Pair<>(new CPUProcess(60), 0)); //P3
        arrivalList.add(new Pair<>(new CPUProcess(80), 0)); //P4
        arrivalList.add(new Pair<>(new CPUProcess(10), 1)); //P5
        arrivalList.add(new Pair<>(new CPUProcess(45), 2)); //P6

        int[] expectedResult = new int[] {1, 2, 3, 4, 5, 6, 3, 4, 4};

        testProcessOrder(arrivalList, expectedResult);
    }

    @Test
    void test_LIFO_BehaviorForLateArrivingProcesses() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();

        arrivalList.add(new Pair<>(new CPUProcess(200), 0)); //P1
        arrivalList.add(new Pair<>(new CPUProcess(200), 0)); //P2
        arrivalList.add(new Pair<>(new CPUProcess(10), 0)); //P3
        arrivalList.add(new Pair<>(new CPUProcess(10), 0)); //P4
        arrivalList.add(new Pair<>(new CPUProcess(10), 0)); //P5

        //2x 175 in Q2, time = 80 (2x25 + 3x10)
        arrivalList.add(new Pair<>(new CPUProcess(50), 79)); //P6
        arrivalList.add(new Pair<>(new CPUProcess(50), 79)); //P7
        arrivalList.add(new Pair<>(new CPUProcess(50), 79)); //P8
        arrivalList.add(new Pair<>(new CPUProcess(10), 79)); //P9
        arrivalList.add(new Pair<>(new CPUProcess(10), 79)); //P10
        arrivalList.add(new Pair<>(new CPUProcess(10), 79)); //P11
        //2x 175 and 3x 30 in Q2 time = 200 = 80 + (3*30)+ (3*10)


        //2x 145 in Q3 time = 310 =  200 + (3*20) + (2*25)
        arrivalList.add(new Pair<>(new CPUProcess(5), 309)); //P12


        int[] expectedResult = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 1, 2, 6, 7, 8, 12, 1, 2};

        testProcessOrder(arrivalList, expectedResult);
    }
}