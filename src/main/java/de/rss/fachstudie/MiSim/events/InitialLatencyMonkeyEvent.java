package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

public class InitialLatencyMonkeyEvent extends ExternalEvent {

    private MainModel model;
    private double time;
    private String microservice;
    private int msId = -1;
    private double delay;

    public InitialLatencyMonkeyEvent(Model owner, String name, boolean showInTrace, double time, int msId, double delay) {
        super(owner, name, showInTrace);

        this.model = (MainModel) owner;
        this.time = time;
        this.msId = msId;
        this.delay = delay;
        this.microservice = model.allMicroservices.get(msId).getName();
    }

    @Override
    public void eventRoutine() {
        if (msId == -1) {
            msId = model.getIdByName(microservice);
        }
        String monkeyName = this.getName().substring(0, getName().lastIndexOf("_Initializer"));
//        LatencyMonkeyEvent monkeyEvent = new LatencyMonkeyEvent(model, monkeyName, model.getShowMonkeyEvent(), msId, delay);
//        monkeyEvent.schedule(new TimeSpan(time, model.getTimeUnit()));
    }


    @Override
    public String toString() {
        return "LatencyMonkeyInitialEvent";
    }

    public double getTime() {
        return this.time;
    }

    public String getMicroservice() {
        return this.microservice;
    }

    public double getDelay() {
        return this.delay;
    }

}
