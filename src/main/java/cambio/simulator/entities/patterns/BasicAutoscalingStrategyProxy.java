package cambio.simulator.entities.patterns;

import cambio.simulator.nparsing.adapter.JsonTypeName;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
@JsonTypeName("autoscaling")
public class BasicAutoscalingStrategyProxy extends StrategicPeriodicServiceOwnedPattern<IAutoscalingPolicy> {

    public BasicAutoscalingStrategyProxy(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        strategy = new ReactiveAutoscalingPolicy();//default strategy
    }

    @Override
    public void onTriggered() {
        strategy.apply(owner);
    }
}
