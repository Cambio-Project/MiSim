package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.CPU;
import desmoj.core.simulator.ExternalEvent;

/**
 * @author Lion Wagner
 */
public class LatencyMonkeyEvent extends ExternalEvent {

    private MainModel model;
    private String microservice;
    private int msId = -1;
    private double delay;


    public LatencyMonkeyEvent(MainModel model, String monkeyName, boolean showInTrace, int msId, double delay) {
        super(model, monkeyName, showInTrace);

        this.model = (MainModel) model;
        this.msId = msId;
        this.delay = delay;
        this.microservice = model.allMicroservices.get(msId).getName();
    }


    @Override
    public void eventRoutine() {
        for (Microservice msEntity : model.services.get(msId)) {
            for (CPU cpu : model.serviceCPU.get(msEntity.getId()).values()) {
                cpu.applyDelay(delay);
            }
        }

    }

}
