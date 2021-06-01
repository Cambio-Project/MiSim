package testutils;

import desmoj.core.simulator.EventList;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Scheduler;

/**
 * @author Lion Wagner
 */
public class TestScheduler extends Scheduler {
    public TestScheduler(Experiment experiment, String s, EventList eventList) {
        super(experiment, s, eventList);
    }


}
