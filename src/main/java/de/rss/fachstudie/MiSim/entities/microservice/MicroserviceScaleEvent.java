package de.rss.fachstudie.MiSim.entities.microservice;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * For now this is an unused event to represent the scaling of a microservice.
 *
 * @author Lion Wagner
 * TODO: consider using this event
 */
public class MicroserviceScaleEvent extends ExternalEvent {

    private final Microservice microservice;
    private final int targetInstanceCount;

    public MicroserviceScaleEvent(Model model, String s, boolean b, Microservice microservice, int targetInstanceCount) {
        super(model, s, b);
        this.microservice = microservice;
        this.targetInstanceCount = targetInstanceCount;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        microservice.scaleToInstancesCount(targetInstanceCount);
    }

}
