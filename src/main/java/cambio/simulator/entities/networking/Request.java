package cambio.simulator.entities.networking;

import java.util.Collection;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.math3.util.Precision;

/**
 * Represents a Request that can travel between two {@link MicroserviceInstance}s.
 *
 * @author Lion Wagner
 */
public abstract class Request extends NamedEntity {
    public final Operation operation;
    private final Set<ServiceDependencyInstance> dependencies = new HashSet<>();
    private final Request parent;
    private final MicroserviceInstance requester;
    private final PriorityQueue<IRequestUpdateListener> updateListeners = new PriorityQueue<>();
    private MicroserviceInstance handlerInstance;
    //microservice instance that collects dependencies of this request and computes it
    private boolean computationCompleted = false;
    private boolean dependenciesCompleted = false;
    private NetworkRequestSendEvent sendEvent;
    private NetworkRequestReceiveEvent receiveEvent;
    private NetworkRequestCanceledEvent canceledEvent;
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
            //TODO: clean up this mess (this call is made to neatly trigger onDependenciesComplete)
            notifyDependencyHasFinished(null);
        }
    }


    private void createDependencies() {
        // Roll probability
        Random prob;
        prob = new Random(((MiSimModel) getModel()).getExperimentMetaData().getSeed()); //TODO: resolve this mess

        for (DependencyDescription dependencyDescription : operation.getDependencyDescriptions()) {


            double probability = dependencyDescription.getProbability();
            double sample = prob.nextDouble();
            if (sample <= probability) {

                Operation nextOperationEntity = dependencyDescription.getTargetOperation();

                ServiceDependencyInstance dep = new ServiceDependencyInstance(getModel(), this, nextOperationEntity,
                    dependencyDescription);

                dependencies.add(dep);
            }
        }
    }

    public final Set<ServiceDependencyInstance> getDependencies() {
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

    /**
     * Marks the point in time this request was received at the requester.
     */
    private void setTimestampReceived(TimeInstant timestampReceived) {
        if (this.timestampReceived != null) {
            throw new IllegalStateException("Receive Stamp is already set!");
        }
        this.timestampReceived = timestampReceived;
    }

    public TimeInstant getTimestampSend() {
        return timestampSend;
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

    public void resetSendTimeStamps() {
        timestampSend = null;
    }

    public void setSendEvent(NetworkRequestSendEvent sendEvent) {
        this.sendEvent = sendEvent;
    }

    public void setReceiveEvent(NetworkRequestReceiveEvent receiveEvent) {
        this.receiveEvent = receiveEvent;
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
     * Tells this request that one {@link ServiceDependencyInstance} has finished.
     *
     * @param dep dependency that was completed
     * @return whether all dependencies are completed
     */
    public boolean notifyDependencyHasFinished(ServiceDependencyInstance dep) {
        if (this.dependenciesCompleted) {
            throw new IllegalStateException("Dependencies were already completed!");
        }

        long uncompletedCount =
            dependencies.stream().filter(networkDependency -> !networkDependency.isCompleted()).count();

        if (dep != null) {
            if (!dependencies.contains(dep)) {
                throw new IllegalStateException("This dependency is not part of this Request");
            }
            dep.setCompleted();

            this.sendTraceNote(String.format("Completed Dependency \"%s\".", dep));
            this.sendTraceNote(String.format("Remaining Dependencies: %d.", uncompletedCount - 1));
        }

        if (uncompletedCount == 0) {
            this.dependenciesCompleted = true;
            onDependenciesComplete();
            if (dependenciesCompleted && computationCompleted) {
                onCompletion();
            }
            this.sendTraceNote(String.format("Dependencies of Request \"%s\" are completed.", this.getName()));
            return true;
        }
        return false;
    }


    /**
     * Gets the {@link ServiceDependencyInstance} that should be completed by the given request.
     *
     * @param request child request of this request.
     * @return the {@link ServiceDependencyInstance} that is related to the given request, {@code null} otherwise.
     */
    public ServiceDependencyInstance getRelatedDependency(Request request) {
        for (ServiceDependencyInstance serviceDependencyInstance : dependencies) {
            if (serviceDependencyInstance.getChildRequest() == request) {
                return serviceDependencyInstance;
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

    public MicroserviceInstance getHandler() {
        return handlerInstance;
    }

    public void setHandler(MicroserviceInstance handler) {
        this.handlerInstance = handler;
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
