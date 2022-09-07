package cambio.simulator.export;

import java.util.ArrayList;
import java.util.Collection;

import cambio.simulator.models.MiSimModel;

/**
 * @author Lion Wagner
 */
public final class MiSimReporters {
    private static final Collection<MiSimReporter> reporters = new ArrayList<>();
    public static MultiDataPointReporter RETRY_MANAGER_REPORTER;
    public static MultiDataPointReporter NETWORK_LATENCY_REPORTER;
    public static MultiDataPointReporter USER_REQUEST_REPORTER;
    public static AccumulativeDataPointReporter GENERATOR_REPORTER;

    public static void initializeStaticReporters(MiSimModel model) {

        RETRY_MANAGER_REPORTER = new MultiDataPointReporter("RM_", model);
        NETWORK_LATENCY_REPORTER = new MultiDataPointReporter("NL_", model);
        USER_REQUEST_REPORTER = new MultiDataPointReporter("R", model);
        GENERATOR_REPORTER = new AccumulativeDataPointReporter("GEN_ALL_", model);
    }

    public static void registerReporter(MiSimReporter reporter) {
        reporters.add(reporter);
    }

    public static void finalizeReports() {
        while (reporters.size() > 0) {
            reporters.iterator().next().finalizeReport();
        }
    }

    public static boolean deregister(MiSimReporter multiDataPointReporter) {
        return reporters.remove(multiDataPointReporter);
    }
}
