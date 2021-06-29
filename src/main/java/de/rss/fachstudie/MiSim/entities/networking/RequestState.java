package de.rss.fachstudie.MiSim.entities.networking;

/**
 * TODO: integration of this state definitions.
 * Currently this is only planned for a more informative simulation.
 */
public enum RequestState {
    SEND,
    RECEIVED_AT_HANDLER,
    PARTIALLY_COMPLETED,
    COMPLETED,
    ANSWERED,
    CANCELED
}
