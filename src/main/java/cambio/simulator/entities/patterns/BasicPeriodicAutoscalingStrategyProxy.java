package cambio.simulator.entities.patterns;

import cambio.simulator.parsing.adapter.JsonTypeName;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
@JsonTypeName("autoscaling")
public class BasicPeriodicAutoscalingStrategyProxy extends StrategicPeriodicServiceOwnedPattern<IAutoscalingPolicy> {

    public BasicPeriodicAutoscalingStrategyProxy(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        strategy = new ReactiveAutoscalingPolicy();//default strategy
    }

    @Override
    public void onTriggered() {
        strategy.apply(owner);
    }
}
