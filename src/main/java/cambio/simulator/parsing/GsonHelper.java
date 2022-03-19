package cambio.simulator.parsing;


import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.parsing.adapter.*;
import com.google.gson.*;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to create a Gson with a default set of {@link com.google.gson.TypeAdapter}s.
 *
 * @author Lion Wagner
 * @see com.google.gson.TypeAdapter
 */
public final class GsonHelper {

    /**
     * Creates a new default Gson object for parsing JSON that supports {@link File}, {@link TimeInstant} and {@link
     * TimeSpan} adaption.
     *
     * @return a default {@link Gson}.
     */
    @Contract(" -> new")
    public static @NotNull Gson getGson() {
        return getGsonBuilder().create();
    }

    /**
     * Creates a {@link GsonBuilder} that should contain all common adapters, such as a {@link FileAdapter} and {@link
     * TimeInstantAdapter}.
     */
    @Contract(" -> new")
    public static @NotNull GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(File.class, new FileAdapter())
            .registerTypeAdapter(TimeSpan.class, new TimeSpanAdapter())
            .registerTypeAdapter(TimeInstant.class, new TimeInstantAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapterFactory(new PathAdapterFactory());
    }
}
