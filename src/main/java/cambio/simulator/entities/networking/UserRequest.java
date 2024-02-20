package cambio.simulator.entities.networking;

import static cambio.simulator.export.MiSimReporters.USER_REQUEST_REPORTER;
import static cambio.simulator.export.MiSimReporters.USER_REQUEST_AVG_REPORTER;

import cambio.simulator.entities.generator.LoadGeneratorDescriptionExecutor;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.simulator.Model;

/**
 * A {@code Request} that represents a request that is created by a user from outside the simulated system.
 *
 * <p>
 * These type of requests are generated by the {@code Generator}s and do not have a parent request. They are usually the
 * root request of a trace.
 *
 * @author Lion Wagner
 * @see LoadGeneratorDescriptionExecutor
 */
public class UserRequest extends Request {

    public UserRequest(Model model, String name, boolean showInTrace, Operation operation) {
        super(model, name, showInTrace, null, operation, null);
    }

    @Override
    protected void onReceive() {
        super.onReceive();
        USER_REQUEST_AVG_REPORTER.addDatapoint(
            String.format("[%s]_ResponseTimes_avg", operation.getName()), presentTime(),
            getResponseTime());
        USER_REQUEST_REPORTER
            .addDatapoint(String.format("[%s]_ResponseTimes", operation.getName()), presentTime(), getResponseTime());
        USER_REQUEST_REPORTER
            .addDatapoint("[All]ResponseTimes", presentTime(), getResponseTime());
    }
}
