package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.models.ExperimentMetaData;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.util.Precision;

import java.util.*;

/**
 * @author Lion Wagner
 */
public abstract class Request extends Entity {
    public final Operation operation;
    private final Set<NetworkDependency> dependencies = new HashSet<>();


    private MicroserviceInstance handler_instance; //microservice instance that collects dependencies of this request and computes it
    private boolean computation_completed = false;
    private boolean dependencies_completed = false;

    private final Request parent;
    private final MicroserviceInstance requester;
    private NetworkRequestSendEvent sendEvent;
    private NetworkRequestReceiveEvent receiveEvent;
    private NetworkRequestCanceledEvent canceledEvent;
    private final PriorityQueue<IRequestUpdateListener> updateListeners = new PriorityQueue<>(); //TODO: minor: allow list of listeners so e.g. a tracing tool can be injected by each creation.

    private TimeInstant timestamp_send;
    private TimeInstant timestamp_received;
    private TimeInstant timestamp_received_at_handler;
    private TimeInstant timestamp_computation_completed;
    private TimeInstant timestamp_dependencies_completed;


    public Request(Model model, String name, boolean showInTrace, Request parent, Operation operation, MicroserviceInstance requester) {
        super(model, name, showInTrace);
        this.operation = operation;
        this.requester = requester;
        this.parent = parent;
        createDependencies();
        if (dependencies.isEmpty()) notifyDependencyHasFinished(null);
    }


    private void createDependencies() {

        for (Dependency dependency : operation.getDependencies()) {

            // Roll probability
            Random prob = new Random(ExperimentMetaData.get().getSeed());
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

    public void resetSendTimeStamps() {
        timestamp_send = null;
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
            throw new IllegalStateException("Send Stamp is already set!");
        this.timestamp_send = timestamp_send;
    }

    public boolean notifyDependencyHasFinished(NetworkDependency dep) {
        if (this.dependencies_completed)
            throw new IllegalStateException("Dependencies were already completed!");

        if (dep != null) {
            if (!dependencies.contains(dep))
                throw new IllegalStateException("This dependency is not part of this Request");
            dep.setCompleted();
        }

        if ((dependencies.stream().allMatch(NetworkDependency::isCompleted))) {
            this.dependencies_completed = true;
            onDependenciesComplete();
            if (dependencies_completed && computation_completed) {
                onCompletion();
            }
            return true;
        }
        return false;
    }


    public NetworkDependency getRelatedDependency(Request request) {
        for (NetworkDependency networkDependency : dependencies) {
            if (networkDependency.getChild_request() == request) {
                return networkDependency;
            }
        }
        return null;
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

    public Collection<IRequestUpdateListener> getUpdateListeners() {
        return updateListeners;
    }

    public void addUpdateListener(IRequestUpdateListener updateListener) {
        this.updateListeners.add(updateListener);
    }


    public void cancelSending() {
        if (sendEvent.isScheduled())
            sendEvent.cancel();
        sendEvent.setCanceled();
    }

    /**
     * used to cancel this request
     */
    public void cancelExecutionAtHandler() {
        //TODO: delay could be added here to simulate sending of the HTTP/TCP connection failure
        //Be careful: then messages can arrive at killed services.


        Request request = this; //unpack if request is an
//        if(request instanceof RequestAnswer)
//            request = ((RequestAnswer) request).unpack();


        // (may be fixed) TODO: minor: Completed UserRequests sometimes appear here if a MicroserviceInstance#shutdown call has priority over the IRequestUpdateListener#onRequestArrivalAtTarget
//        if (request instanceof UserRequest && request.isCompleted()) {
//            return;
//        }
        NetworkRequestEvent cancelEvent = new NetworkRequestCanceledEvent(getModel(), String.format("CANCEL Event for %s", request.getQuotedName()), request.traceIsOn(), request, RequestFailedReason.HANDLING_INSTANCE_DIED);
        cancelEvent.schedule(presentTime());
        request.canceledEvent = canceledEvent;
    }


}
