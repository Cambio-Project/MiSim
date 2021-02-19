package de.rss.fachstudie.MiSim.entities;

import de.rss.fachstudie.MiSim.entities.networking.*;
import de.rss.fachstudie.MiSim.models.MainModel;
import de.rss.fachstudie.MiSim.resources.CPU;
import de.rss.fachstudie.MiSim.resources.Thread;

/**
 * @author Lion Wagner
 */
public class MicroserviceInstance extends MainModelAwareEntity {

    private final CPU cpu;
    private final Microservice owner;

    public MicroserviceInstance(MainModel model, String name, boolean showInTrace, Microservice microservice, int instanceID) {
        super(model, name, showInTrace);
        this.cpu = new CPU(model, String.format("%s_CPU", name), showInTrace, microservice.getId(), instanceID, microservice.getCapacity());
        this.owner = microservice;
    }

    public void handle(Request request) {
        if (request.isCompleted()) {
            NetworkRequestSendEvent sendEvent = new NetworkRequestSendEvent(getModel(), "Request_Answer_" + request.getQuotedName(), traceIsOn(), this);
            sendEvent.schedule(request);
        } else if (request.getDependencyRequests().isEmpty() || request.areDependencies_completed()) {
            ComputationCompletedEvent computationCompletedEvent = new ComputationCompletedEvent(model, String.format("ComputationEnd %s", request.getQuotedName()), traceIsOn());

            Thread thread = new Thread(model, String.format("%s_Thread", getName()), this.traceIsOn(), request.operation.getDemand(), computationCompletedEvent, owner, request, request.operation);
            cpu.addThread(thread, request.operation);

        } else {
            for (NetworkDependency dependency : request.getDependencyRequests()) {

                Request internalRequest = new InternalRequest(model, this.traceIsOn(), dependency, this);

                NetworkRequestSendEvent sendEvent = new NetworkRequestSendEvent(getModel(), String.format("Send Cascading_Request for %s", request.getQuotedName()), traceIsOn(), this);
                sendEvent.schedule(internalRequest, presentTime());
            }

        }
    }
}
