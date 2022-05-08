package cambio.simulator.entities.patterns;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.JsonTypeName;

/**
 * An examplary implementation of a {@link StrategicPeriodicServiceOwnedPattern} with a potentially varying {@link
 * IAutoscalingPolicy}.
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "autoscaling", alternativeNames = "autoscale")
public class BasicPeriodicAutoscalingStrategyProxy extends StrategicPeriodicServiceOwnedPattern<IAutoscalingPolicy> {

    public BasicPeriodicAutoscalingStrategyProxy(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        strategy = new ReactiveAutoscalingPolicy(); //default strategy
    }

    @Override
    public void onTriggered() {
        strategy.apply(owner);
    }
}
