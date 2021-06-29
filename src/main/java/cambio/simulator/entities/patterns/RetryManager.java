package cambio.simulator.entities.patterns;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import cambio.simulator.entities.networking.InternalRequest;
import cambio.simulator.entities.networking.NetworkDependency;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.entities.networking.RequestAnswer;
import cambio.simulator.entities.networking.RequestFailedReason;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
import cambio.simulator.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Retry implementation that employs a full jitter based exponential backoff. Jittering can be turned off.
 *
 * @author Lion Wagner
 * @see <a href="https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/">Articel on Backoff and
 * Jitter Algorithms </a>
 */
public class RetryManager extends NetworkPattern implements IRequestUpdateListener {

    private static final MultiDataPointReporter reporter = new MultiDataPointReporter("RM");
    private static final List<Double> all = new LinkedList<>();
    /**
     * Dictonary that tracks how many retries were currently done for each active dependency.
     */
    private final Map<NetworkDependency, Integer> requestIndex = new HashMap<>();
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

    public RetryManager(Model model, String name, boolean showInTrace, MicroserviceInstance listener) {
        super(model, name, showInTrace, listener);
    }

    @Override
    public int getListeningPriority() {
        return Priority.VERY_HIGH;
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        if (reason == RequestFailedReason.MAX_RETRIES_REACHED) {
            return false; // if max retries reached, the Retry does not know how to handle the fail
        }

        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) {
            return false;
        }

        int tries = requestIndex.get(dep);

        if (tries < maxTries) {
            double delay = Math.min(baseBackoff * Math.pow(base, tries - 1), maxBackoff);

            if (jittering) {
                delay = new Random().nextDouble() * delay;
            }

            reporter.addDatapoint("RetryTimings", presentTime(), delay);
            all.add(delay);

            MicroserviceInstance handler = request.getHandler();

            Request newRequest = new InternalRequest(getModel(), this.traceIsOn(), dep,
                request.getRequester()); //updates the dependency that had the original request as child
            if (handler == null || tries == maxTries - 1) {
                owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedName()), newRequest,
                    dep.getTargetService(), new TimeSpan(delay));
            } else {
                owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedName()), newRequest, handler,
                    new TimeSpan(delay));
            }
            sendTraceNote(String.format("Try %d, send Request: %s", tries + 1, newRequest.getQuotedName()));
        } else {
            request.getUpdateListeners().forEach(iRequestUpdateListener -> iRequestUpdateListener
                .onRequestFailed(request, when, RequestFailedReason.MAX_RETRIES_REACHED));
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
        //Request answers will not be repeated
        if (!(request instanceof RequestAnswer)) {
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
