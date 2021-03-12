package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.networking.NetworkRequestEvent;
import de.rss.fachstudie.MiSim.entities.networking.NetworkRequestSendEvent;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import de.rss.fachstudie.MiSim.entities.networking.RequestFailedReason;
import desmoj.core.simulator.TimeInstant;

public interface IRetryListener {

    void onRetry(Request newRequest, NetworkRequestSendEvent event);

    void onRequestFailed(Request newRequest, TimeInstant when, RequestFailedReason reason);
}
