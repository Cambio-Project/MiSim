package cambio.simulator.parsing.adapter;

import java.io.IOException;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Lion Wagner
 */
public class StrategyWrapperTypeAdapter<T, S> extends TypeAdapter<T> {

    private final ConfigurableNamedTypeAdapter<S> strategyLoader;
    private final Function<S, T> mapper;

    public StrategyWrapperTypeAdapter(@NotNull final Class<S> strategyType,
                                      @NotNull final Function<S, T> instanceCreator) {
        this(strategyType, instanceCreator, (TypeAdapterFactory) null);
    }

    public StrategyWrapperTypeAdapter(@NotNull final Class<S> strategyType,
                                      @NotNull final Function<S, T> instanceCreator,
                                      @Nullable TypeAdapterFactory factory) {
        strategyLoader = new ConfigurableNamedTypeAdapter<>(strategyType, factory);
        this.mapper = instanceCreator;
    }

    public StrategyWrapperTypeAdapter(@NotNull final Class<S> strategyType,
                                      @NotNull final Function<S, T> instanceCreator,
                                      @NotNull Gson gson) {
        strategyLoader = new ConfigurableNamedTypeAdapter<>(strategyType, gson);
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
