package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

import java.util.concurrent.TimeUnit;


public class NetworkRequestTimeoutEvent extends NetworkRequestEvent implements IRequestUpdateListener {
    private boolean canceled = false;

    public NetworkRequestTimeoutEvent(Model model, String name, boolean showInTrace, Request request) {
        super(model, name, showInTrace, request);
        this.setSchedulingPriority(Priority.LOW);
        this.schedule(new TimeSpan(Integer.MAX_VALUE-1, TimeUnit.SECONDS));
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        if(canceled) return;
        NetworkRequestEvent cancelEvent = new NetworkRequestCanceledEvent(getModel(), "RequestCancel", getModel().traceIsOn(), traveling_request, RequestFailedReason.TIMEOUT, "Request " + traveling_request.getName() + " was canceled due to a timeout.");
        cancelEvent.schedule(new TimeSpan(0));
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
//        if (this.isScheduled())
//            this.cancel();
        canceled = true;
        return false;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
//        if (this.isScheduled())
//            this.cancel();
        canceled = true;
        return false;
    }

    @Override
    public int getListeningPriority() {
        return Priority.NORMAL + 1;
    }
}
