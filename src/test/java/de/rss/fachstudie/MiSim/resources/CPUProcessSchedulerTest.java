package de.rss.fachstudie.MiSim.resources;


import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import de.rss.fachstudie.MiSim.resources.cpu.scheduling.CPUProcessScheduler;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;
import testutils.TestExperiment;
import testutils.TestModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CPUProcessSchedulerTest<T extends CPUProcessScheduler> {

    private CPUProcessScheduler scheduler;

    @BeforeEach
    void setUp() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Assumptions.assumeTrue(this.getClass() != CPUProcessSchedulerTest.class); //skips tests in this superclass

        TestExperiment testExperiment = new TestExperiment();
        TestModel mod = new TestModel(null, "TestModel", false, false, () -> {
        }, () -> {
        });
        mod.connectToExperiment(testExperiment);

        Class<? extends T> genericType = ((Class<? extends T>) ((ParameterizedType) this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]);

        Constructor<? extends T> constructor = genericType.getConstructor(String.class);
        this.scheduler = constructor.newInstance("TestScheduler");
    }

    @Test
    public void returnsNullOnEmpty() {
        assertNull(scheduler.retrieveNextProcess());
    }

    @Test
    public void returnsHasToScheduleCorrectly() {
        assertFalse(scheduler.hasThreadsToSchedule());
        scheduler.enterProcess(new CPUProcess(1));
        assertTrue(scheduler.hasThreadsToSchedule());
    }

    @Test
    public void detectsThreadsToSchedule() {
        assertFalse(scheduler.hasThreadsToSchedule());
        scheduler.enterProcess(new CPUProcess(1));
        assertTrue(scheduler.hasThreadsToSchedule());
    }

    @Test
    public void doesNotReschedule() {
        scheduler.enterProcess(new CPUProcess(1));
        scheduler.enterProcess(new CPUProcess(2));
        scheduler.enterProcess(new CPUProcess(3));
        scheduler.enterProcess(new CPUProcess(4));
        for (int i = 0; i < 4; i++) {
            scheduler.retrieveNextProcessNoReschedule();
        }
        assertEquals(0, scheduler.getTotalWorkDemand());
        assertNull(scheduler.retrieveNextProcess());
        assertNull(scheduler.retrieveNextProcessNoReschedule());
    }

    @Test
    @Timeout(value = 1)
    public void returnsOneProcessCorrectly() {
        for (int i = 0; i < 100; i++) {
            int nextDemand;
            do
                nextDemand = new Random().nextInt();
            while (nextDemand < 1);
            CPUProcess process = new CPUProcess(nextDemand);
            scheduler.enterProcess(process);
            List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder();
            for (Pair<CPUProcess, Integer> result : results) {
                Assertions.assertEquals(process, result.getValue0());
            }
            int targetDemand = results.stream().mapToInt(Pair::getValue1).sum();
            Assertions.assertEquals(nextDemand, targetDemand);
        }

    }

    protected List<Pair<CPUProcess, Integer>> retrieveSchedulingOrder() {
        List<Pair<CPUProcess, Integer>> order = new ArrayList<>();
        Pair<CPUProcess, Integer> next;

        while (true) {
            next = scheduler.retrieveNextProcess();
            if (next == null) break;
            order.add(next);
            next.getValue0().reduceDemandRemainder(next.getValue1());
        }

        return order;
    }

    protected List<Pair<CPUProcess, Integer>> retrieveSchedulingOrder(Iterable<CPUProcess> input) {
        for (CPUProcess cpuProcess : input) scheduler.enterProcess(cpuProcess);
        return retrieveSchedulingOrder();
    }

    protected List<Pair<CPUProcess, Integer>> retrieveSchedulingOrder(List<Pair<CPUProcess, Integer>> input) {

        List<Pair<CPUProcess, Integer>> order = new ArrayList<>();
        Pair<CPUProcess, Integer> next;

        if (input.isEmpty()) return order;

        LinkedList<Pair<CPUProcess, Integer>> sortedInput = new LinkedList<>(input);
        sortedInput.sort(Comparator.comparing(Pair::getValue1));

        int lastStartTime = 0;
        int nextStartTime = 0;

        while (true) {
            next = scheduler.retrieveNextProcess();
            if (next == null) {
                if (sortedInput.isEmpty()) {
                    break;
                } else {
                    int nextStart = sortedInput.peek().getValue1();
                    while (!sortedInput.isEmpty() && sortedInput.peek().getValue1() == nextStart) {
                        Pair<CPUProcess, Integer> pair = sortedInput.poll();
                        scheduler.enterProcess(pair.getValue0());
                        lastStartTime = pair.getValue1();
                    }
                    continue;
                }
            }
            order.add(next);
            next.getValue0().reduceDemandRemainder(next.getValue1());
            nextStartTime = next.getValue1() + lastStartTime;

            while (!sortedInput.isEmpty() && sortedInput.peek().getValue1() < nextStartTime) {
                CPUProcess nextToSchedule = sortedInput.poll().getValue0();
                scheduler.enterProcess(nextToSchedule);
            }
            lastStartTime = nextStartTime;

        }

        Assertions.assertTrue(order.stream().allMatch(objects -> objects.getValue1() > 0));
        return order;
    }

    /**
     * @param arrivalList    list of process arrival tuples
     * @param expectedResult array containing the numbers of processes (1-based index) in expected result order.
     */
    protected void testProcessOrder(ArrayList<Pair<CPUProcess, Integer>> arrivalList, int[] expectedResult) {
        List<Pair<CPUProcess, Integer>> results = retrieveSchedulingOrder(arrivalList);

        assertEquals(expectedResult.length, results.size());

        for (int i = 0; i < results.size(); i++) {
            Pair<CPUProcess, Integer> current = results.get(i);
            assertEquals(arrivalList.get(expectedResult[i] - 1).getValue0(), current.getValue0());
        }
    }
}