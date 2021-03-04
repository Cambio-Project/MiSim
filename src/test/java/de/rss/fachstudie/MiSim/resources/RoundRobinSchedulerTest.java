package de.rss.fachstudie.MiSim.resources;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundRobinSchedulerTest extends CPUProcessSchedulerTest<RoundRobinScheduler> {


    /**
     * Case 1 from algorithm proposing paper
     */
    @Test
    void case1() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(20), 0));
        arrivalList.add(new Pair<>(new CPUProcess(40), 0));
        arrivalList.add(new Pair<>(new CPUProcess(60), 0));
        arrivalList.add(new Pair<>(new CPUProcess(80), 0));

        int[] expectedResult = new int[]{1, 2, 3, 4, 3, 4, 4}; //process numbers (position in arrivalList + 1)

        testProcessOrder(arrivalList, expectedResult);
    }

    /**
     * Case 2
     */
    @Test
    void case2() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(10), 0));
        arrivalList.add(new Pair<>(new CPUProcess(14), 0));
        arrivalList.add(new Pair<>(new CPUProcess(70), 0));
        arrivalList.add(new Pair<>(new CPUProcess(120), 0));

        int[] expectedResult = new int[]{1, 2, 3, 4, 3, 4, 4}; //process numbers (position in arrivalList + 1)

        testProcessOrder(arrivalList, expectedResult);
    }


    /**
     * Case 3
     */
    @Test
    void case3() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(18), 0));
        arrivalList.add(new Pair<>(new CPUProcess(22), 4));
        arrivalList.add(new Pair<>(new CPUProcess(70), 8));
        arrivalList.add(new Pair<>(new CPUProcess(74), 16));

        int[] expectedResult = new int[]{1, 2, 3, 4, 4}; //process numbers (position in arrivalList + 1)

        testProcessOrder(arrivalList, expectedResult);
    }

    /**
     * Case 4
     */
    @Test
    void case4() {
        ArrayList<Pair<CPUProcess, Integer>> arrivalList = new ArrayList<>();
        arrivalList.add(new Pair<>(new CPUProcess(10), 0));
        arrivalList.add(new Pair<>(new CPUProcess(14), 6));
        arrivalList.add(new Pair<>(new CPUProcess(70), 13));
        arrivalList.add(new Pair<>(new CPUProcess(120), 21));

        int[] expectedResult = new int[]{1, 2, 3, 4, 4}; //process numbers (position in arrivalList + 1)

        testProcessOrder(arrivalList, expectedResult);
    }

}