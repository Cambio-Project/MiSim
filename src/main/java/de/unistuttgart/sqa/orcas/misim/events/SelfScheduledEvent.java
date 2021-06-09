package de.unistuttgart.sqa.orcas.misim.events;

import java.util.Objects;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an event that can do an initial self schedule if ask to.
 *
 * @author Lion Wagner
 */
public abstract class SelfScheduledEvent extends ExternalEvent implements ISelfScheduled {
    private TimeInstant targetTime;

    @Override
    public void doInitialSelfSchedule() {
        Objects.requireNonNull(targetTime);
        this.schedule(targetTime);
    }


    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.targetTime = targetTime;
    }

    public SelfScheduledEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

}
