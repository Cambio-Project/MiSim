package cambio.simulator.events;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an event that can do an initial self schedule if ask to.
 *
 * @author Lion Wagner
 */
public abstract class SelfScheduledEvent extends ExternalEvent implements ISelfScheduled {
    @SerializedName(value = "arrival_time", alternate = {"time", "target_time", "targetTime"})
    private TimeInstant targetTime;

    public SelfScheduledEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void doInitialSelfSchedule() {
        Objects.requireNonNull(targetTime);
        this.schedule(targetTime);
    }

    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.targetTime = targetTime;
    }

}
