package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class UserRequest extends Request {

    private static final MultiDataPointReporter reporter = new MultiDataPointReporter("R");

    public UserRequest(Model model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace, null, operation);
    }


    @Override
    protected void onReceive() {
        super.onReceive();
        reporter.addDatapoint(String.format("[%s]_ResponseTimes", operation.getName()), presentTime(), getResponseTime());
    }
}
