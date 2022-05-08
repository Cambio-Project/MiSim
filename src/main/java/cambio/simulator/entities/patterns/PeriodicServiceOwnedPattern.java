package cambio.simulator.entities.patterns;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * A service owned pattern that is automatically triggered periodically.
 *
 * @author Lion Wagner
 */
public abstract class PeriodicServiceOwnedPattern extends ServiceOwnedPattern implements IPeriodicPattern {

    @Expose
    @SerializedName(value = "interval", alternate = {"period"})
    private double period = 1;
    @Expose
    private double start = 0;
    @Expose
    private double stop = Double.MAX_VALUE;

    private transient PeriodicPatternScheduler scheduler;

    public PeriodicServiceOwnedPattern(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void onInitializedCompleted(Model model) {
        scheduler = new PeriodicPatternScheduler(getModel(), this, start, stop, period);
        scheduler.activate(new TimeInstant(start));
    }

    @Override
    public final PeriodicPatternScheduler getScheduler() {
        return scheduler;
    }

}
