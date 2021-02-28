package de.rss.fachstudie.MiSim.entities.networking;

public enum RequestState {
    SEND,
    RECEIVED_AT_HANDLER,
    PARTIALLY_COMPLETED,
    COMPLETED,
    ANSWERED,
    CANCELED
}
