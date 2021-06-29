package de.rss.fachstudie.MiSim.parsing.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

/**
 * @author Lion Wagner
 */
public class FileAdapter extends TypeAdapter<File> {

    @Override
    public void write(JsonWriter out, File value) throws IOException {
        if (value == null) out.nullValue();
        else {
            out.value(value.getAbsolutePath());
        }
    }

    @Override
    public File read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        String path = reader.nextString();
        return new File(path);
    }
}
