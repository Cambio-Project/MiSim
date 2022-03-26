package cambio.simulator.entities.patterns;

import java.util.*;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.*;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
import cambio.simulator.parsing.JsonTypeName;
import com.google.gson.annotations.Expose;
import desmoj.core.simulator.*;

/**
 * Retry implementation that employs a full jitter based exponential backoff. Jittering can be turned off.
 *
 * @author Lion Wagner
 * @see <a href="https://aws.amazon.com/de/blogs/architecture/exponential-backoff-and-jitter/">Articel on Backoff and
 *     Jitter Algorithms </a>
 */
@JsonTypeName("retry")
public class Retry extends StrategicInstanceOwnedPattern<IRetryStrategy> implements IRequestUpdateListener {

    private static final MultiDataPointReporter reporter = new MultiDataPointReporter("RM");
    private static final List<Double> all = new LinkedList<>();

    private final Map<ServiceDependencyInstance, Integer> requestIndex = new HashMap<>();

    @Expose
    private int maxTries = 5;


    public Retry(final Model model, final String name, final boolean showInTrace) {
        super(model, name, showInTrace);
        this.setStrategy(new JitteringExponentialBackoffRetryStrategy()); // set default value
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

        ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
        if (!requestIndex.containsKey(dep)) {
            return false;
        }

        int tries = requestIndex.get(dep);

        if (tries < maxTries) {
            double delay = strategy.getNextDelay(tries);

            reporter.addDatapoint("RetryTimings", presentTime(), delay);
            all.add(delay);

            MicroserviceInstance handler = request.getHandler();

            //wrap the dependency in a new request
            //also updates the child request of the dependency
            Request newRequest = new InternalRequest(getModel(), this.traceIsOn(), dep,
                request.getRequester());

            // TODO: check whether this is the correct behavior
            //if the request has a handler, it will be resent to the same one
            //            if (handler == null || tries == maxTries - 1) {
            //                owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedPlainName()),
            //                newRequest, dep.getTargetService(), new TimeSpan(delay));
            //            } else {
            //                owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedPlainName()),
            //                newRequest, handler, new TimeSpan(delay));
            //            }
            //for now we just send the request to the load balancer, which can decide which instance to use
            owner.sendRequest(String.format("Collecting dependency %s", dep.getQuotedPlainName()), newRequest,
                dep.getTargetService(), new TimeSpan(delay));
            sendTraceNote(String.format("Try %d, send Request: %s", tries + 1, newRequest.getQuotedPlainName()));
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
            ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
            requestIndex.merge(dep, 1, Integer::sum);
        }
        return false;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        if (request.getParent() == null) {
            return true;
        }
        ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
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
