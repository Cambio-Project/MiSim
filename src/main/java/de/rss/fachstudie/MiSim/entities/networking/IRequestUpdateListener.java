package de.rss.fachstudie.MiSim.entities.networking;

public interface IRequestUpdateListener {

    void onRequestFailed(Request request);

    void onRequestArrivalAtTarget(Request request);
}
