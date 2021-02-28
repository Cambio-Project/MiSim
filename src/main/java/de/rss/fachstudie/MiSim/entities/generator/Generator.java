package de.rss.fachstudie.MiSim.entities.generator;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.networking.NetworkRequestReceiveEvent;
import de.rss.fachstudie.MiSim.entities.networking.UserRequest;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class Generator extends ExternalEvent {

    protected final Model model;
    protected final Operation operation;

    protected final MultiDataPointReporter reporter;

    private int currentTimestepSendCount = 0;

    /**
     * last
     */
    private TimeInstant lastTargetTime;
    /**
     * next execution in the future
     */
    private TimeInstant nextTargetTime;

    public Generator(Model model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace);
        this.model = model;
        this.operation = operation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());
        this.schedule(new TimeInstant(0));
        reporter = new MultiDataPointReporter(name);
    }


    protected abstract TimeInstant getNextTargetTime(final TimeInstant lastTargetTime);

    protected abstract TimeInstant getFirstTargetTime();

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

        NetworkRequestReceiveEvent event = new NetworkRequestReceiveEvent(model, String.format("User_Request@(%s) ", operation.getQuotedName()), this.traceIsOn());
        request.stampSendoff(presentTime());
        event.schedule(request, presentTime());

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
}
