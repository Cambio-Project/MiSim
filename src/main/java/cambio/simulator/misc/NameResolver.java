package cambio.simulator.misc;

import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return model.getMicroservices().stream().filter(microservice -> microservice.getPlainName().equals(name))
            .findFirst().orElse(null);
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
        if (operationNameIsComposed(name)) {
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
                .map(microservice -> findOperation(microservice.getOperations(), name)).filter(Objects::nonNull)
                .findFirst().orElse(null);
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
    public static String combineToFullyQualifiedName(String serviceName, String operationName)
        throws java.lang.IllegalStateException {
        String fullyQualifiedName;
        if (serviceName != null && !operationName.startsWith(serviceName)) {

            if (operationNameIsComposed(operationName)) {
                throw new IllegalArgumentException(
                    "Inconsistent Operation description. (Service name and fully qualified operation name do not "
                        + "match.)");
            }
            fullyQualifiedName = serviceName + "." + operationName;

        } else {
            fullyQualifiedName = operationName;
        }

        return fullyQualifiedName;
    }

    /**
     * Evaluates whether the operation name also contains the name of the microservice.
     *
     * @param operationName the name of an {@link Operation}
     * @return true if name of the microservice is also present.
     */
    public static boolean operationNameIsComposed(final String operationName) {
        Objects.requireNonNull(operationName);
        return operationName.contains(".");
    }

    /**
     * Extracts the actual operation name from a composed operation name (including the microservice name). Only use
     * this operation when the operation name is actually composed (see {@link #operationNameIsComposed(String)}).
     *
     * @param composedOperationName the name of an {@link Operation} that also involves the name of a
     *                              {@link Microservice}
     * @return only the name of the {@link Operation}
     */
    public static String operationNameFromComposed(final String composedOperationName) {
        Objects.requireNonNull(composedOperationName);
        return operationNameFromComposed(composedOperationName, null);
    }

    /**
     * Extracts the actual operation name from a composed operation name (including the microservice name). Only use
     * this operation when the operation name is actually composed (see {@link #operationNameIsComposed(String)}).
     *
     * @param composedOperationName    the name of an {@link Operation} that also involves the name of a
     *                                 {@link Microservice}
     * @param expectedMicroserviceName checks whether this is the name of the microservice.
     * @return only the name of the {@link Operation}
     * @throws ParsingException if the actual and the expected name of the microservice does not match
     */
    public static String operationNameFromComposed(final String composedOperationName,
                                                   @Nullable final String expectedMicroserviceName)
        throws ParsingException {
        Objects.requireNonNull(composedOperationName);
        String[] names = composedOperationName.split("\\.");
        String microserviceName = names[0];
        String opNameFragment = names[1];
        if (expectedMicroserviceName != null) {
            assureMicroserviceNameMatchesParent(microserviceName, opNameFragment, expectedMicroserviceName);
        }
        return opNameFragment;
    }

    private static void assureMicroserviceNameMatchesParent(final String microserviceName, final String operationName,
                                                            final String expectedMicroserviceName) {
        assert microserviceName != null;
        assert operationName != null;
        if (!microserviceName.equals(expectedMicroserviceName)) {
            throw new ParsingException(
                String.format("Fully qualified name \"%s\" does not match name of the parent \"%s\"", operationName,
                    expectedMicroserviceName));
        }
    }
}
