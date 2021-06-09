package de.unistuttgart.sqa.orcas.misim.entities.networking;

/**
 * Contains a collection of reasons, why a request can fail.
 */
public enum RequestFailedReason {
    NETWORK_FAILED,
    NO_INSTANCE_AVAILABLE,
    HANDLING_INSTANCE_DIED,
    REQUESTING_INSTANCE_DIED,
    ENDPOINT_DOES_NOT_EXIST,
    DEPENDENCY_NOT_AVAILABLE,
    TIMEOUT,
    MAX_RETRIES_REACHED,
    CIRCUIT_IS_OPEN, CONNECTION_VOLUME_LIMIT_REACHED, REQUEST_VOLUME_REACHED
}
