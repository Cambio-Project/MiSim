package de.rss.fachstudie.MiSim.entities.generator;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.export.AccumulativeDatPointReporter;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class Generator extends ExternalEvent implements IRequestUpdateListener {

    protected final Model model;

    /**
     * Target Operation
     */
    protected final Operation operation;

    /**
     * latest scheduled execution time before the currently scheduled
     */
    private TimeInstant lastTargetTime;

    /**
     * next scheduled execution in the future
     */
    private TimeInstant nextTargetTime;

    protected final MultiDataPointReporter reporter;
    protected final AccumulativeDatPointReporter accReporter;

    /**
     * Superclass for all generators. Automatically takes care of output reporting and pulling/scheduling and basic
     * monitoring or Requests.
     *
     * @param model       Default desmoj parameter
     * @param name        Default desmoj parameter
     * @param showInTrace Default desmoj parameter
     * @param operation   Target Operation Instance
     */
    public Generator(Model model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace);
        this.model = model;
        this.operation = operation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());

        String reportName = String.format("G[%s]_[%s(%s)]_", this.getClass().getSimpleName(), operation.getOwner().getName(), operation.getName());
        reporter = new MultiDataPointReporter(reportName);
        accReporter = new AccumulativeDatPointReporter(reportName);
        schedule();

    }

    private void doInitialSchedule() {
        try {
            TimeInstant next = getNextExecutionTimeInstance();
            if (next == null) throw new GeneratorStopException();

            if (this.isScheduled()) this.reSchedule(next);
            else this.schedule(next);

        } catch (GeneratorStopException e) {
            sendWarning("Generator %s did not start.", this.getClass().getCanonicalName(), "Load profile was faulty.",
                    "Check your request generators definition and input for errors.");
        }
    }


    /**
     * Method to compute the next target time. Called by the superclass upon need for a new target TimeInstance.
     * Provides the TimeInstance of last scheduling.
     * <p>
     * Can return {@code null} or throw a {@link GeneratorStopException} to stop the generator.
     *
     * @throws GeneratorStopException when the Generator is stops.
     */
    protected abstract TimeInstant getNextTargetTime(final TimeInstant lastTargetTime);

    /**
     * Method to compute used get the first target time. Called by the superclass upon need for the first TimeInstance.
     * Should return a constant value.
     * <p>
     * Can return {@code null} or throw a {@link GeneratorStopException} to stop the generator.
     *
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
     * This method is automatically called by the Generator itself. (by rescheduling itself)
     * <p>
     * If absolutely needed it can be manually called to send the schedule the next Request immediately.
     *
     * @throws SuspendExecution
     */
    @Override
    public void eventRoutine() throws SuspendExecution {
        if (lastTargetTime == null) {
            doInitialSchedule();
            return;
        }

        UserRequest request = new UserRequest(model, String.format("User_Request@([%s] %s)", operation.getOwner().getName(), operation.getName()), true, operation);
        request.addUpdateListener(this);

        try {
            NetworkRequestEvent event = new UserRequestArrivalEvent(model,
                    String.format("User_Request@(%s) ",
                            operation.getQuotedName()),
                    this.traceIsOn(),
                    request,
                    operation.getOwner().getNextAvailableInstance());


            event.schedule(presentTime());

            TimeInstant nextExecutionTimeInstance;
            try {
                nextExecutionTimeInstance = getNextExecutionTimeInstance();
            } catch (GeneratorStopException e) {
                sendTraceNote(String.format("Generator %s has stopped.\nReason: %s", this.getQuotedName(), e.getMessage()));
                return;
            }


            if (nextExecutionTimeInstance == null) {
                sendWarning(
                        String.format("Did not schedule event %s",
                                event.getQuotedName()), this.getClass().getTypeName(),
                        "Next time to schedule the event was 'null'.",
                        "Check your request generators definition and input for errors.");
                return;
            }

            if (this.isScheduled())
                this.reSchedule(nextExecutionTimeInstance);
            else this.schedule(nextExecutionTimeInstance);

        } catch (NoInstanceAvailableException e) {
            onRequestFailed(request, presentTime(), RequestFailedReason.NO_INSTANCE_AVAILABLE);

        }
    }

    public TimeInstant getLastTargetTime() {
        return lastTargetTime;
    }

    public TimeInstant getNextTargetTime() {
        return nextTargetTime;
    }

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     */
    @Override
    public void onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        sendTraceNote(String.format("Arrival of Request %s failed at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests so they can be directly compared
        accReporter.addDatapoint("SuccessfulRequests", currentTime, 0);
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     */
    @Override
    public void onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        sendTraceNote(String.format("Successfully completed Request %s at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
        //also creates a datapoint for failed requests so they can be directly compared
        accReporter.addDatapoint("FailedRequests", currentTime, 0);
    }
}
