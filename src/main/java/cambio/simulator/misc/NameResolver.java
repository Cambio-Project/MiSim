package cambio.simulator.misc;

import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class to help resolve names into {@link Operation} and {@link Microservice} object.
 *
 * @author Lion Wagner
 */
public final class NameResolver {

    /**
     * Resolves the given name into a {@link Microservice} object.
     *
     * @param model underlying MiSim model
     * @param name  Name that should be resolved.
     * @return a {@link Microservice} with the given name or null if none is found
     */
    public static Microservice resolveMicroserviceName(MiSimModel model, String name) {
        return resolveMicroserviceName(model.getArchitectureModel(), name);
    }

    /**
     * Resolves the given name into a {@link Microservice} object.
     *
     * @param model underlying architecture model
     * @param name  Name that should be resolved.
     * @return a {@link Microservice} with the given name or null if none is found
     */
    public static Microservice resolveMicroserviceName(ArchitectureModel model, String name) {
        return model.getMicroservices()
            .stream()
            .filter(microservice -> microservice.getPlainName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * Resolves the given name into a {@link Operation} object.
     *
     * @param model underlying model
     * @param name  Name that should be resolved.
     * @return a {@link Operation} with the given name or null if none is found
     */
    public static Operation resolveOperationName(MiSimModel model, String name) {
        return resolveOperationName(model.getArchitectureModel(), name);

    }

    /**
     * Resolves the given name into a {@link Operation} object.
     *
     * <p>
     * The name can be either the plain name of the operation (then any operation that matches will be returned) or a
     * fully qualified name.
     *
     * @param model underlying architecture model
     * @param name  Name that should be resolved.
     * @return a {@link Operation} with the given name or null if none is found
     * @see Operation#getPlainName()
     */
    public static Operation resolveOperationName(@NotNull ArchitectureModel model, @NotNull String name) {

        if (name.contains(".")) {
            String[] names = name.split("\\.");
            String serviceName = names[0];
            String operationName = names[1];
            Microservice ms = resolveMicroserviceName(model, serviceName);
            if (ms == null) {
                return null;
            }
            return findOperation(ms.getOperations(), operationName);
        } else {
            return model.getMicroservices().stream()
                .map(microservice -> findOperation(microservice.getOperations(), name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        }


    }

    private static Operation findOperation(Operation[] operations, String name) {

        for (Operation operation : operations) {
            if (operation.getPlainName().equals(name)) {
                return operation;
            }
        }
        return null;
    }
    /**
     * Tries to combine a service name and an operation name into a fully qualified name.
     *
     * @return &lt;service_name&gt;.&lt;operation_name&gt; or &lt;operation_name&gt; if no combination could be
     *     established.
     * @throws java.lang.IllegalArgumentException if the given operation is fully qualified and the serviceName does not
     *                                            match.
     */

    public static String resolveFullyQualifiedName(String serviceName, String operationName)
        throws java.lang.IllegalStateException {
        String fullyQualifiedName;
        if (serviceName != null && !operationName.startsWith(serviceName)) {

            if (operationName.contains(".")) {
                throw new IllegalStateException(
                    "Inconsistent Operation description. (Service name and fully qualified name do not match.)");
            }
            fullyQualifiedName = serviceName + "." + operationName;

        } else {
            fullyQualifiedName = operationName;
        }

        return fullyQualifiedName;
    }
}
