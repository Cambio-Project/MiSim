package de.unistuttgart.sqa.orcas.misim.parsing.adapter;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import desmoj.core.simulator.TimeSpan;

/**
 * Gson {@link TypeAdapter} for {@link TimeSpan} objects.
 *
 * @author Lion Wagner
 */
public class TimeSpanAdapter extends TypeAdapter<TimeSpan> {

    @Override
    public void write(JsonWriter out, TimeSpan value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getTimeAsDouble());
        }
    }

    @Override
    public TimeSpan read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        double time = reader.nextDouble();
        return new TimeSpan(time);
    }
}
