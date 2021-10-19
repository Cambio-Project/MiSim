package cambio.simulator.parsing.adapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Adapter factory to react to different Path classes such as  {@code sun.nio.fs.WindowsPath}.
 *
 * @author Lion Wagner
 */
public class PathAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (Path.class.isAssignableFrom(type.getRawType())) {
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else if (value instanceof Path) {
                        out.value(((Path) value).toAbsolutePath().toString());
                    }
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.STRING) {
                        //noinspection unchecked
                        return (T) Paths.get(in.nextString());
                    } else {
                        return null;
                    }
                }
            };
        }
        return null;
    }
}
