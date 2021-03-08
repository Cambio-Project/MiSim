package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class InitialChaosMonkeyEvent extends ExternalEvent {
    private MainModel model;
    private double time;
    private String microservice;
    private int msId = -1;
    private int instances;

    public InitialChaosMonkeyEvent(Model owner, String name, boolean showInTrace, double time, int msId, int instances) {
        super(owner, name, showInTrace);

        this.model = (MainModel) owner;
        this.time = time;
        this.msId = msId;
        this.instances = instances;
        this.microservice = model.allMicroservices.get(msId).getName();
    }

    @Override
    public String toString() {
        return "ChaosMonkeyInitialEvent";
    }

    public double getTime() {
        return this.time;
    }

    public String getMicroservice() {
        return this.microservice;
    }

    public int getInstances() {
        return this.instances;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        if (msId == -1) {
            msId = model.getIdByName(microservice);
        }
        String monkeyName = this.getName().substring(0, getName().lastIndexOf("_Initializer"));
        ChaosMonkeyEvent monkeyEvent = new ChaosMonkeyEvent(model, monkeyName, model.getShowMonkeyEvent(), null, instances);
        monkeyEvent.schedule(new TimeSpan(time, model.getTimeUnit()));
    }
}
