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
 * Retry implementation that employs a jitter based exponential backoff. see https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/
 *
 * @author Lion Wagner
 */
public class RetryManager extends Entity implements IRequestUpdateListener {

    Map<NetworkDependency, Integer> requestIndex = new HashMap<>();
    private int maxTries = 10;
    private double baseBackoff = 0.010;
    private double maxBackoff = 1;
    private int exponentialBackoffBase = 3;
    private final IRetryListener listener;

    public RetryManager(Model model, String s, boolean b, IRetryListener listener) {
        super(model, s, b);
        this.listener = listener;
    }

    @Override
    public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) return;

        int tries = requestIndex.get(dep);

        if (tries < maxTries) {
            double steadyDelay = Math.min(baseBackoff * Math.pow(exponentialBackoffBase, tries), maxBackoff);
            double jitterDelay = new Random().nextDouble() * steadyDelay;

            Request newRequest = new InternalRequest(getModel(), this.traceIsOn(), dep, request.getRequester()); //updates the dependency that had the original request as child
            NetworkRequestSendEvent newSendEvent = new NetworkRequestSendEvent(getModel(), String.format("Retry of sending %s", request.getQuotedName()), true, newRequest, dep.getTarget_Service());
            newRequest.addUpdateListener(this);
            listener.onRetry(newRequest, newSendEvent);
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
            requestIndex.merge(dep, 1, Integer::sum);
        }

    }

    @Override
    public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        requestIndex.remove(dep);

    }

    public void clear() {
        requestIndex.clear();
        traceOn();
        sendTraceNote(String.format("Clearing Retry %s", this.getQuotedName()));
        traceOff();
    }
}
