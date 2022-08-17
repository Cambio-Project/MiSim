package cambio.simulator.parsing.adapter.architecture;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.entities.networking.SimpleDependencyDescription;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.GsonHelper;
import cambio.simulator.parsing.ParsingException;
import cambio.simulator.parsing.adapter.MiSimModelReferencingTypeAdapter;
import cambio.simulator.parsing.adapter.NormalDistributionAdapter;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import desmoj.core.dist.ContDistNormal;

/**
 * Adapter for parsing {@link Operation}s from JSON.
 *
 * @author Lion Wagner, Sebastian Frank
 */
public class OperationAdapter extends MiSimModelReferencingTypeAdapter<Operation> {

	private final String parentMicroserviceName;
	private final List<DependencyDescription> dependencies;

	/**
	 * Constructor creating an adapter for parsing Operations. This adapter will not
	 * parse the underlying {@link DependencyDescription}s, since they can only be
	 * resolved once all {@link Operation}s have been initialized. Instead, it will
	 * provide a list of dependencies, that can be resolved later.
	 *
	 * @param baseModel    Base model of the simulation.
	 * @param name         Name of the parent
	 *                     {@link cambio.simulator.entities.microservice.Microservice}.
	 * @param dependencies A mutable {@link List} to which all dependencies of the
	 *                     operation will be added.
	 */
	public OperationAdapter(MiSimModel baseModel, String name, List<DependencyDescription> dependencies) {
		super(baseModel);
		parentMicroserviceName = name;
		this.dependencies = dependencies;
	}

	@Override
	public void write(JsonWriter out, Operation value) throws IOException {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public Operation read(JsonReader in) throws IOException {
		JsonObject root = JsonParser.parseReader(in).getAsJsonObject();
		JsonElement nameElement = root.get("name");
		if (nameElement == null) {
			throw new ParsingException("name element is mandatory but not present");
		}

		String operationName = extractOperationName(nameElement);
		Operation operation = createOperationFrom(root, operationName);

		Collections.addAll(this.dependencies, operation.getDependencyDescriptions());
		try {
			setAsParentOperationForAllLeafDependencies(operation);
		} catch (ReflectiveOperationException e) {
			throw new ParsingException("Failed to set parent operations for contained dependencies.", e);
		}
		return operation;
	}

	private void setAsParentOperationForAllLeafDependencies(final Operation parentOperation) throws ReflectiveOperationException {
		assert parentOperation != null;
		// TODO: There is certainly a better option than using reflection
		Field parentOperationField = SimpleDependencyDescription.class.getDeclaredField("parentOperation");
		parentOperationField.setAccessible(true);
		for (final DependencyDescription dependency : parentOperation.getDependencyDescriptions()) {
			for (final SimpleDependencyDescription leafDependency : dependency.getLeafDescendants()) {
				parentOperationField.set(leafDependency, parentOperation);
			}
		}
	}

	private Operation createOperationFrom(final JsonObject root, final String operationName) {
		assert root != null;
		assert operationName != null;

		Gson gson = GsonHelper.getGsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.registerTypeAdapter(Operation.class, new OperationInstanceCreator(model, operationName))
				.registerTypeAdapter(ContDistNormal.class, new NormalDistributionAdapter(model)).registerTypeAdapter(
						DependencyDescription.class, new DependencyDescriptionAdapter(model, operationName))
				.create();

		return gson.fromJson(root, Operation.class);
	}

	private String extractOperationName(final JsonElement nameElement) {
		String operationName = nameElement.getAsString().trim();
		if (operationNameIsComposed(operationName)) {
			operationName = operationNameFromComposed(operationName);
		}
		return operationName;
	}

	private boolean operationNameIsComposed(final String operationName) {
		assert operationName != null;
		return operationName.contains(".");
	}

	private String operationNameFromComposed(final String composedOperationName) {
		assert composedOperationName != null;
		assert composedOperationName.contains(".");
		String[] names = composedOperationName.split("\\.");
		String microserviceName = names[0];
		String opNameFragment = names[1];
		assureMicroserviceNameMatchesParent(microserviceName, opNameFragment);
		return opNameFragment;
	}

	private void assureMicroserviceNameMatchesParent(final String microserviceName, final String operationName) {
		assert microserviceName != null;
		assert operationName != null;
		if (!microserviceName.equals(parentMicroserviceName)) {
			throw new ParsingException(
					String.format("Fully qualified name \"%s\" does not match name of the parent \"%s\"", operationName,
							parentMicroserviceName));
		}
	}

	private static final class OperationInstanceCreator implements InstanceCreator<Operation> {
		private final MiSimModel model;
		private final String name;

		public OperationInstanceCreator(MiSimModel model, String name) {
			this.model = model;
			this.name = name;
		}

		@Override
		public Operation createInstance(Type type) {
			return new Operation(model, name, true, null, 0);
		}
	}
}
