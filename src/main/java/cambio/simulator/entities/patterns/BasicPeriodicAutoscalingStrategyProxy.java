package cambio.simulator.entities.patterns;

import cambio.simulator.parsing.adapter.JsonTypeName;
import desmoj.core.simulator.Model;

/**
 * An examplary implementation of a {@link StrategicPeriodicServiceOwnedPattern} with a potentially varying {@link
 * IAutoscalingPolicy}.
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "autoscaling", alternativeNames = "autoscale")
public class BasicPeriodicAutoscalingStrategyProxy extends StrategicPeriodicServiceOwnedPattern<IAutoscalingPolicy> {

    public BasicPeriodicAutoscalingStrategyProxy(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        strategy = new ReactiveAutoscalingPolicy(); //default strategy
    }

    @Override
    public void onTriggered() {
        strategy.apply(owner);
    }
}
