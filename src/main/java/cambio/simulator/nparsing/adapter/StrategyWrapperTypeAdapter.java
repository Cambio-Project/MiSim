package cambio.simulator.nparsing.adapter;

import java.io.IOException;
import java.util.function.Function;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class StrategyWrapperTypeAdapter<T, S> extends TypeAdapter<T> {

    private final ConfigurableNamedTypeAdapter<S> strategyLoader;
    private final Function<S, T> mapper;

    public StrategyWrapperTypeAdapter(final Class<S> strategyType, final Function<S, T> instanceCreator) {
        strategyLoader = new ConfigurableNamedTypeAdapter<>(strategyType);
        this.mapper = instanceCreator;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
    }

    @Override
    public T read(JsonReader in) throws IOException {
        S strategy = strategyLoader.read(in);
        return mapper.apply(strategy);
    }
}
