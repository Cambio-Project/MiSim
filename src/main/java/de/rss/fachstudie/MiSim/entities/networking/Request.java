package de.rss.fachstudie.MiSim.entities.networking;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.misc.Util;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.util.Precision;

/**
 * Represents a Request that can travel between two {@link MicroserviceInstance}s.
 *
 * @author Lion Wagner
 */
public abstract class Request extends Entity {
    public final Operation operation;
    private final Set<NetworkDependency> dependencies = new HashSet<>();


    private MicroserviceInstance handlerInstance;
    //microservice instance that collects dependencies of this request and computes it
    private boolean computationCompleted = false;
    private boolean dependenciesCompleted = false;

    private final Request parent;
    private final MicroserviceInstance requester;
    private NetworkRequestSendEvent sendEvent;
    private NetworkRequestReceiveEvent receiveEvent;
    private NetworkRequestCanceledEvent canceledEvent;
    private final PriorityQueue<IRequestUpdateListener> updateListeners = new PriorityQueue<>();

    private TimeInstant timestampSend;
    private TimeInstant timestampReceived;
    private TimeInstant timestampReceivedAtHandler;
    private TimeInstant timestampComputationCompleted;
    private TimeInstant timestampDependenciesCompleted;


    protected Request(Model model, String name, boolean showInTrace, Request parent, Operation operation,
                      MicroserviceInstance requester) {
        super(model, name, showInTrace);
        this.operation = operation;
        this.requester = requester;
        this.parent = parent;
        createDependencies();
        if (dependencies.isEmpty()) {
            notifyDependencyHasFinished(null);
        }
    }


    private void createDependencies() {

        for (Dependency dependency : operation.getDependencies()) {

            // Roll probability
            Random prob;
            prob = Util.tryGetRandomFromExperimentSeed();

            double probability = dependency.getProbability();
            double sample = prob.nextDouble();
            if (sample <= probability) {

                Operation nextOperationEntity = dependency.getTargetOperation();

                NetworkDependency dep = new NetworkDependency(getModel(), this, nextOperationEntity, dependency);

                dependencies.add(dep);
            }
        }
    }

    public final Set<NetworkDependency> getDependencies() {
        return dependencies;
    }

    public final Request getParent() {
        return parent;
    }

    public final boolean hasParent() {
        return parent != null;
    }

    public MicroserviceInstance getRequester() {
        return requester;
    }

    public final boolean isCompleted() {
        return computationCompleted && dependenciesCompleted;
    }

    public boolean isComputationCompleted() {
        return computationCompleted;
    }

    public boolean isDependenciesCompleted() {
        return dependenciesCompleted;
    }

    public TimeInstant getTimestampReceived() {
        return timestampReceived;
    }

    public TimeInstant getTimestampSend() {
        return timestampSend;
    }

    public void resetSendTimeStamps() {
        timestampSend = null;
    }

    public void setSendEvent(NetworkRequestSendEvent sendEvent) {
        this.sendEvent = sendEvent;
    }

    public void setReceiveEvent(NetworkRequestReceiveEvent receiveEvent) {
        this.receiveEvent = receiveEvent;
    }

    public void setCanceledEvent(NetworkRequestCanceledEvent canceledEvent) {
        this.canceledEvent = canceledEvent;
    }

    /**
     * Marks the request computation demand as fulfilled at the present time.
     */
    public final void setComputationCompleted() {
        if (this.computationCompleted) {
            throw new IllegalStateException("Computation was already completed!");
        }
        this.computationCompleted = true;
        timestampComputationCompleted = presentTime();
        onComputationComplete();
        if (dependenciesCompleted && computationCompleted) {
            onCompletion();
        }
    }


    //    private void setDependenciesCompleted() {
    //        if (this.dependenciesCompleted) {
    //            throw new IllegalStateException("Dependencies were already completed!");
    //        }
    //        this.dependenciesCompleted = true;
    //        timestampDependenciesCompleted = presentTime();
    //        onDependenciesComplete();
    //        if (dependenciesCompleted && computationCompleted) {
    //            onCompletion();
    //        }
    //        if (handlerInstance != null && handlerInstance.checkIfCanHandle(this)) {
    //            handlerInstance.handle(this); //resubmitting itself for further handling
    //        }
    //    }

    public final void stampReceived(TimeInstant stamp) {
        this.setTimestampReceived(stamp);
        onReceive();
    }

    /**
     * Marks the point in time this request was send.
     */
    public final void stampSendoff(TimeInstant stamp) {
        this.setTimestampSend(stamp);
    }

    /**
     * Marks the point in time this request was received at a handler.
     */
    public final void stampReceivedAtHandler(TimeInstant stamp) {
        if (this.timestampReceivedAtHandler != null) {
            throw new IllegalStateException("This Request was already received by its handler.");
        }
        this.timestampReceivedAtHandler = stamp;
    }

    /**
     * Marks the point in time this request was received at the requester.
     */
    private void setTimestampReceived(TimeInstant timestampReceived) {
        if (this.timestampReceived != null) {
            throw new IllegalStateException("Receive Stamp is already set!");
        }
        this.timestampReceived = timestampReceived;
    }

    /**
     * Marks the point in time this request was send.
     */
    private void setTimestampSend(TimeInstant timestampSend) {
        if (this.timestampSend != null) {
            throw new IllegalStateException("Send Stamp is already set!");
        }
        this.timestampSend = timestampSend;
    }

    /**
     * Tells this request that one {@link NetworkDependency} has finished.
     *
     * @param dep dependency that was completed
     */
    public boolean notifyDependencyHasFinished(NetworkDependency dep) {
        if (this.dependenciesCompleted) {
            throw new IllegalStateException("Dependencies were already completed!");
        }

        if (dep != null) {
            if (!dependencies.contains(dep)) {
                throw new IllegalStateException("This dependency is not part of this Request");
            }
            dep.setCompleted();
        }

        if ((dependencies.stream().allMatch(NetworkDependency::isCompleted))) {
            this.dependenciesCompleted = true;
            onDependenciesComplete();
            if (dependenciesCompleted && computationCompleted) {
                onCompletion();
            }
            return true;
        }
        return false;
    }


    /**
     * Gets the {@link NetworkDependency} that should be completed by the given request.
     *
     * @param request child request of this request.
     * @return the {@link NetworkDependency} that is related to the given request, {@code null} otherwise.
     */
    public NetworkDependency getRelatedDependency(Request request) {
        for (NetworkDependency networkDependency : dependencies) {
            if (networkDependency.getChildRequest() == request) {
                return networkDependency;
            }
        }
        return null;
    }

    /**
     * Calculates the response time of this request.
     *
     * @return a double, describing the response time with the reference unit.
     */
    public final double getResponseTime() {
        if (timestampSend == null) {
            throw new IllegalStateException("Can't retrieve response time: Request was not send yet.");
        } else if (timestampReceived == null) {
            throw new IllegalStateException("Can't retrieve response time: Request was not received yet.");
        }

        double responsetime = timestampReceived.getTimeAsDouble() - timestampSend.getTimeAsDouble();
        responsetime = Precision.round(responsetime, 15);
        return responsetime;
    }

    public final double getDependencyWaitTime() {
        return timestampDependenciesCompleted.getTimeAsDouble() - timestampSend.getTimeAsDouble();
    }

    public final double getComputeTime() {
        return timestampComputationCompleted.getTimeAsDouble() - timestampDependenciesCompleted.getTimeAsDouble();
    }

    public final boolean areDependenciesCompleted() {
        return dependenciesCompleted;
    }


    /*
     * Interface for subclasses to monitor their state, while keeping core functions hidden.
     * these are essentially Events in a programmatic sense (like in C#, not like in DES)
     */
    protected void onDependenciesComplete() {
        sendDebugNote(String.format("Dependencies Completed: %s", getQuotedName()));
    }

    protected void onComputationComplete() {
        sendDebugNote(String.format("Computation Completed: %s", getQuotedName()));
    }

    protected void onCompletion() {
        sendDebugNote(String.format("Completed %s!", getQuotedName()));
    }

    protected void onReceive() {
        sendDebugNote(String.format("Arrived at Parent %s!", getQuotedName()));
    }

    public void setHandler(MicroserviceInstance handler) {
        this.handlerInstance = handler;
    }

    public MicroserviceInstance getHandler() {
        return handlerInstance;
    }

    public Collection<IRequestUpdateListener> getUpdateListeners() {
        return updateListeners;
    }

    /**
     * Adds a new {@link IRequestUpdateListener} to the request.
     *
     * @param updateListener listener to add.
     */
    public void addUpdateListener(IRequestUpdateListener updateListener) {
        this.updateListeners.add(updateListener);
    }


    /**
     * Cancels the sending process of this request. Also prevents it from starting.
     */
    public void cancelSending() {
        if (sendEvent.isScheduled()) {
            sendEvent.cancel();
        }
        sendEvent.setCanceled();
    }

    /**
     * Use, to cancel this request.
     */
    public void cancelExecutionAtHandler() {
        Request request = this;
        NetworkRequestEvent cancelEvent =
            new NetworkRequestCanceledEvent(getModel(), String.format("CANCEL Event for %s", request.getQuotedName()),
                request.traceIsOn(), request, RequestFailedReason.HANDLING_INSTANCE_DIED);
        cancelEvent.schedule(presentTime());
        request.canceledEvent = canceledEvent;
    }


}
