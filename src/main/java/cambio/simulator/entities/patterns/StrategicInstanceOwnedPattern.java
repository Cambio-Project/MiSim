package cambio.simulator.entities.patterns;

import com.google.gson.annotations.Expose;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class StrategicInstanceOwnedPattern<S extends IStrategy> extends InstanceOwnedPattern
    implements IStrategyAcceptor<S> {

    @Expose
    protected S strategy;

    public StrategicInstanceOwnedPattern(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public S getStrategy() {
        return strategy;
    }

    @Override
    public void setStrategy(S strategy) {
        this.strategy = strategy;
    }
}
