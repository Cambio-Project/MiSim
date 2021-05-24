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
 * Jittering can be turned off.
 *
 * @author Lion Wagner
 * @see <a href=https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/>Articel on Backoff and Jitter Algorithms </a>
 */
public class RetryManager extends NetworkPattern implements IRequestUpdateListener {

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int maxTries = 5;

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double baseBackoff = 0.010;

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double maxBackoff = 1;

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private int base = 3;

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private boolean jittering = false;

    /**
     * Dictonary that tracks how many retries were currently done for each active dependencie
     */
    private final Map<NetworkDependency, Integer> requestIndex = new HashMap<>();

    public RetryManager(Model model, String name, boolean showInTrace, MicroserviceInstance listener) {
        super(model, name, showInTrace, listener);
    }

    @Override
    public int getListeningPriority() {
        return Priority.VERY_HIGH;
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        if (reason == RequestFailedReason.MAX_RETRIES_REACHED) return false; // if max retries reached, the Retry does not know how to handle the fail

        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) return false;

        int tries = requestIndex.get(dep);

        if (tries < maxTries) {
            double delay = Math.min(baseBackoff * Math.pow(base, tries), maxBackoff);

            if (jittering) {
                delay = new Random().nextDouble() * delay;
            }

            Request newRequest = new InternalRequest(getModel(), this.traceIsOn(), dep, request.getRequester()); //updates the dependency that had the original request as child
            owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedName()), newRequest, dep.getTarget_Service(), new TimeSpan(delay));
            sendTraceNote(String.format("Try %d, send Request: %s", tries + 1, newRequest.getQuotedName()));
        } else {
            request.getUpdateListeners().forEach(iRequestUpdateListener -> iRequestUpdateListener.onRequestFailed(request, when, RequestFailedReason.MAX_RETRIES_REACHED));
            //owner.updateListenerProxy.onRequestFailed(request, when, RequestFailedReason.MAX_RETRIES_REACHED); //notify everyone that a request failed
            sendTraceNote(String.format("Max Retries Reached for Dependency %s", dep));
            return true;
        }
        return false;
    }

    @Override
    public boolean onRequestArrivalAtTarget(Request request, TimeInstant when) {
        return false;
    }

    @Override
    public boolean onRequestSend(Request request, TimeInstant when) {
        if (!(request instanceof RequestAnswer)) //Request answers will not be repeated
        {
            NetworkDependency dep = request.getParent().getRelatedDependency(request);
            requestIndex.merge(dep, 1, Integer::sum);
        }
        return false;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        if (request.getParent() == null) {
            return true;
        }
        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        requestIndex.remove(dep);
        return false;
    }

    @Override
    public void shutdown() {
        requestIndex.clear();
        traceOn();
        sendTraceNote(String.format("Clearing Retry %s", this.getQuotedName()));
        traceOff();
    }
}
