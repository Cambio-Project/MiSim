package de.rss.fachstudie.MiSim.parsing;


import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.rss.fachstudie.MiSim.parsing.adapter.FileAdapter;

/**
 * Utility class to create a GsonParser that has appropriate type adapters.
 *
 * @see com.google.gson.TypeAdapter
 *
 * @author Lion Wagner
 */
public class GsonParser {

    public GsonParser() {
    }

    public Gson getGson() {

        return new GsonBuilder().registerTypeAdapter(File.class, new FileAdapter()).create();
    }

}
