package cambio.simulator.parsing.adapter;

import java.io.IOException;
import java.util.Collections;

import cambio.simulator.parsing.*;
import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is an Adapter for a configurable and named type. Using an instance of this class for a {@link Gson} parser,
 * automatically extracts the target type (a child-class of {@code T}) form the given information and injects the given
 * configuration.
 *
 * @param <T> super type whose subclasses should be searched for usage of the {@link JsonTypeName} annotation.
 * @author Lion Wagner
 * @see JsonTypeName
 * @see JsonTypeNameResolver
 * @see TypeNameAssociatedConfigurationData
 */
public class ConfigurableNamedTypeAdapter<T> extends TypeAdapter<T> {

    private final Class<T> superClassType;

    private final Gson gson;

    public ConfigurableNamedTypeAdapter(@NotNull Class<T> superClassType) {
        this(superClassType, (TypeAdapterFactory) null);
    }

    /**
     * Creates an adapter for a configurable named type.
     *
     * @param superClassType     concrete class of the output value
     * @param typeAdapterFactory {@link TypeAdapterFactory} when additional adapters are necessary.
     */
    public ConfigurableNamedTypeAdapter(@NotNull Class<T> superClassType,
                                        @Nullable TypeAdapterFactory typeAdapterFactory) {
        this(superClassType,
            typeAdapterFactory != null
                ? GsonHelper.getGsonBuilder().registerTypeAdapterFactory(typeAdapterFactory).create()
                : GsonHelper.getGsonBuilder().create()
        );
    }

    /**
     * Creates an adapter for a configurable named type.
     *
     * @param superClassType concrete class of the output value
     * @param gson           parser that should be used to parse the {@link TypeNameAssociatedConfigurationData} into
     *                       the target type {@code T}.
     */
    public ConfigurableNamedTypeAdapter(@NotNull Class<T> superClassType, @NotNull Gson gson) {
        this.superClassType = superClassType;
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        throw new RuntimeException("Cannot write this value.");
    }

    @Override
    public T read(JsonReader in) throws IOException {
        JsonToken token = in.peek();

        if (token == JsonToken.STRING) {
            String jsonTypeName = in.nextString();
            Class<? extends T> type = JsonTypeNameResolver.resolveFromJsonTypeName(jsonTypeName, superClassType);
            System.out.printf("[Warning] Potential unsafe parsing of value %s. Make sure %s defines default values.%n",
                jsonTypeName, type.getName());

            T newObject = null;
            try {
                //doing some Gson magic to almost force the creation of an object.
                @SuppressWarnings("unchecked")
                ConstructorConstructor constructorConstructor = new ConstructorConstructor(Collections.EMPTY_MAP);
                ObjectConstructor<? extends T> constructor = constructorConstructor.get(TypeToken.get(type));
                newObject = constructor.construct();
            } catch (Exception e) {
                System.out.printf("[Error] Could not parse %s into a %s object.%n", jsonTypeName,
                    type.getName());
            }
            return newObject;
        } else if (token == JsonToken.BEGIN_OBJECT) {

            TypeNameAssociatedConfigurationData typeNameAssociatedConfigurationData =
                gson.fromJson(in, TypeNameAssociatedConfigurationData.class);

            if (typeNameAssociatedConfigurationData.type == null) {
                throw new ParsingException("Type is missing from a configuration.");
            }
            Class<? extends T> targetType =
                JsonTypeNameResolver.resolveFromJsonTypeName(typeNameAssociatedConfigurationData.type,
                    superClassType);
            if (targetType == null) {
                throw new ParsingException(String.format("Could not find json type '%s' of super class %s",
                    typeNameAssociatedConfigurationData.type, superClassType.getName()));
            }
            return gson.fromJson(typeNameAssociatedConfigurationData.getConfigAsJsonString(), targetType);
        }

        return null;
    }


}
