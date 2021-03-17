package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Retry implementation that employs a full jitter based exponential backoff.
 *
 * @author Lion Wagner
 * @see https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/
 */
public class RetryManager extends NetworkPattern implements IRequestUpdateListener {

    @FromJson
    private final int maxTries = 5;
    @FromJson
    private final double baseBackoff = 0.010;
    @FromJson
    private final double maxBackoff = 1;
    @FromJson
    private final int base = 3;
    @FromJson
    private final boolean jittering = true;

    private final Map<NetworkDependency, Integer> requestIndex = new HashMap<>();

    public RetryManager(Model model, String s, boolean b, MicroserviceInstance listener) {
        super(model, s, b, listener);
    }

    @Override
    public int getListeningPriority() {
        return Priority.HIGH;
    }

    @Override
    public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) return;

        int tries = requestIndex.get(dep);

        if (tries < maxTries) {
            double delay = Math.min(baseBackoff * Math.pow(base, tries), maxBackoff);

            if (jittering) delay = new Random().nextDouble() * delay;

            Request newRequest = new InternalRequest(getModel(), this.traceIsOn(), dep, request.getRequester()); //updates the dependency that had the original request as child
            owner.sendRequest(String.format("Retry of sending %s", request.getQuotedName()), newRequest, dep.getTarget_Service(), new TimeSpan(delay));
        } else {
            owner.onRequestFailed(request, when, RequestFailedReason.MAX_RETRIES_REACHED);
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
        if (request.getParent() == null) {
            return;
        }
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        requestIndex.remove(dep);
    }

    @Override
    public void close() {
        requestIndex.clear();
        traceOn();
        sendTraceNote(String.format("Clearing Retry %s", this.getQuotedName()));
        traceOff();
    }
}
