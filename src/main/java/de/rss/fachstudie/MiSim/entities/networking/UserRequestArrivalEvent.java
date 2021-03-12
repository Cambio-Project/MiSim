package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public final class UserRequestArrivalEvent extends NetworkRequestReceiveEvent {

    public UserRequestArrivalEvent(Model model, String name, boolean showInTrace, Request request, MicroserviceInstance receiver) {
        super(model, name, showInTrace, request, receiver);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        traveling_request.stampSendoff(presentTime());
        updateListener.onRequestSend(traveling_request, presentTime());
        super.eventRoutine();
    }
}
