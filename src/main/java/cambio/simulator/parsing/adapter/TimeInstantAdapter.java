package cambio.simulator.parsing.adapter;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import desmoj.core.simulator.TimeInstant;

/**
 * Gson {@link TypeAdapter} for {@link TimeInstant} objects.
 *
 * @author Lion Wagner
 */
public class TimeInstantAdapter extends TypeAdapter<TimeInstant> {

    @Override
    public void write(JsonWriter out, TimeInstant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getTimeAsDouble());
        }
    }

    @Override
    public TimeInstant read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        double time = reader.nextDouble();
        return new TimeInstant(time);
    }
}
