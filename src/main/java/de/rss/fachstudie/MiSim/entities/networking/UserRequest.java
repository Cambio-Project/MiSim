package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.models.MainModel;

/**
 * @author Lion Wagner
 */
public class UserRequest extends Request{

    public UserRequest(MainModel model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace, null, operation);
    }

    @Override
    protected void onDependenciesComplete() {
        super.onDependenciesComplete();
        sendTraceNote(String.format("Dependencies Completed: %s", getQuotedName()));
    }

    @Override
    protected void onComputationComplete() {
        super.onComputationComplete();
        sendTraceNote(String.format("Computation Completed: %s", getQuotedName()));
    }

    @Override
    protected void onCompletion() {
        super.onCompletion();
        sendTraceNote(String.format("Completed %s!", getQuotedName()));
    }
}
