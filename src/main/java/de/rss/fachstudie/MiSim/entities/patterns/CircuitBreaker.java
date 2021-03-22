package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Manager class of all CircuitBreakers of one Microservice Instance.
 */
public final class CircuitBreaker extends NetworkPattern implements IRequestUpdateListener {

    @FromJson
    private int requestVolumeThreshold = Integer.MAX_VALUE;
    @FromJson
    private double errorThresholdPercentage = Double.POSITIVE_INFINITY;
    @FromJson
    private double sleepWindow = 0.500;
    @FromJson
    private int timeout = Integer.MAX_VALUE;
    @FromJson
    private int rollingWindow = 20; //window over which error rates are collected

    private final Set<NetworkDependency> activeConnections = new HashSet<>();
    private final Map<Microservice, CircuitBreakerState> breakerStates = new HashMap<>();

    private final MultiDataPointReporter reporter;


    public CircuitBreaker(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace, owner);
        reporter = new MultiDataPointReporter(String.format("CB[%s]_", name));
    }

    @Override
    public int getListeningPriority() {
        return Priority.HIGH;
    }

    public int getRollingWindow() {
        return rollingWindow;
    }

    public int getRequestVolumeThreshold() {
        return requestVolumeThreshold;
    }

    public double getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }

    public double getSleepWindow() {
        return sleepWindow;
    }

    public int getTimeout() {
        return timeout;
    }


    @Override
    public void shutdown() {
        activeConnections.clear();
        breakerStates.clear();
        collectData(presentTime());
    }

    @Override
    public boolean onRequestSend(Request request, TimeInstant when) {
        if (!(request instanceof InternalRequest))
            return false; //ignore everything except InternalRequests (i.e. RequestAnswers)

        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        Microservice target = dep.getTarget_Service();
        activeConnections.add(dep);
        CircuitBreakerState state = breakerStates.computeIfAbsent(target,
                monitoredService -> new CircuitBreakerState(monitoredService, this.errorThresholdPercentage, rollingWindow, sleepWindow));


        boolean consumed = false;
        if (state.isOpen()) {
//            owner.updateListenerProxy.onRequestFailed(request, when, RequestFailedReason.CIRCUIT_IS_OPEN);
            request.cancelSending();
            NetworkRequestEvent cancelEvent = new NetworkRequestCanceledEvent(getModel(), String.format("Canceling of %s", request.getQuotedName()),true,request,RequestFailedReason.CIRCUIT_IS_OPEN);
            cancelEvent.schedule();
            consumed = true;
        } else {
            int currentActiveConnections = (int) (activeConnections.stream().filter(dependency -> dependency.getTarget_Service() == target).count());
            if (currentActiveConnections > requestVolumeThreshold) {
                state.notifyArrivalFailure();
                owner.updateListenerProxy.onRequestFailed(request, when, RequestFailedReason.CONNECTION_VOLUME_LIMIT_REACHED);
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
        if (!(request instanceof InternalRequest))
            return false; //ignore everything except InternalRequests (i.e. RequestAnswers)

        NetworkDependency dep = request.getParent().getRelatedDependency(request);
        Microservice target = dep.getTarget_Service();

        activeConnections.remove(dep);

        breakerStates.get(target).notifySuccessfulCompletion();

        collectData(when);
        return false;
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        if (!(request instanceof InternalRequest))
            return false; //ignore everything except InternalRequests (i.e. RequestAnswers)

        InternalRequest internal_request = (InternalRequest) request;

        NetworkDependency dep = internal_request.getDependency();
        if (dep.getChild_request() != internal_request) {
            //dependency was asinged a new child Request already (e.g due to a retry), therefore we ignore the request
            return false;
        }

        Microservice target = dep.getTarget_Service();
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
            reporter.addDatapoint(String.format("[%s]", microservice.getName()), when, circuitBreakerState.getCurrentStatistics());
        }
    }
}
