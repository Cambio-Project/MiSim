package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;

import cambio.simulator.entities.patterns.ServiceOwnedPattern;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.PatternConfigurationParser;
import cambio.simulator.parsing.TypeNameAssociatedConfigurationData;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter for parsing patterns owned by {@link cambio.simulator.entities.microservice.Microservice}s.
 *
 * @author Lion Wagner
 */
public class ServiceOwnedPatternAdapter extends MiSimModelReferencingTypeAdapter<ServiceOwnedPattern> {

    private final String msName;

    public ServiceOwnedPatternAdapter(MiSimModel model, String msName) {
        super(model);
        this.msName = msName;
    }

    @Override
    public void write(JsonWriter out, ServiceOwnedPattern value) throws IOException {

    }

    @Override
    public ServiceOwnedPattern read(@NotNull JsonReader in) throws IOException {
        Gson gson = new GsonBuilder().create();

        if (in.peek() == JsonToken.BEGIN_OBJECT) {
            TypeNameAssociatedConfigurationData configData =
                gson.fromJson(in, TypeNameAssociatedConfigurationData.class);

            ServiceOwnedPattern patternInstance = null;
            try {
                patternInstance =
                    PatternConfigurationParser.getPatternInstance(model, msName, configData,
                        ServiceOwnedPattern.class);
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new ParsingException(String.format("Could not create a service owned pattern with type %s.",
                    configData.type), e);
            }

            return patternInstance;
        }
        return null;
    }
}