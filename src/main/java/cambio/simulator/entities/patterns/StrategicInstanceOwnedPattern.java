package cambio.simulator.entities.patterns;

import cambio.simulator.models.MiSimModel;
import com.google.gson.annotations.Expose;

/**
 * Represents a {@link ServiceOwnedPattern} that wants to be injected with an  {@link IStrategy} object.
 *
 * @param <S> type of the {@link IStrategy} which is expected.
 * @author Lion Wagner
 */
public abstract class StrategicInstanceOwnedPattern<S extends IStrategy> extends InstanceOwnedPattern
    implements IStrategyAcceptor<S> {

    @Expose
    protected S strategy;

    public StrategicInstanceOwnedPattern(MiSimModel model, String name, boolean showInTrace) {
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
