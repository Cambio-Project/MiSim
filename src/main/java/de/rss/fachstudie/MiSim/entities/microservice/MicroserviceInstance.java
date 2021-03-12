package de.rss.fachstudie.MiSim.entities.microservice;

import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.entities.patterns.IRetryListener;
import de.rss.fachstudie.MiSim.entities.patterns.RetryManager;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.resources.CPUImpl;
import de.rss.fachstudie.MiSim.resources.CPUProcess;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Lion Wagner
 */
public class MicroserviceInstance extends Entity implements IRequestUpdateListener, IRetryListener {

    private final CPUImpl cpu;
    private final Microservice owner;
    private final int instanceID;

    private InstanceState state;
    private LinkedHashSet<Request> currentRequestsToHandle = new LinkedHashSet<>(); //Queue with only unique entries
    private LinkedHashSet<NetworkDependency> currentlyOpenDependencies = new LinkedHashSet<>(); //Queue with only unique entries

    private LinkedHashSet<NetworkRequestSendEvent> currentAnswers = new LinkedHashSet<>(); //Contains all current outgoing answers
    private LinkedHashSet<NetworkRequestSendEvent> currentInternalSends = new LinkedHashSet<>(); //contains all current outgoing dependency requests

    private final MultiDataPointReporter reporter;

    private final RetryManager retryManager;

    /**
     * Observer/Listener of requests send by this instance.
     */
    private final IRequestUpdateListener sendCompletionListener = new IRequestUpdateListener() {
        public MicroserviceInstance owner;

        @Override
        public void onRequestArrivalAtTarget(Request request, TimeInstant when) {
            removeSendEvents(request);
        }

        @Override
        public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
            removeSendEvents(request);
        }

        private void removeSendEvents(Request request) {
            if (request instanceof RequestAnswer) { //answer arrived successfully at the target
                NetworkRequestSendEvent sendEvent = currentAnswers
                        .stream()
                        .filter(networkRequestSendEvent -> networkRequestSendEvent.getTraveling_request() == request)
                        .findAny()
                        .orElse(null);
                currentInternalSends.remove(sendEvent);

            } else if (request instanceof InternalRequest) { //dependency request arrives at target
                NetworkRequestSendEvent sendEvent = currentAnswers
                        .stream()
                        .filter(networkRequestSendEvent -> networkRequestSendEvent.getTraveling_request() == request)
                        .findAny()
                        .orElse(null);
                currentAnswers.remove(sendEvent);
            }
        }
    };

    public MicroserviceInstance(Model model, String name, boolean showInTrace, Microservice microservice, int instanceID) {
        super(model, name, showInTrace);
        this.owner = microservice;
        this.instanceID = instanceID;
        this.cpu = new CPUImpl.OwnedCPU(model, String.format("%s_CPU", name), showInTrace, microservice.getCapacity(), this);

        String[] names = name.split("_");
        reporter = new MultiDataPointReporter(String.format("I%s_[%s]_", names[0], names[1]));

        changeState(InstanceState.CREATED);

        try {
            sendCompletionListener.getClass().getField("owner").set(sendCompletionListener, this);//injecting this as owner
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }


        retryManager = new RetryManager(model, String.format("RetryManager of %s", this.getQuotedName()), false, this);
    }

    public double getUsage() {
        return this.cpu.getCurrentRelativeWorkDemand();
    }

    public InstanceState getState() {
        return state;
    }


    public void handle(Request request) {
        Objects.requireNonNull(request);

        if (!checkIfCanHandle(request)) { //throw error if instance cannot handle the request
            throw new IllegalStateException(String.format("Cannot handle this Request. State: [%s]", this.state));
        }

        if (request instanceof RequestAnswer) {
            handleRequestAnswer((RequestAnswer) request);
        } else {
            handleIncomingRequest(request);
        }

        collectQueueStatistics(); //collecting Statistics
    }

    /**
     * Checks whether this Instance can handle the Request.
     *
     * @return true if this request will be handled, false otherwise
     */
    public boolean checkIfCanHandle(Request request) {
        //if the instance is running it can handle the request
        if ((state == InstanceState.RUNNING)) return true;

        //if the instance is shutting down but already received the request it can continue to finish it.
        // else the instance cant handle the instance
        return state == InstanceState.SHUTTING_DOWN && (currentRequestsToHandle.contains(request) || currentRequestsToHandle.contains(request.getParent()));
    }

    private void handleRequestAnswer(RequestAnswer answer) {
        Request answeredRequest = answer.unpack();

        if (!(answeredRequest instanceof InternalRequest))
            throw new IllegalArgumentException(String.format("Dont know how to handle a %s", answeredRequest.getClass().getSimpleName()));

        InternalRequest request = (InternalRequest) answeredRequest;
        NetworkDependency dep = request.getDependency();

        if (!currentlyOpenDependencies.remove(dep) || !currentRequestsToHandle.contains(dep.getParent_request())) {
            throw new IllegalStateException("This Request is not handled by this Instance");
        }

        Request parent = dep.getParent_request();
        if (parent.notifyDependencyHasFinished(dep)) {
            this.handle(parent);
        }
    }

    private void handleIncomingRequest(Request request) {

        if (currentRequestsToHandle.add(request)) { //register request and stamp as received if not already known
            request.setHandler(this);
        }

        //three possiblities:
        //1. request is completed -> send it back to its sender (target is retrieved by the SendEvent)
        //2. requests' dependecies were all recevied -> send it to the cpu for handling. The CPU will "send" it back to this method once its done.
        //3. request does have dependencies -> create internal request
        if (request.isCompleted()) {
            RequestAnswer answer = new RequestAnswer(request, this);
            answer.addUpdateListener(sendCompletionListener);
            NetworkRequestSendEvent sendEvent = new NetworkRequestSendEvent(getModel(), "Request_Answer_" + request.getQuotedName(), traceIsOn(), answer, request.getRequester());
            currentAnswers.add(sendEvent);
            sendEvent.schedule();//send away the answer

            int size = currentRequestsToHandle.size();
            currentRequestsToHandle.remove(request);
            assert currentRequestsToHandle.size() == size - 1;

            //shutdown after the last answer was send. It doesn't care if the original sender does not live anymore
            if (currentRequestsToHandle.isEmpty() && getState() == InstanceState.SHUTTING_DOWN) {
                InstanceShutdownEndEvent event = new InstanceShutdownEndEvent(getModel(), String.format("Instance %s Shutdown End", this.getQuotedName()), traceIsOn());
                event.schedule(this, presentTime());
            }

        } else if (request.getDependencies().isEmpty() || request.areDependencies_completed()) {
            CPUProcess newProcess = new CPUProcess(request);
            cpu.submitProcess(newProcess);
        } else {
            for (NetworkDependency dependency : request.getDependencies()) {

                Request internalRequest = new InternalRequest(getModel(), this.traceIsOn(), dependency, this);
                internalRequest.addUpdateListener(sendCompletionListener);
                internalRequest.addUpdateListener(retryManager);
                currentlyOpenDependencies.add(dependency);


                NetworkRequestSendEvent sendEvent = new NetworkRequestSendEvent(getModel(), String.format("Send Cascading_Request for %s", request.getQuotedName()), traceIsOn(), internalRequest, dependency.getTarget_Service());
                currentInternalSends.add(sendEvent);
                sendEvent.schedule(presentTime());
            }
        }

    }


    private void changeState(InstanceState targetState) {
        if (this.state == targetState)
            return;

        sendTraceNote(this.getQuotedName() + " changed to state " + targetState.name());
        reporter.addDatapoint("State", presentTime(), targetState.name());
        this.state = targetState;

    }

    public void start() {
        if (!(this.state == InstanceState.CREATED || this.state == InstanceState.SHUTDOWN)) {
            throw new IllegalStateException(String.format("Cannot start Instance %s: Was not recently created or Shutdown. (Current State [%s])", this.getQuotedName(), state.name()));
        }

        changeState(InstanceState.STARTING);

        changeState(InstanceState.RUNNING);

    }

    public final void startShutdown() {
        if (!(this.state == InstanceState.CREATED || this.state == InstanceState.SHUTDOWN)) {
            throw new IllegalStateException(String.format("Cannot start Instance %s: Was not recently created or Shutdown. (Current State [%s])", this.getQuotedName(), state.name()));
        }
        changeState(InstanceState.SHUTTING_DOWN);
    }

    public final void shutdown() {
        if (this.state != InstanceState.SHUTTING_DOWN) {
            throw new IllegalStateException(String.format("Cannot shutdown Instance %s: This instance has not started its shutdown. (Current State [%s])", this.getQuotedName(), state.name()));
        }
        changeState(InstanceState.SHUTDOWN);
    }

    public final void die() {
        if (this.state == InstanceState.KILLED) {
            throw new IllegalStateException(String.format("Cannot kill Instance %s: This instance was already killed. (Current State [%s])", this.getQuotedName(), state.name()));
        }
        changeState(InstanceState.KILLED);


        retryManager.clear();

        //clears all currently running calculations
        cpu.clear();

        //cancel all send answers and send current internal requests
        Stream.concat(currentAnswers.stream(), currentInternalSends.stream()).forEach(networkEvent -> {
            if (networkEvent.isScheduled()) networkEvent.cancel();
        });

        //TODO: notify sender of currently handled requests, that the requests failed (TCP/behavior)
        currentRequestsToHandle.forEach(Request::cancelExecutionAtHandler);


    }

    public final Microservice getOwner() {
        return owner;
    }

    public final int getInstanceID() {
        return instanceID;
    }


    private void collectQueueStatistics() {
        reporter.addDatapoint("SendOff_Internal_Requests", presentTime(), currentlyOpenDependencies.size());
        reporter.addDatapoint("Requests_InSystem", presentTime(), currentRequestsToHandle.size());
        reporter.addDatapoint("Requests_NotComputed", presentTime(), currentRequestsToHandle.stream().filter(request -> !request.isComputation_completed()).count());
        reporter.addDatapoint("Requests_WaitingForDependencies", presentTime(), currentRequestsToHandle.stream().filter(request -> !request.isDependencies_completed()).count());
    }

    @Override
    public void onRequestFailed(final Request request, final TimeInstant when, final RequestFailedReason reason) {
        //called if an internal request definitely failed (e.g after 5 retries or circuit breaker has no idea what to do)
        if (request instanceof RequestAnswer) //does not care about Request answeres.
            return;

        NetworkDependency failedDependency = request.getParent().getRelatedDependency(request);
        if (!currentlyOpenDependencies.contains(failedDependency) || !currentRequestsToHandle.contains(request.getParent())) {
            throw new IllegalArgumentException("The given request was not request by this Instance.");
        }

        Request parentToCancel = request.getParent();

        //cancel parent
        NetworkRequestEvent cancelEvent = new NetworkRequestCanceledEvent(getModel(),
                String.format("Canceling of request %s", parentToCancel.getQuotedName()),
                traceIsOn(),
                parentToCancel,
                RequestFailedReason.DEPENDENCY_NOT_AVAILABLE,
                String.format("Dependency %s", request.getQuotedName()));
        cancelEvent.schedule(presentTime());

        //cancel all internal requests of dependencies of the parent
        for (NetworkRequestSendEvent internalSend : currentInternalSends) {
            if (internalSend.isScheduled() && internalSend.getTraveling_request().getParent() == parentToCancel)
                internalSend.cancel();
        }
        currentlyOpenDependencies.removeAll(parentToCancel.getDependencies());
        currentRequestsToHandle.remove(parentToCancel);

        collectQueueStatistics(); //collecting Statistics
    }

    @Override
    public void onRetry(Request newRequest, NetworkRequestSendEvent event) {
        newRequest.addUpdateListener(sendCompletionListener);
        if (newRequest instanceof InternalRequest) {
            currentInternalSends.add(event);
        }
    }

}
