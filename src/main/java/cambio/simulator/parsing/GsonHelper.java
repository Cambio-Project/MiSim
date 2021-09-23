package cambio.simulator.parsing;


import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.parsing.adapter.FileAdapter;
import cambio.simulator.parsing.adapter.LocalDateTimeAdapter;
import cambio.simulator.parsing.adapter.TimeInstantAdapter;
import cambio.simulator.parsing.adapter.TimeSpanAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Utility class to create a GsonParser that has appropriate type adapters.
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
    public Gson getGson() {
        return getGsonBuilder().create();
    }

    /**
     * Creates a {@link GsonBuilder} that should contain all common adapters, such as a {@link FileAdapter} and {@link
     * TimeInstantAdapter}.
     */
    public GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(File.class, new FileAdapter())
            .registerTypeAdapter(TimeSpan.class, new TimeSpanAdapter())
            .registerTypeAdapter(TimeInstant.class, new TimeInstantAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
    }


}
