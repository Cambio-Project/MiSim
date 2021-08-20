package cambio.simulator.nparsing.adapter.architecture;

import java.io.IOException;

import cambio.simulator.entities.patterns.ILoadBalancingStrategy;
import cambio.simulator.entities.patterns.LoadBalancer;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.nparsing.adapter.ConfigurableNamedTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class LoadBalancerAdapter extends TypeAdapter<LoadBalancer> {

    private final ConfigurableNamedTypeAdapter<ILoadBalancingStrategy> wrappedAdapter =
        new ConfigurableNamedTypeAdapter<>(ILoadBalancingStrategy.class);
    private final MiSimModel baseModel;

    public LoadBalancerAdapter(MiSimModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void write(JsonWriter out, LoadBalancer value) throws IOException {
        wrappedAdapter.write(out, value.getLoadBalancingStrategy());
    }

    @Override
    public LoadBalancer read(JsonReader in) throws IOException {
        ILoadBalancingStrategy strategy = wrappedAdapter.read(in);
        return new LoadBalancer(baseModel, "Loadbalancer", false, strategy);
    }
}
