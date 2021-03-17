package de.rss.fachstudie.MiSim.entities.networking;

public enum RequestFailedReason {
    NETWORK_FAILED,
    NO_INSTANCE_AVAILABLE,
    HANDLING_INSTANCE_DIED,
    REQUESTING_INSTANCE_DIED,
    ENDPOINT_DOES_NOT_EXIST,
    DEPENDENCY_NOT_AVAILABLE,
    MAX_RETRIES_REACHED;
}
