package cambio.simulator.entities.patterns;

import cambio.simulator.models.MiSimModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents a pattern owned by a {@link cambio.simulator.entities.microservice.Microservice} that employs a {@link
 * IStrategy}. This periodic pattern will automatically and periodically the {@link IPeriodicPattern#trigger()} method.
 *
 * @author Lion Wagner
 * @see IPeriodicPattern
 */
public abstract class StrategicPeriodicServiceOwnedPattern<S extends IStrategy> extends StrategicServiceOwnedPattern<S>
    implements IPeriodicPattern {

    @Expose
    @SerializedName(value = "interval", alternate = {"period"})
    protected double period = 1;
    @Expose
    protected double start = 0;
    @Expose
    protected double stop = Double.MAX_VALUE;

    private transient PeriodicPatternScheduler scheduler;

    public StrategicPeriodicServiceOwnedPattern(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void onInitializedCompleted(MiSimModel model) {
        scheduler = new PeriodicPatternScheduler(getModel(), this, start, stop, period);
        scheduler.activate(new TimeInstant(start));
    }

    @Override
    public final PeriodicPatternScheduler getScheduler() {
        return scheduler;
    }
}
