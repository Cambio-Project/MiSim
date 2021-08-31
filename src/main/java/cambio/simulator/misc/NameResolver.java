package cambio.simulator.misc;

import java.util.Objects;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.models.ArchitectureModel;
import cambio.simulator.models.MiSimModel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public final class NameResolver {

    public static Microservice resolveMicroserviceName(MiSimModel model, String name) {
        return resolveMicroserviceName(model.getArchitectureModel(), name);
    }

    public static Microservice resolveMicroserviceName(ArchitectureModel model, String name) {
        return model.getMicroservices()
            .stream()
            .filter(microservice -> microservice.getPlainName().equals(name))
            .findFirst()
            .orElse(null);
    }


    public static Operation resolveOperationName(MiSimModel model, String name) {
        return resolveOperationName(model.getArchitectureModel(), name);

    }

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
