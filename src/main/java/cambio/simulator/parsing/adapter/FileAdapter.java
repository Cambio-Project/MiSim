package cambio.simulator.parsing.adapter;

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.*;

/**
 * Gson {@link TypeAdapter} for the {@link File} type.
 *
 * <p>
 * This adapter is mandatory since Javas' stricter reflection rules. This adapter converts {@link File}s to {@link
 * String}s and vise-versa using the files' absolut path.
 *
 * @author Lion Wagner
 */
public class FileAdapter extends TypeAdapter<File> {

    @Override
    public void write(JsonWriter out, File value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
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
        if (path.startsWith("\"")) {
            path = path.replaceAll("\"", "");
        }
        return new File(path);
    }
}
