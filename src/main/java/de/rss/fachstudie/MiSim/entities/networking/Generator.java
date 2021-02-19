package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class Generator extends ExternalEvent {

    protected final MainModel model;
    protected final Operation operation;
    /**
     * last
     */
    protected TimeInstant lastTargetTime;
    /**
     * next execution in the future
     */
    protected TimeInstant nextTargetTime;

    public Generator(MainModel model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace);
        this.model = model;
        this.operation = operation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());
        this.schedule(new TimeInstant(0));
    }


    protected abstract TimeInstant getNextTargetTime(final TimeInstant lastTargetTime);

    private TimeInstant getNextExecutionTimeInstance() {
        nextTargetTime = getNextTargetTime(lastTargetTime);
        lastTargetTime = nextTargetTime;
        return lastTargetTime;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

        UserRequest request = new UserRequest(model, "UserRequest_" + operation.getName(), true, operation);

        NetworkRequestReceiveEvent event = new NetworkRequestReceiveEvent(model, String.format("User_Request@(%s) ", operation.getQuotedName()), this.traceIsOn());
        request.stampSendoff(presentTime());
        event.schedule(request, presentTime());

        TimeInstant nextExecutionTimeInstance = getNextExecutionTimeInstance();

        if (this.isScheduled())
            this.reSchedule(nextExecutionTimeInstance);
        else this.schedule(nextExecutionTimeInstance);
    }


}
