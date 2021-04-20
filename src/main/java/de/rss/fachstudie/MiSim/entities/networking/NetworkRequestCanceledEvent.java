package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Event that should be scheduled when a request gets canceled.
 *
 * @author Lion Wagner
 */
public class NetworkRequestCanceledEvent extends NetworkRequestEvent {

    private final RequestFailedReason reason;
    private final String details;

    private static final List<NetworkDependency> canceledDependencies = new ArrayList<>();

    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Request request, RequestFailedReason reason) {
        this(model, name, showInTrace, request, reason, null);
    }

    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Request request, RequestFailedReason reason, String details) {
        super(model, name, showInTrace, request);
        this.reason = reason;
        this.details = details;
        setSchedulingPriority(Priority.VERY_HIGH);

//        if(request instanceof InternalRequest){
//            if(canceledDependencies.contains(((InternalRequest) request).getDependency())){
//                System.out.print("ok");
//            }
//            canceledDependencies.add(((InternalRequest) request).getDependency());
//        }
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        sendTraceNote(String.format("Request %s was not handled. Cause: %s", traveling_request.getQuotedName(), reason));
        if (details != null) {
            sendTraceNote(String.format("Details: %s", details));
        }
        updateListener.onRequestFailed(traveling_request, presentTime(), reason);
    }
}
