package cambio.simulator.entities.patterns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import cambio.simulator.entities.networking.InternalRequest;
import cambio.simulator.entities.networking.NetworkRequestCanceledEvent;
import cambio.simulator.entities.networking.NetworkRequestEvent;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.entities.networking.RequestFailedReason;
import cambio.simulator.entities.networking.ServiceDependencyInstance;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
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
 * @see CircuitBreakerState
 * @see Microservice
 * @see MicroserviceInstance
 */
@JsonTypeName("circuitbreaker")
public final class CircuitBreaker extends InstanceOwnedPattern implements IRequestUpdateListener {

    private final Set<ServiceDependencyInstance> activeConnections = new HashSet<>();
    private final Map<Microservice, CircuitBreakerState> breakerStates = new HashMap<>();
    private final Map<Microservice, Integer> activeConnectionCount = new HashMap<>();
    private final MultiDataPointReporter reporter;

    @Expose
    private int requestVolumeThreshold = Integer.MAX_VALUE;
    @Expose
    @SerializedName(value = "error_threshold_percentage", alternate = "threshold")
    private double errorThresholdPercentage = 0.71;
    @Expose
    private double sleepWindow = 30;
    @Expose
    private int timeout = Integer.MAX_VALUE;
    @Expose
    private int rollingWindow = 3; //window over which error rates are collected

    public boolean waitsForHalfOpen = false;

    public CircuitBreaker(Model model, String name, boolean showInTrace) {
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
        CircuitBreakerState state = breakerStates.computeIfAbsent(target,
            monitoredService -> new CircuitBreakerState(monitoredService, this.errorThresholdPercentage, rollingWindow,
                sleepWindow, this));


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
                state.notifyArrivalFailure();
                owner.updateListenerProxy
                    .onRequestFailed(request, when, RequestFailedReason.CONNECTION_VOLUME_LIMIT_REACHED);
                consumed = true;
            }
        }
        collectData(when);
        return consumed;
    }

    @Override
    public boolean onRequestArrivalAtTarget(Request request, TimeInstant when) {
        collectData(when);
        return false;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        if (!(request instanceof InternalRequest)) {
            return false; //ignore everything except InternalRequests (e.g. RequestAnswers)
        }

        ServiceDependencyInstance dep = request.getParent().getRelatedDependency(request);
        Microservice target = dep.getTargetService();

        if (target == this.owner.getOwner()) { //prevents the circuit breaker from reacting to unpacked RequestAnswers
            return false;
        }

        activeConnections.remove(dep);
        activeConnectionCount.merge(target, -1, Integer::sum);

        breakerStates.get(target).notifySuccessfulCompletion();

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
        if (dep.getChildRequest() != internalRequest) {
            //dependency was asinged a new child Request already (e.g due to a retry), therefore we ignore the request
            return false;
        }

        Microservice target = dep.getTargetService();
        if (activeConnections.remove(dep)) {
            breakerStates.get(target).notifyArrivalFailure();
        }

        collectData(when);
        return false;
    }


    private void collectData(TimeInstant when) {
        for (Map.Entry<Microservice, CircuitBreakerState> entry : breakerStates.entrySet()) {
            Microservice microservice = entry.getKey();
            CircuitBreakerState circuitBreakerState = entry.getValue();
            reporter.addDatapoint(String.format("[%s]", microservice.getName()), when,
                circuitBreakerState.getCurrentStatistics());
        }
    }
}
