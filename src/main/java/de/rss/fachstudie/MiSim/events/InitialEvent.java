package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * An <code>InitialEvent</code> is an <code>ExternalEvent</code> which is the first event in the simulation
 * that is called in <code>doInitalSchedule</code> in the <code>Model</code>.
 */
public class InitialEvent extends ExternalEvent {
    private MainModel model;
    private double interval;
    private ContDistUniform timeToCreate;
    private String microservice = "";
    private String operation = "";
    private int msId = -1;

    /**
     * Triggers the first event.
     * Has to be called in doInitalSchedules.
     *
     * @param owner Model: The model that owns this event
     * @param name String: The name of this event
     * @param showInTrace boolean: Whether this event is shown in the trace or not
     * @param interval double: The timepoint at which the first event will be called
     * @param msId int: The ID of the microservice for which this event will create a request
     * @param op: String: The name of the operation that this event will schedule
     */
    public InitialEvent(Model owner, String name, boolean showInTrace, double interval, int msId, String op) {
        super(owner, name, showInTrace);

        model = (MainModel) owner;
        timeToCreate = new ContDistUniform(model, name, interval, interval, model.getShowInitEvent(), true);
        this.msId = msId;
        this.microservice = model.allMicroservices.get(msId).getName();
        this.operation = op;
    }

    public double getInterval() {
        return this.interval;
    }

    public String getMicroservice() {
        return this.microservice;
    }

    public int getId() {
        return this.msId;
    }

    public String getOperation() {
        return operation;
    }

    /**
     * The <code>eventRoutine</code> of the <code>InitialEvent</code>.
     * Schedules a start event to simulate an operation of a specified microservice
     *
     * @throws SuspendExecution
     */
    @Override
    public void eventRoutine() throws SuspendExecution {

        // Create a message object and begin event
        MessageObject initialMessageObject = new MessageObject(model, this.getClass().getName(), model.getShowStartEvent());
        StartEvent startEvent = new StartEvent(model,
                "Start Event: " + microservice + "(" + operation + ")",
                model.getShowStartEvent(), msId, operation);

        startEvent.schedule(initialMessageObject, new TimeSpan(0, model.getTimeUnit()));

        schedule(new TimeSpan(timeToCreate.sample(), model.getTimeUnit()));
    }
}
