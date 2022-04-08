package cambio.simulator.entities.patterns;

import java.util.*;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.*;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.JsonTypeName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Manager class of all CircuitBreakers of one Microservice Instance.
 *
 * <p>
 * Creates a {@code CircuitBreakerState} for each connection to another {@code Microservice} of the owning {@code
 * MicroserviceInstance}
 *
 * <p>
 * This class is a {@code NetworkPattern} and therefore monitors all requests send by its owning {@code
 * MicroserviceInstance}.
 *
 * @author Lion Wagner
 * @see CountingCircuitBreakerState
 * @see Microservice
 * @see MicroserviceInstance
 */
@JsonTypeName("circuitbreaker")
public final class CircuitBreaker extends InstanceOwnedPattern implements IRequestUpdateListener {

    private final Set<ServiceDependencyInstance> activeConnections = new HashSet<>();
    private final Map<Microservice, TimingWindowCircuitBreakerState> breakerStates = new HashMap<>();
    private final Map<Microservice, Integer> activeConnectionCount = new HashMap<>();
    private final MultiDataPointReporter reporter;

    @Expose
    private int requestVolumeThreshold = Integer.MAX_VALUE;
    @Expose
    @SerializedName(value = "error_threshold_percentage", alternate = "threshold")
    private double errorThresholdPercentage = Double.POSITIVE_INFINITY;
    @Expose
    private double sleepWindow = 0.500;
    @Expose
    private int rollingWindow = 20; //window over which error rates are collected

    public CircuitBreaker(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        reporter = new MultiDataPointReporter(String.format("CB[%s]_", name));
    }

    @Override
    public int getListeningPriority() {
        return Priority.HIGH;
    }

    @Override
    public void shutdown() {
        activeConnections.clear();
        activeConnectionCount.clear();
        breakerStates.clear();
        collectData(presentTime());
    }

    @Override
    public boolean onRequestSend(Request request, TimeInstant when) {
        if (!(request instanceof InternalRequest)) {
            return false; //ignore everything except InternalRequests (e.g. RequestAnswers)
        }

        ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
        Microservice target = dep.getTargetService();
        activeConnections.add(dep);
        activeConnectionCount.merge(target, 1, Integer::sum);
        TimingWindowCircuitBreakerState state = breakerStates.computeIfAbsent(target,
            monitoredService -> new TimingWindowCircuitBreakerState(monitoredService, this.errorThresholdPercentage,
                rollingWindow, sleepWindow));


        boolean consumed = false;
        if (state.isOpen()) {
            //owner.updateListenerProxy.onRequestFailed(request, when, RequestFailedReason.CIRCUIT_IS_OPEN);
            request.cancelSending();
            NetworkRequestEvent cancelEvent =
                new NetworkRequestCanceledEvent(getModel(), String.format("Canceling of %s", request.getQuotedName()),
                    true, request, RequestFailedReason.CIRCUIT_IS_OPEN);
            cancelEvent.schedule();
            consumed = true;
        } else {
            int currentActiveConnections = activeConnectionCount.get(target);
            if (currentActiveConnections > requestVolumeThreshold) {
                state.notifyArrivalFailure(when);
                owner.updateListenerProxy
                    .onRequestFailed(request, when, RequestFailedReason.CONNECTION_VOLUME_LIMIT_REACHED);
                consumed = true;
            }
        }
        collectData(when);
        return consumed;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        if (!(request instanceof InternalRequest)) {
            return false; //ignore everything except InternalRequests (e.g. RequestAnswers)
        }

        ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
        Microservice target;
        if (dep == null || (target = dep.getTargetService()) == this.owner.getOwner()) {
            //dep==null if the request is not related to any dependency anymore (e.g. due to a timeout and replacement)
            //target==this.owner.getOwner() if its a local "send-to-self" request
            //both cases we just ignore
            return false;
        }

        activeConnections.remove(dep);
        activeConnectionCount.merge(target, -1, Integer::sum);

        breakerStates.get(target).notifySuccessfulCompletion(when);

        collectData(when);
        return false;
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        if (!(request instanceof InternalRequest)) {
            return false; //ignore everything except InternalRequests (e.g. RequestAnswers)
        }

        InternalRequest internalRequest = (InternalRequest) request;
        ServiceDependencyInstance dep = internalRequest.getDependency();

        Microservice target = dep.getTargetService();
        if (activeConnections.remove(dep)) {
            breakerStates.get(target).notifyArrivalFailure(when);
            activeConnectionCount.merge(target, -1, Integer::sum);
        }

        collectData(when);
        return false;
    }


    private void collectData(TimeInstant when) {
        for (Map.Entry<Microservice, TimingWindowCircuitBreakerState> entry : breakerStates.entrySet()) {
            Microservice microservice = entry.getKey();
            TimingWindowCircuitBreakerState circuitBreakerState = entry.getValue();
            reporter.addDatapoint(String.format("[%s]", microservice.getName()), when,
                circuitBreakerState.getCurrentStatistics());
        }
    }

    @Override
    public void onInitializedCompleted(Model model) {
        super.onInitializedCompleted(model);
        if (errorThresholdPercentage != Double.POSITIVE_INFINITY
            && errorThresholdPercentage > 1) {
            if (errorThresholdPercentage <= 100) {
                System.out.println("Warning: errorThresholdPercentage is in between 1 and 100, dividing it by 100");
                errorThresholdPercentage /= 100.0;
            } else {
                throw new IllegalArgumentException(
                    "errorThresholdPercentage must be in between 0.0 and 1.0 or 1 and 100");
            }
        }
    }
}
