package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.networking.*;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Retry implementation that employs a
 *
 * @author Lion Wagner
 */
public class RetryManager extends Entity implements IRequestUpdateListener {

    Map<NetworkDependency, Integer> requestIndex = new HashMap<>();
    private int maxTries = 5;
    private double baseBackoff = 0.004;
    private double maxBackoff = 1;
    private int exponentialBackoffBase = 3;
    private final IRequestUpdateListener listener;


    public RetryManager(Model model, String s, boolean b, IRequestUpdateListener listener) {
        super(model, s, b);
        this.listener = listener;

    }

    @Override
    public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) return;

        int tries = requestIndex.merge(dep, 1, Integer::sum);

        if (tries < maxTries) {
            double steadyDelay = Math.min(baseBackoff * Math.pow(exponentialBackoffBase, tries), maxBackoff);
            double jitterDelay = new Random().nextDouble() * steadyDelay;

            request = new InternalRequest(getModel(), this.traceIsOn(), dep, request.getHandler()); //updates the dependency that had the original request as child
            NetworkRequestEvent newSendEvent = new NetworkRequestSendEvent(getModel(), String.format("Retry of sending %s", request.getQuotedName()), true, request);
            request.addUpdateListener(this);
            newSendEvent.schedule(new TimeSpan(jitterDelay));

        } else {
            listener.onRequestFailed(request, when, reason);
        }
    }

    @Override
    public void onRequestArrivalAtTarget(Request request, TimeInstant when) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        requestIndex.remove(dep);
    }

    @Override
    public void onRequestSend(Request request, TimeInstant when) {
        if (!(request instanceof RequestAnswer)) //Request answers will not be repeated
        {
            NetworkDependency dep = request.getParent().getRelatedDependency(request);
            requestIndex.put(dep, 1);
        }

    }

    @Override
    public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        requestIndex.remove(dep);

    }

    public void clear() {
        requestIndex.clear();
    }
}
