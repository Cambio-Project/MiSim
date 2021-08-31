package cambio.simulator.parsing.adapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Lion Wagner
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value != null) {
            long epochSec = value.toEpochSecond(ZoneOffset.UTC) * 1000;
            out.value(epochSec);
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NUMBER) {
            long value = in.nextLong();
            return LocalDateTime.ofEpochSecond(value, 0, OffsetDateTime.now().getOffset());
        } else {
            return null;
        }
    }
}
