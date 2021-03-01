package de.rss.fachstudie.MiSim.entities.generator;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.networking.*;
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

    /**
     * Counter for how many Requests were send at the current nextTargetTime Used for Reporting.
     */
    private int currentTimestepSendCount = 0;

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
        this.schedule(new TimeInstant(0));
        reporter = new MultiDataPointReporter(name);
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

        if (nextTargetTime.equals(lastTargetTime)) {
            currentTimestepSendCount = currentTimestepSendCount + 1;
        } else {
            if (lastTargetTime != null) {
                reporter.addDatapoint("Load", lastTargetTime, currentTimestepSendCount);
            }
            currentTimestepSendCount = 1;
        }

        lastTargetTime = nextTargetTime;
        return nextTargetTime;
    }


    @Override
    public void eventRoutine() throws SuspendExecution {

        UserRequest request = new UserRequest(model, String.format("User_Request@([%s] %s)", operation.getOwner().getName(), operation.getName()), true, operation);

        NetworkRequestEvent event = new UserRequestArrivalEvent(model, String.format("User_Request@(%s) ", operation.getQuotedName()), this.traceIsOn(), this, request);
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

    }

    public TimeInstant getLastTargetTime() {
        return lastTargetTime;
    }

    public TimeInstant getNextTargetTime() {
        return nextTargetTime;
    }

    /**
     * Listener for the successful completion of the sending process. Provides a reference to the successfully arrived
     * request.
     *
     * @param request
     */
    @Override
    public void onRequestArrivalAtTarget(Request request) {
        //ignored
    }

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     *
     * @param request
     */
    @Override
    public void onRequestFailed(Request request) {
        sendTraceNote(String.format("Arrival of Request %s failed.", request));
    }
}
