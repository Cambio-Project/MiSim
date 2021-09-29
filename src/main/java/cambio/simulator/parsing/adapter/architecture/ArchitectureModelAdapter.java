package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.util.LinkedList;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * Adapter for parsing the architecture model from json into an object.
 *
 * <p>
 * Also triggers the resolving of names into proper objects once all dependencies and nodes are established.
 *
 * @author Lion Wagner
 */
public class ArchitectureModelAdapter extends MiSimModelReferencingTypeAdapter<ArchitectureModel> {

    private final LinkedList<DependencyDescription> dependencies = new LinkedList<>();

    public ArchitectureModelAdapter(MiSimModel baseModel) {
        super(baseModel);
    }

    @Override
    public void write(JsonWriter out, ArchitectureModel value) throws IOException {
    }

    @Override
    public ArchitectureModel read(JsonReader in) throws IOException {
        JsonObject root = JsonParser.parseReader(in).getAsJsonObject();

        Gson gson = new GsonHelper()
            .getGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(baseModel))
            .registerTypeAdapter(Microservice.class, new MicroserviceAdapter(baseModel, dependencies))
            .create();

        ArchitectureModel architectureModel = gson.fromJson(root, ArchitectureModel.class);

        for (DependencyDescription dependency : dependencies) {
            dependency.resolveNames(architectureModel);
        }
        return architectureModel;
    }
}
