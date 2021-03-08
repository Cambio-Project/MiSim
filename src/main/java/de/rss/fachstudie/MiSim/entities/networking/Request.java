package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.DependencyGraph;
import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.util.Precision;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Lion Wagner
 */
public abstract class Request extends MessageObject {
    protected static final DependencyGraph dependencyGraph = new DependencyGraph();

    public final Operation operation;
    private final Set<NetworkDependency> dependencyRequests = new HashSet<>();


    private MicroserviceInstance handler_instance; //microservice instance that collects dependencies of this request and computes it
    private boolean computation_completed = false;
    private boolean dependencies_completed = false;

    private final Request parent;
    private NetworkRequestSendEvent sendEvent;
    private NetworkRequestReceiveEvent receiveEvent;
    private NetworkRequestCanceledEvent canceledEvent;
    private IRequestUpdateListener updateListener; //TODO: minor: allow list of listeners so e.g. a tracing tool can be injected by each creation.

    private TimeInstant timestamp_send;
    private TimeInstant timestamp_received;
    private TimeInstant timestamp_received_at_handler;
    private TimeInstant timestamp_computation_completed;
    private TimeInstant timestamp_dependencies_completed;


    public Request(Model model, String name, boolean showInTrace, Operation operation) {
        this(model, name, showInTrace, null, operation);
    }

    public Request(Model model, String name, boolean showInTrace, Request parent, Operation operation) {
        super(model, name, showInTrace);
        this.operation = operation;
        createDependencies();
        if (dependencyRequests.isEmpty()) setDependencies_completed();
        this.parent = parent;
    }

    private void createDependencies() {

        for (Dependency dependency : operation.getDependencies()) {

            // Roll probability
            ContDistUniform prob = new ContDistUniform(getModel(), "", 0.0, 1.0, false, false);
            double probability = dependency.getProbability();
            if (prob.sample() <= probability) {

                Operation nextOperationEntity = dependency.getOperation_instance();

                NetworkDependency dep = new NetworkDependency(this, nextOperationEntity);

                dependencyRequests.add(dep);
                if (parent != null)
                    dependencyGraph.insertDependency(parent.getHandler().getOwner(), parent.operation, nextOperationEntity.getOwner(), nextOperationEntity, null);
            }
        }
    }

    public final Set<NetworkDependency> getDependencyRequests() {
        return dependencyRequests;
    }

    public final Request getParent() {
        return parent;
    }

    public final boolean hasParent() {
        return parent != null;
    }

    public final boolean isCompleted() {
        return computation_completed && dependencies_completed;
    }

    public boolean isComputation_completed() {
        return computation_completed;
    }

    public boolean isDependencies_completed() {
        return dependencies_completed;
    }

    public TimeInstant getTimestamp_received() {
        return timestamp_received;
    }

    public TimeInstant getTimestamp_send() {
        return timestamp_send;
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

    public final void setComputation_completed() {
        if (this.computation_completed)
            throw new IllegalStateException("Computation was already completed!");
        this.computation_completed = true;
        timestamp_computation_completed = presentTime();
        onComputationComplete();
        if (dependencies_completed && computation_completed) {
            onCompletion();
        }
        handler_instance.handle(this); //resubmitting itself for further handling
    }

    private void setDependencies_completed() {
        if (this.dependencies_completed)
            throw new IllegalStateException("Dependencies were already completed!");
        this.dependencies_completed = true;
        timestamp_dependencies_completed = presentTime();
        onDependenciesComplete();
        if (dependencies_completed && computation_completed) {
            onCompletion();
        }
        if (handler_instance != null && handler_instance.checkIfCanHandle(this))
             handler_instance.handle(this); //resubmitting itself for further handling
    }

    public final void stampReceived(TimeInstant stamp) {
        this.setTimestamp_received(stamp);
        onReceive();
    }

    public final void stampSendoff(TimeInstant stamp) {
        this.setTimestamp_send(stamp);
    }

    public final void stampReceivedAtHandler(TimeInstant stamp) {
        if (this.timestamp_received_at_handler != null)
            throw new IllegalStateException("This Request was already received by its handler.");
        this.timestamp_received_at_handler = stamp;
    }

    private void setTimestamp_received(TimeInstant timestamp_received) {
        if (this.timestamp_received != null)
            throw new IllegalStateException("Receive Stamp is already set!");
        this.timestamp_received = timestamp_received;
    }

    private void setTimestamp_send(TimeInstant timestamp_send) {
        if (this.timestamp_send != null)
            throw new IllegalStateException("Receive Stamp is already set!");
        this.timestamp_send = timestamp_send;
    }


    public final void notifyDependencyHasFinished(Request request) {
        for (NetworkDependency networkDependency : dependencyRequests) {
            if (networkDependency.getChild_request() == request) {
                if (networkDependency.isCompleted()) {
                    throw new IllegalStateException("Request cannot be finished twice!");
                } else
                    networkDependency.setCompleted();
            }
        }

        if (dependencyRequests.stream().allMatch(NetworkDependency::isCompleted)) {
            setDependencies_completed();
        }
    }

    public final double getResponseTime() {
        if (timestamp_send == null) {
            throw new IllegalStateException("Can't retrieve response time: Request was not send yet.");
        } else if (timestamp_received == null) {
            throw new IllegalStateException("Can't retrieve response time: Request was not received yet.");
        }

        double responsetime = timestamp_received.getTimeAsDouble() - timestamp_send.getTimeAsDouble();
        responsetime = Precision.round(responsetime, 15);
        return responsetime;
    }

    public final double getDependencyWaitTime() {
        return timestamp_dependencies_completed.getTimeAsDouble() - timestamp_send.getTimeAsDouble();
    }

    public final double getComputeTime() {
        return timestamp_computation_completed.getTimeAsDouble() - timestamp_dependencies_completed.getTimeAsDouble();
    }

    public final boolean areDependencies_completed() {
        return dependencies_completed;
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
        this.handler_instance = handler;
    }

    public MicroserviceInstance getHandler() {
        return handler_instance;
    }

    public IRequestUpdateListener getUpdateListener() {
        return updateListener;
    }

    public void setUpdateListener(IRequestUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

}
