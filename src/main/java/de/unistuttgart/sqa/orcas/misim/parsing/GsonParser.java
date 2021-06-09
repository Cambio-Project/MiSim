package de.unistuttgart.sqa.orcas.misim.parsing;


import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.unistuttgart.sqa.orcas.misim.parsing.adapter.FileAdapter;
import de.unistuttgart.sqa.orcas.misim.parsing.adapter.TimeInstantAdapter;
import de.unistuttgart.sqa.orcas.misim.parsing.adapter.TimeSpanAdapter;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Utility class to create a GsonParser that has appropriate type adapters.
 *
 * @author Lion Wagner
 * @see com.google.gson.TypeAdapter
 */
public class GsonParser {

    public GsonParser() {
    }

    /**
     * Creates a new default Gson object for parsing JSON that supports {@link File}, {@link TimeInstant} and {@link
     * TimeSpan} adaption.
     *
     * @return a default {@link Gson}.
     */
    public Gson getGson() {
        return new GsonBuilder()
            .registerTypeAdapter(File.class, new FileAdapter())
            .registerTypeAdapter(TimeInstant.class, new TimeInstantAdapter())
            .registerTypeAdapter(TimeSpan.class, new TimeSpanAdapter())
            .create();
    }

}
