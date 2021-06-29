package cambio.simulator.entities.generator;

import java.util.Objects;

import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.entities.networking.RequestFailedReason;
import cambio.simulator.entities.networking.RequestSender;
import cambio.simulator.entities.networking.UserRequest;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.AccumulativeDataPointReporter;
import cambio.simulator.export.MultiDataPointReporter;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Superclass for all generators. Automatically takes care of the (re-)scheduling of the generation events, sending of
 * the {@code UserRequest}s and observation of send requests.
 *
 * @author Lion Wagner
 * @see UserRequest
 * @see IntervalGenerator
 * @see LIMBOGenerator
 */
public abstract class Generator extends RequestSender implements IRequestUpdateListener, ISelfScheduled {

    protected static final AccumulativeDataPointReporter allReporter = new AccumulativeDataPointReporter("GEN_ALL");
    protected final Model model;
    /**
     * Target Operation.
     */
    protected final Operation operation;
    /*
     * Reporters
     */
    protected final MultiDataPointReporter reporter;
    protected final AccumulativeDataPointReporter accReporter;
    private final GeneratorTriggerEvent trigger;
    /**
     * latest scheduled execution time before the currently scheduled.
     */
    private TimeInstant lastTargetTime;
    /**
     * Next scheduled execution in the future.
     */
    private TimeInstant nextTargetTime;

    /**
     * Superclass for all generators. Automatically takes care of the (re-)scheduling of the generation events, sending
     * of the {@code * UserRequest}s and observation of send requests.
     *
     * @param model       Default desmoj parameter
     * @param name        Default desmoj parameter
     * @param showInTrace Default desmoj parameter
     * @param operation   Target Operation Instance
     */
    public Generator(Model model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace);
        Objects.requireNonNull(operation);

        this.model = model;
        this.operation = operation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());

        String reportName = String
            .format("G[%s]_[%s(%s)]_", this.getClass().getSimpleName(), operation.getOwnerMS().getName(),
                operation.getName());
        reporter = new MultiDataPointReporter(reportName);
        accReporter = new AccumulativeDataPointReporter(reportName);

        trigger = new GeneratorTriggerEvent(model, null, false, this);

        addUpdateListener(this);
    }


    public TimeInstant getLastTargetTime() {
        return lastTargetTime;
    }

    public TimeInstant getNextTargetTime() {
        return nextTargetTime;
    }

    /**
     * Method to compute the next target time. Called by the superclass upon need for a new target TimeInstance.
     * Provides the TimeInstance of last scheduling.
     *
     * <p>
     * Can return {@code null} or throw a {@link GeneratorStopException} to stop the generator.
     *
     * @param lastTargetTime last target time
     * @return the next target time
     * @throws GeneratorStopException when the Generator is stops.
     */
    protected abstract TimeInstant getNextTargetTime(final TimeInstant lastTargetTime);

    /**
     * Method to compute the first or initial target time. Called by the superclass upon need for the first
     * TimeInstance. Should return a constant value.
     *
     * <p>
     * Can return {@code null} or throw a {@link GeneratorStopException} to stop the generator.
     *
     * @return the point in simulation time when the first request should be sent by this generator
     * @throws GeneratorStopException when the Generator is stops.
     */
    protected abstract TimeInstant getFirstTargetTime();

    /**
     * Helper to decide between First and Next TargetTime and to help with data collection.
     */
    private TimeInstant getNextExecutionTimeInstance() {
        nextTargetTime = lastTargetTime == null ? getFirstTargetTime() : getNextTargetTime(lastTargetTime);

        accReporter.addDatapoint("Load", nextTargetTime, 1);

        lastTargetTime = nextTargetTime;
        return nextTargetTime;
    }

    /**
     * This method is automatically called by the Generator itself. (by rescheduling via its {@code
     * GeneratorTriggerEvent})
     *
     * <p>
     * If absolutely needed it can be manually called to send the next request immediately.
     */
    public void eventRoutine() {
        UserRequest request = new UserRequest(model,
            String.format("User_Request@([%s] %s)", operation.getOwnerMS().getName(), operation.getName()), true,
            operation);

        try {
            sendRequest(String.format("User_Request@(%s) ",
                operation.getQuotedName()), request, operation.getOwnerMS().getNextAvailableInstance(),
                new TimeSpan(0));

            TimeInstant nextExecutionTimeInstance;
            try {
                nextExecutionTimeInstance = getNextExecutionTimeInstance();
            } catch (GeneratorStopException e) {
                sendTraceNote(
                    String.format("Generator %s has stopped.\nReason: %s", this.getQuotedName(), e.getMessage()));
                return;
            }


            if (nextExecutionTimeInstance == null) {
                sendWarning(
                    String.format("Did not schedule next trigger of %s",
                        this.getName()), this.getClass().getTypeName(),
                    "Next time to schedule the event was 'null'.",
                    "Check your request generators definition and input for errors.");
                return;
            }

            if (trigger.isScheduled()) {
                trigger.reSchedule(nextExecutionTimeInstance);
            } else {
                trigger.schedule(nextExecutionTimeInstance);
            }

        } catch (NoInstanceAvailableException e) {
            onRequestFailed(request, presentTime(), RequestFailedReason.NO_INSTANCE_AVAILABLE);
        }
    }


    /**
     * Does the initial scheduling of this generator.
     */
    @Override
    public void doInitialSelfSchedule() {
        try {
            TimeInstant next = getNextExecutionTimeInstance();
            if (next == null) {
                throw new GeneratorStopException();
            }

            if (trigger.isScheduled()) {
                trigger.reSchedule(next);
            } else {
                trigger.schedule(next);
            }

        } catch (GeneratorStopException e) {
            sendWarning(String.format("Generator %s did not start.", this.getName()),
                this.getClass().getCanonicalName(), e.getMessage(),
                "Check your request generators definition and input for errors.");
        }
    }

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     */
    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        sendTraceNote(String.format("Arrival of Request %s failed at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests so they can be directly compared
        accReporter.addDatapoint("SuccessfulRequests", currentTime, 0);

        allReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests so they can be directly compared
        allReporter.addDatapoint("SuccessfulRequests", currentTime, 0);

        return true;
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     */
    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        sendTraceNote(String.format("Successfully completed Request %s at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
        //also creates a datapoint for failed requests so they can be directly compared
        accReporter.addDatapoint("FailedRequests", currentTime, 0);


        allReporter.addDatapoint("FailedRequests", currentTime, 0);
        //also creates a datapoint for successful requests so they can be directly compared
        allReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
        return true;
    }


    /**
     * Event that represents the triggering of the generation of a new request.
     */
    //TODO: change this to a desmoj.core.simulator.SimProcess to reduce the model clogging up with objects
    private static class GeneratorTriggerEvent extends ExternalEvent {

        private final Generator generator;

        public GeneratorTriggerEvent(Model model, String name, boolean showInTrace, Generator generator) {
            super(model, name, showInTrace);
            this.generator = generator;
        }

        @Override
        public void eventRoutine() throws SuspendExecution {
            generator.eventRoutine();
        }
    }
}
