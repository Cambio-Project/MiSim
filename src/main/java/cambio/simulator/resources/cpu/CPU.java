package cambio.simulator.resources.cpu;

import java.util.*;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.resources.cpu.scheduling.CPUProcessScheduler;
import cambio.simulator.resources.cpu.scheduling.RoundRobinScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Pair;


/**
 * Represents a CPU resource.
 *
 * <p>
 * The CPU implementation supports multithreading with a thread pool size and multiple scheduling strategies.
 *
 * @author Lion Wagner
 * @see CPUProcess
 * @see CPUProcessScheduler
 * @see CPU#CPU(Model, String, boolean, MicroserviceInstance, int, CPUProcessScheduler, int)
 */
public class CPU extends NamedExternalEvent {

    private static final int DEFAULT_THREADPOOL_SIZE = 4;
    private static final String QUEUE_STATE_DATASET_NAME = "QueueState";

    private final MultiDataPointReporter reporter;

    private final BinnedCPUUtilizationTracker binnedUtilizationTracker;

    private final MicroserviceInstance owner;
    private final CPUProcessScheduler scheduler;
    private final double capacityPerThread; //computation capacity of one thread in one (1.0) simulation time unit
    private final int threadPoolSize; //counts the current size of the thread pool, just in case its atomic
    private final Set<CPUProcess> activeProcesses;

    /**
     * Constructs a new CPU with a default Round-Robin scheduler and a default thread pool size of
     * {@code CPUImpl.DEFAULT_THREADPOOLSIZE}.
     *
     * @see CPU#CPU
     * @see RoundRobinScheduler
     */
    public CPU(Model model, String name, boolean showInTrace, int capacity, MicroserviceInstance owner) {
        this(model, name, showInTrace, capacity, DEFAULT_THREADPOOL_SIZE, owner);
    }

    /**
     * Constructs a new CPU with a default Round-Robin scheduler.
     *
     * @see CPU#CPU
     * @see RoundRobinScheduler
     */
    public CPU(Model model, String name, boolean showInTrace, int capacity, int threadPoolSize,
               MicroserviceInstance owner) {
        this(model, name, showInTrace, owner, capacity, new RoundRobinScheduler(name + "_scheduler"), threadPoolSize);
    }

    /**
     * Constructs a new CPU with a default thread pool size of {@code CPUImpl.DEFAULT_THREADPOOLSIZE}.
     *
     * <p>
     * {@inheritDoc}
     *
     * @see CPU#CPU
     */
    public CPU(Model model, String name, boolean showInTrace, int capacity, CPUProcessScheduler scheduler,
               MicroserviceInstance owner) {
        this(model, name, showInTrace, owner, capacity, scheduler, DEFAULT_THREADPOOL_SIZE);
    }

    /**
     * Constructs a new CPU resource instance.
     *
     * @param model          parent and simulation model
     * @param name           CPU name
     * @param showInTrace    whether the computation events should be shown in the trace
     * @param owner          instance that owns this cpu
     * @param capacity       total capacity of the cpu resource. Each thread will be assigned a capacity of
     *                       {@code Math.floor(capacity/threadPoolSize)}.
     * @param scheduler      implementation of a scheduling strategy that should be used by the CPU
     * @param threadPoolSize thread count of the CPU
     * @see CPUProcessScheduler
     * @see RoundRobinScheduler
     */
    public CPU(Model model, String name, boolean showInTrace, MicroserviceInstance owner, int capacity,
               CPUProcessScheduler scheduler, int threadPoolSize) {
        super(model, name, showInTrace);
        this.owner = owner;
        this.scheduler = scheduler;

        this.capacityPerThread = (double) capacity / threadPoolSize;
        this.threadPoolSize = threadPoolSize;
        activeProcesses = new HashSet<>(threadPoolSize);

        reporter = new MultiDataPointReporter(String.format("C[%s]_", name), model);
        binnedUtilizationTracker = new BinnedCPUUtilizationTracker(this);

        reporter.registerDefaultHeader(QUEUE_STATE_DATASET_NAME, "TotalProcesses", "ActiveProcesses");
        reporter.addDatapoint(QUEUE_STATE_DATASET_NAME, presentTime(), 0, 0);
        reportUtilization();
    }

    /**
     * Schedules the given process to be executed.
     *
     * <p>
     * Depending on the choice of scheduler (fair/unfair) the execution is not guaranteed.
     *
     * @param process {@code CPUProcess} object to be submitted for scheduling
     * @see CPUProcess
     * @see CPUProcessScheduler
     */
    public void submitProcess(CPUProcess process) {
        scheduler.enterProcess(process);
        if (hasProcessAndThreadReady()) {
            forceScheduleNow();
        }
        reportQueueState();
    }

    /**
     * Generic routine of the CPU implementation.
     *
     * <p>
     * As long as processes can be scheduled, the cpu retrieves the next process and target burst duration from the
     * scheduler. Based on the retrieved data a {@link ComputationBurstCompletedEvent} is scheduled to execute after the
     * target burst duration.
     * </p>
     *
     * <p>
     * {@link ComputationBurstCompletedEvent}s will notify this object about this object about their execution via the
     * {@code CPUImpl#onBurstFinished} method.
     *
     * @see ComputationBurstCompletedEvent
     */
    @Override
    public void eventRoutine() throws SuspendExecution {
        while (hasProcessAndThreadReady()) {
            Pair<CPUProcess, Integer> next = scheduler.retrieveNextProcessNoReschedule();
            CPUProcess nextProcess = next.getValue0();
            int nextTotalDemand = next.getValue1();

            nextProcess.stampCurrentBurstStarted(presentTime());
            TimeSpan processBurstDuration = new TimeSpan(nextTotalDemand / capacityPerThread);

            ComputationBurstCompletedEvent endEvent = new ComputationBurstCompletedEvent(getModel(),
                "Computation burst finished of " + nextProcess.getRequest().getQuotedPlainName(),
                debugIsOn(),
                nextProcess,
                this,
                nextTotalDemand);
            endEvent.schedule(processBurstDuration);
            activeProcesses.add(nextProcess);
        }


        binnedUtilizationTracker.updateUtilization(getCurrentUsage(), presentTime());


        reportQueueState();
        reportUtilization();
    }

    private boolean hasProcessAndThreadReady() {
        return scheduler.hasProcessesToSchedule() && this.hasThreadsAvailable();
    }

    private double getPerThreadCapacity() {
        return capacityPerThread;
    }

    private boolean hasThreadsAvailable() {
        return activeProcesses.size() < threadPoolSize;
    }


    void onBurstFinished(CPUProcess process) {
        Objects.requireNonNull(process);

        if (process.getDemandRemainder() > 0) { //if process is not finished reschedule it
            if (traceIsOn()) {
                sendTraceNote(String.format("Burst for process of %s completed, but has %d demand remaining",
                    process.getRequest().getName(), process.getDemandRemainder()));
            }
            scheduler.enterProcess(process);
        }

        activeProcesses.remove(process); //the process whose burst finished is not active anymore

        //since at least one thread should be free now, a rescheduling happens
        forceScheduleNow();

        binnedUtilizationTracker.updateUtilization(getCurrentUsage(), presentTime());

        reportQueueState();
        reportUtilization();
    }

    /**
     * Reschedules the {@code eventRoutine} immediately.
     *
     * @see CPU#eventRoutine()
     */
    private void forceScheduleNow() {
        if (this.isScheduled()) {
            this.reSchedule(presentTime());
        } else {
            this.schedule(presentTime());
        }
    }


    private int getProcessesCount() {
        return scheduler.size() + activeProcesses.size();
    }


    /**
     * Forcibly stops all currently running and scheduled processes.
     */
    public synchronized void clear() {
        activeProcesses.forEach(CPUProcess::cancel);
        scheduler.clear();

        reportQueueState();
        reportUtilization();
    }

    private void reportQueueState() {
        reporter.addDatapoint(QUEUE_STATE_DATASET_NAME, presentTime(), getProcessesCount(), activeProcesses.size());
    }


    private void reportUtilization() {
        reporter.addDatapoint("Utilization", presentTime(), getCurrentUsage());
        reporter.addDatapoint("RelativeUtilization", presentTime(), getCurrentRelativeWorkDemand());
    }

    /** rounding factor to get rid of minuscule imprecision during the relative utilization calculation. */
    private static final double roundingFactor = Math.pow(10, 14);

    /**
     * Calculates the relative remaining workload demand of this CPU.
     *
     * <p>
     * This demand is calculated by (queuedDemand + activeDemand)/(totalCapacity)
     * </p>
     *
     * @return the remaining amount of cycles to complete the current workload
     */
    public double getCurrentRelativeWorkDemand() {
        int totalQueuedWorkRemainder = scheduler.getTotalWorkDemand();
        double activeWorkRemainder =
            activeProcesses.stream().mapToDouble(value -> value.getDemandRemainder(presentTime(), capacityPerThread))
                .sum();
        double workTotal = totalQueuedWorkRemainder + activeWorkRemainder;
        double workPercentage = workTotal / (threadPoolSize * capacityPerThread);
        workPercentage = Math.round(workPercentage * roundingFactor) / roundingFactor;

        reporter.addDatapoint("RelativeUtilization", presentTime(), workPercentage);
        return workPercentage;
    }

    public double getCurrentUsage() {
        return (double) activeProcesses.size() / threadPoolSize;
    }
}

