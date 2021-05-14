package de.rss.fachstudie.MiSim.parsing;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.rss.fachstudie.MiSim.parsing.adapter.FileAdapter;

import java.io.File;

/**
 * @author Lion Wagner
 */
public class GsonParser {

    public GsonParser(){}

    public Gson getGson(){

        return new GsonBuilder().registerTypeAdapter(File.class,new FileAdapter()).create();
    }

}
