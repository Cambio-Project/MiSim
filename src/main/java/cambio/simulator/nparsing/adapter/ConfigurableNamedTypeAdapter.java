package cambio.simulator.nparsing.adapter;

/**
 * @author Lion Wagner
 */

import java.io.IOException;
import java.util.Collections;

import cambio.simulator.nparsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class ConfigurableNamedTypeAdapter<T> extends TypeAdapter<T> {

    private final Class<T> superClassType;

    public ConfigurableNamedTypeAdapter(Class<T> superClassType) {
        this.superClassType = superClassType;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        throw new RuntimeException("Cannot write this value.");
    }

    @Override
    public T read(JsonReader in) throws IOException {
        JsonToken token = in.peek();


        if (token == JsonToken.STRING) {
            String JsonTypeName = in.nextString();
            Class<? extends T> type = JsonTypeNameResolver.resolveFromJsonTypeName(JsonTypeName, superClassType);
            System.out.printf("[Warning] Potential unsafe parsing of value %s. Make sure %s defines default values.%n",
                JsonTypeName, type.getName());

            T newObject = null;
            try {
                //doing some Gson magic to almost force the creation of an object.
                @SuppressWarnings("unchecked")
                ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.EMPTY_MAP);
                ObjectConstructor<? extends T> constructor = constructorConstructor.get(TypeToken.get(type));
                newObject = constructor.construct();
            } catch (Exception e) {
                System.out.printf("[Error] Could not parse %s into a %s object.%n", JsonTypeName,
                    type.getName());
            }
            return newObject;
        } else if (token == JsonToken.BEGIN_OBJECT) {

            Gson gson = new GsonHelper().getGson();

            TypeNameAssociatedConfigurationData typeNameAssociatedConfigurationData =
                gson.fromJson(in, TypeNameAssociatedConfigurationData.class);

            if (typeNameAssociatedConfigurationData.type == null) {
                throw new ParsingException("Type is missing from a configuration.");
            }
            Class<? extends T> targetType =
                JsonTypeNameResolver.resolveFromJsonTypeName(typeNameAssociatedConfigurationData.type,
                    superClassType);
            return gson.fromJson(typeNameAssociatedConfigurationData.getConfigAsJsonString(), targetType);
        }

        return null;
    }


}
