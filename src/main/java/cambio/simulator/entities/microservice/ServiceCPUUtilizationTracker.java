package cambio.simulator.entities.microservice;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.TimeSpan;

/**
 * Simulation "background" process that periodically reports the relative CPU utilization of the owning microservice.
 *
 * @author Lion Wagner
 */
public class ServiceCPUUtilizationTracker extends NamedSimProcess {

    public static TimeSpan probeInterval = new TimeSpan(0.1);

    private final MultiDataPointReporter reporter;
    private final Microservice owner;


    /**
     * Creates a new CPU Utilization Tracker that reports the utilization of the owning CPU periodically.
     */
    public ServiceCPUUtilizationTracker(Microservice owner) {
        this(owner, null);
    }

    /**
     * Creates a new CPU Utilization Tracker that reports the utilization of the owning CPU periodically.
     * Uses the given reporter to collect results.
     */
    public ServiceCPUUtilizationTracker(Microservice owner, MultiDataPointReporter reporter) {
        super(owner.getModel(), "CPU Utilization Tracker of " + owner.getPlainName(), true, owner.traceIsOn());

        this.owner = owner;
        this.reporter = reporter != null ? reporter :
            new MultiDataPointReporter("S[" + owner.getPlainName() + "]_", owner.getModel());

        this.setSchedulingPriority(Priority.LOW);
        this.activate();
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        reporter.addDatapoint("CPUUtilization", presentTime(), owner.getAverageRelativeUtilization());
        this.hold(probeInterval);
    }
}
