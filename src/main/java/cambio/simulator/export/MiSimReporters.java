package cambio.simulator.export;

import java.util.ArrayList;
import java.util.Collection;

import cambio.simulator.models.MiSimModel;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * Class that manges all {@link MiSimReporter}s. In this case managing means it takes care of creating the global/static
 * reporters that are shared between enities. Further it also takes care of finalizing all reporters at the end of the
 * simulation.
 *
 * <p>
 * Currently it is designed as a static class (not quite a singleton but close). {@link MiSimReporter}s register
 * themselves automatically on creation.
 *
 * <p>
 * Reporters can also be deregistered in case they should not be terminated at the end of the simulation. However this
 * will keep the output file locked until you call {@link MiSimReporter#finalizeReport()}
 *
 * @author Lion Wagner
 * @see MiSimReporter
 */
public final class MiSimReporters {

    public static final String DEFAULT_TIME_COLUMN_NAME = "SimulationTime";
    public static final String DEFAULT_VALUE_COLUMN_NAME = "Value";
    private static final Collection<MiSimReporter<?>> reporters = new ArrayList<>();
    public static MultiDataPointReporter RETRY_MANAGER_REPORTER;
    public static MultiDataPointReporter NETWORK_LATENCY_REPORTER;
    public static MultiDataPointReporter USER_REQUEST_REPORTER;
    public static AccumulativeDataPointReporter GENERATOR_REPORTER;

    public static final String csvSeperator = ";";


    /**
     * Initializes the static reporters that are shared between entities.
     *
     * @param model Model that provides the report location in its metadata.
     */
    public static void initializeStaticReporters(MiSimModel model) {
        RETRY_MANAGER_REPORTER = new MultiDataPointReporter("RM_", model);
        NETWORK_LATENCY_REPORTER = new MultiDataPointReporter("NL_", model);
        USER_REQUEST_REPORTER = new MultiDataPointReporter("R", model);
        GENERATOR_REPORTER = new AccumulativeDataPointReporter("GEN_ALL_", model);
    }

    /**
     * Registers a new reporter to be finalized at the end of the simulation. This is called automatically on the
     * creation of a new {@link MiSimReporter}.
     */
    public static void registerReporter(MiSimReporter<?> reporter) {
        reporters.add(reporter);
    }


    /**
     * Finalizes all registered reporters. This includes deregistering them via {@link MiSimReporter#finalizeReport()}
     */
    public static void finalizeReports() {
        while (reporters.size() > 0) {
            reporters.iterator().next().finalizeReport();
        }
    }

    /**
     * Deregisters a reporter. This will prevent the reporter from being finalized automatically at the end of the
     * simulation.
     *
     * @return true if the reporter was registered and could be deregistered.
     * @see MiSimReporter#finalizeReport()
     */
    public static boolean deregister(MiSimReporter<?> reporter) {
        return reporters.remove(reporter);
    }

    /**
     * Gets an immutable collection of all registered reporters.#
     */
    public static ImmutableCollection<MiSimReporter<?>> getReporters() {
        return ImmutableList.copyOf(reporters);
    }
}
