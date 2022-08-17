package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.util.LinkedList;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.SimpleDependencyDescription;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import desmoj.core.dist.ContDistNormal;

/**
 * Adapter for parsing the architecture model from json into an object.
 *
 * <p>
 * Also triggers the resolving of names into proper objects once all
 * dependencies and nodes are established.
 *
 * @author Lion Wagner, Sebastian Frank
 */
public class ArchitectureModelAdapter extends MiSimModelReferencingTypeAdapter<ArchitectureModel> {
	/**
	 * All (top level) dependencies that have been generated. They need to be late
	 * resolved to reference the parsed microservices and operations.
	 */
	protected final LinkedList<DependencyDescription> dependencies = new LinkedList<>();

	public ArchitectureModelAdapter(MiSimModel baseModel) {
		super(baseModel);
	}

	@Override
	public void write(JsonWriter out, ArchitectureModel value) throws IOException {
	}

	@Override
	public ArchitectureModel read(JsonReader in) throws IOException {
		final JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
		final ArchitectureModel architectureModel = createArchitectureModelFrom(root);
		lateResolveReferencesInAllDependencies(architectureModel);
		return architectureModel;
	}

	private ArchitectureModel createArchitectureModelFrom(JsonObject root) {
		Gson gson = GsonHelper.getGsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model))
				.registerTypeAdapter(Microservice.class, new MicroserviceAdapter(model, dependencies)).create();

		return gson.fromJson(root, ArchitectureModel.class);
	}

	private void lateResolveReferencesInAllDependencies(final ArchitectureModel architectureModel) {
		for (final DependencyDescription dependency : dependencies) {
			for (final SimpleDependencyDescription simpleDependency : dependency.getLeafDescendants()) {
				simpleDependency.resolveNames(architectureModel);
			}
		}
	}

}
