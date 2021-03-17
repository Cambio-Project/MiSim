package de.rss.fachstudie.MiSim.entities.microservice;

import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.entities.patterns.NetworkPattern;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.entities.patterns.RetryManager;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.parsing.PatternData;
import de.rss.fachstudie.MiSim.resources.CPUImpl;
import de.rss.fachstudie.MiSim.resources.CPUProcess;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lion Wagner
 */
public class MicroserviceInstance extends RequestSender implements IRequestUpdateListener {

    private final CPUImpl cpu;
    private final Microservice owner;
    private final int instanceID;

    private InstanceState state;
    private LinkedHashSet<Request> currentRequestsToHandle = new LinkedHashSet<>(); //Queue with only unique entries
    private LinkedHashSet<NetworkDependency> currentlyOpenDependencies = new LinkedHashSet<>(); //Queue with only unique entries

    private LinkedHashSet<RequestAnswer> currentAnswers = new LinkedHashSet<>(); //Contains all current outgoing answers
    private LinkedHashSet<InternalRequest> currentInternalSends = new LinkedHashSet<>(); //contains all current outgoing dependency requests

    private final MultiDataPointReporter reporter;

    private Set<Pattern> patterns = new HashSet<>();

    public MicroserviceInstance(Model model, String name, boolean showInTrace, Microservice microservice, int instanceID) {
        super(model, name, showInTrace);
        this.owner = microservice;
        this.instanceID = instanceID;
        this.cpu = new CPUImpl.OwnedCPU(model, String.format("%s_CPU", name), showInTrace, microservice.getCapacity(), this);

        String[] names = name.split("_");
        reporter = new MultiDataPointReporter(String.format("I%s_[%s]_", names[0], names[1]));

        changeState(InstanceState.CREATED);

        this.addUpdateListener(this);
    }

    public void activatePatterns(PatternData[] patterns) {
        this.patterns = Arrays.stream(patterns)
                .map(patternData -> patternData.getNewInstance(this))
                .collect(Collectors.toSet());
        this.patterns.stream()
                .filter(pattern -> pattern instanceof NetworkPattern)
                .forEach(pattern -> addUpdateListener((NetworkPattern) pattern));
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
            sendRequest("Request_Answer_" + request.getQuotedName(), answer, request.getRequester());

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
                currentlyOpenDependencies.add(dependency);

                Request internalRequest = new InternalRequest(getModel(), this.traceIsOn(), dependency, this);
                sendRequest(String.format("Send Cascading_Request for %s", request.getQuotedName()), internalRequest, dependency.getTarget_Service());

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


        patterns.forEach(Pattern::close);

        //clears all currently running calculations
        cpu.clear();

        //cancel all send answers and send current internal requests
        Stream.concat(currentAnswers.stream(), currentInternalSends.stream()).forEach(Request::cancelSending);

        //notify sender of currently handled requests, that the requests failed (TCP/behavior)
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
        if (request instanceof RequestAnswer) //specifically does not care about request answers failing.
        {
            currentAnswers.remove(request);
            return;
        }

        if (request instanceof InternalRequest)
            currentInternalSends.remove(request);

        if (patterns.stream().anyMatch(pattern -> pattern instanceof RetryManager)) {
            if (reason != RequestFailedReason.MAX_RETRIES_REACHED) {
                return;
            }
        }

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

        //cancel all internal requests  of the parent that are underway
        for (InternalRequest internalSend : currentInternalSends) {
            if (internalSend.getParent() == parentToCancel) {
                internalSend.cancelSending();
            }
        }
        currentlyOpenDependencies.removeAll(parentToCancel.getDependencies());
        currentRequestsToHandle.remove(parentToCancel);

        collectQueueStatistics(); //collecting Statistics
    }

    @Override
    public void onRequestArrivalAtTarget(Request request, TimeInstant when) {
        if (request instanceof RequestAnswer)
            currentAnswers.remove(request);

        collectQueueStatistics(); //collecting Statistics
    }

    @Override
    public void onRequestSend(Request request, TimeInstant when) {
        if (request instanceof RequestAnswer) {
            currentAnswers.add((RequestAnswer) request);
        } else if (request instanceof InternalRequest) {
            currentInternalSends.add((InternalRequest) request);
        }

        collectQueueStatistics(); //collecting Statistics
    }

    @Override
    public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        if (request instanceof InternalRequest)
            currentInternalSends.remove(request);

        collectQueueStatistics(); //collecting Statistics
    }

}
