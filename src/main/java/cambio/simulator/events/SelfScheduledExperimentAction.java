package cambio.simulator.events;

import java.util.Objects;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an event that can do an initial self schedule if asked to.
 *
 * @author Lion Wagner
 */
public abstract class SelfScheduledExperimentAction extends ExperimentAction implements ISelfScheduled {
    private TimeInstant targetTime;

    public SelfScheduledExperimentAction(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void doInitialSelfSchedule() {
        this.schedule(Objects.requireNonNullElseGet(targetTime, () -> new TimeInstant(initialArrivalTime)));
    }

    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.targetTime = targetTime;
    }

}
