package de.rss.fachstudie.MiSim.events;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Objects;

/**
 *
 * @author Lion Wagner
 */
public abstract class SelfScheduledEvent extends ExternalEvent implements ISelfScheduled {
    private TimeInstant targetTime;

    @Override
    public void doInitialSelfSchedule() {
        this.schedule(targetTime);
    }


    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.targetTime = targetTime;
    }

    public SelfScheduledEvent(Model model, String s, boolean b) {
        super(model, s, b);
    }

}
