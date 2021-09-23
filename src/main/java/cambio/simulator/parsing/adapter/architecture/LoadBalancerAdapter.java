package cambio.simulator.parsing.adapter.architecture;

import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.LoadBalancer;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.adapter.StrategyWrapperTypeAdapter;

/**
 * Wrapper class around a {@link StrategyWrapperTypeAdapter} for creating new {@link LoadBalancer} objects.
 *
 * @author Lion Wagner
 */
//This class could be replaced by inlining the constructor call below.
//However, this would decrease readability by quite a bit.
class LoadBalancerAdapter extends StrategyWrapperTypeAdapter<LoadBalancer, ILoadBalancingStrategy> {
    public LoadBalancerAdapter(MiSimModel baseModel) {
        super(ILoadBalancingStrategy.class,
            iLoadBalancingStrategy -> new LoadBalancer(baseModel, "Loadbalancer", true, iLoadBalancingStrategy));

    }

}
