package cambio.simulator.parsing.adapter.experiment;

import java.io.IOException;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Factory for Adapters that turn JSON-strings into {@link Microservice} and {@link Operation} objects using the {@link
 * NameResolver}. Requires the presence of these instances in the {@link cambio.simulator.models.ArchitectureModel}.
 *
 * <p>
 * Currently, only supports {@link Microservice}s and {@link Operation}s.
 *
 * @author Lion Wagner
 * @see NameResolver
 */
public class NameReferenceTypeAdapterFactory implements TypeAdapterFactory {

    private final MiSimModel model;

    public NameReferenceTypeAdapterFactory(MiSimModel model) {
        this.model = model;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> clazz = type.getRawType();
        if (!Operation.class.isAssignableFrom(clazz) && !Microservice.class.isAssignableFrom(clazz)) {
            return null;
        }
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value instanceof Operation) {
                    out.value(((Operation) value).getFullyQualifiedName());
                } else if (value instanceof Microservice) {
                    out.value(((Microservice) value).getPlainName());
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement entityNameJsonElement = JsonParser.parseReader(in);
                T resolved = null;
                try {
                    String entityName = entityNameJsonElement.getAsString();
                    if (Operation.class.isAssignableFrom(clazz)) {
                        resolved = (T) NameResolver.resolveOperationName(model, entityName);
                    } else if (Microservice.class.isAssignableFrom(clazz)) {
                        resolved = (T) NameResolver.resolveMicroserviceName(model, entityName);
                    }

                } catch (ClassCastException | IllegalStateException e) {
                    throw new ParsingException(
                        String.format("[Error] could not parse %s. Name is not known.", entityNameJsonElement));
                }
                return resolved;

            }
        };
    }

}
