package cambio.simulator.entities.patterns;

import com.google.gson.annotations.Expose;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class StrategicPeriodicServiceOwnedPattern<S extends IStrategy> extends StrategicServiceOwnedPattern<S>
    implements IPeriodicServiceOwnedPattern {

    @Expose
    protected final double period = 1;
    @Expose
    protected final double start = 0;
    @Expose
    protected final double stop = Double.MAX_VALUE;

    private transient PeriodicPatternScheduler scheduler;

    public StrategicPeriodicServiceOwnedPattern(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void onInitializedCompleted() {
        scheduler = new PeriodicPatternScheduler(getModel(), this, start, stop, period);
        scheduler.activate(new TimeInstant(start));
    }

    @Override
    public final PeriodicPatternScheduler getScheduler() {
        return scheduler;
    }
}
