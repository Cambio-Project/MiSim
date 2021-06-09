package de.unistuttgart.sqa.orcas.misim.resources.cpu;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.unistuttgart.sqa.orcas.misim.entities.microservice.MicroserviceInstance;
import de.unistuttgart.sqa.orcas.misim.export.MultiDataPointReporter;
import de.unistuttgart.sqa.orcas.misim.resources.cpu.scheduling.CPUProcessScheduler;
import de.unistuttgart.sqa.orcas.misim.resources.cpu.scheduling.RoundRobinScheduler;
import desmoj.core.simulator.ExternalEvent;
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
public class CPU extends ExternalEvent {

    private static final int DEFAULT_THREADPOOLSIZE = 1000;

    private final MultiDataPointReporter reporter;


    private final MicroserviceInstance owner;
    private final CPUProcessScheduler scheduler;
    private final double capacityPerThread; //computation capacity of one thread in one (1.0) simulation time unit
    private final int threadPoolSize; //counts the current size of the thread pool, just in case its atomic
    private final Set<CPUProcess> activeProcesses;

    /**
     * Constructs a new CPU with a default Round-Robin scheduler and a default thread pool size of {@code
     * CPUImpl.DEFAULT_THREADPOOLSIZE}.
     *
     * @see CPU#CPU
     * @see RoundRobinScheduler
     */
    public CPU(Model model, String name, boolean showInTrace, int capacity, MicroserviceInstance owner) {
        this(model, name, showInTrace, capacity, DEFAULT_THREADPOOLSIZE, owner);
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
        this(model, name, showInTrace, owner, capacity, scheduler, DEFAULT_THREADPOOLSIZE);
    }

    /**
     * Constructs a new CPU resource instance.
     *
     * @param model          parent and simulation model
     * @param name           CPU name, should be formatted {}_{}
     * @param showInTrace    whether the computation events should be shown in the trace
     * @param owner          instance that owns this cpu
     * @param capacity       total capacity of the cpu resource. Each thread will be assigned a capacity of {@code
     *                       Math.floor(capacity/threadPoolSize)}.
     * @param scheduler      implementation of a scheduling strategy that should be used by the CPU
     * @param threadPoolSize thread count of the CPU
     *
     * @see CPUProcessScheduler
     * @see RoundRobinScheduler
     */
    public CPU(Model model, String name, boolean showInTrace, MicroserviceInstance owner, int capacity,
               CPUProcessScheduler scheduler, int threadPoolSize) {
        super(model, name, showInTrace);
        this.owner = owner;
        this.scheduler = scheduler;

        if (this.owner.getName().contains("gateway")) {
            threadPoolSize = 1;
        }
        this.capacityPerThread = (double) capacity / threadPoolSize;
        this.threadPoolSize = threadPoolSize;
        activeProcesses = new HashSet<>(threadPoolSize);

        String[] names = name.split("_");
        reporter = new MultiDataPointReporter(String.format("C%s_[%s]_", names[0], names[1]));
    }

    /**
     * Schedules the given process to be executed.
     *
     * <p>
     * Depending on the choice of scheduler (fair/unfair) the execution is not guaranteed.
     *
     * @param process {@code CPUProcess} object to be submitted for scheduling
     *
     * @see CPUProcess
     * @see CPUProcessScheduler
     */
    public void submitProcess(CPUProcess process) {
        scheduler.enterProcess(process);
        if (hasProcessAndThreadReady()) {
            forceScheduleNow();
        }
        // reporter.addDatapoint("TotalProcesses", presentTime(), getProcessesCount());
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
                String.format("Computation burst finished of %s",
                    nextProcess.getRequest().getQuotedName()),
                debugIsOn(),
                nextProcess,
                this,
                nextTotalDemand);
            endEvent.schedule(processBurstDuration);
            activeProcesses.add(nextProcess);
        }

        reporter.addDatapoint("ActiveProcesses", presentTime(), activeProcesses.size());
        reporter.addDatapoint("Usage", presentTime(), activeProcesses.size() / (double) threadPoolSize);
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
            sendTraceNote(String.format("Burst for process of %s completed, but has %d demand remaining",
                process.getRequest().getName(), process.getDemandRemainder()));
            scheduler.enterProcess(process);
        }

        activeProcesses.remove(process); //the process whose burst finished is not active anymore

        //since at least one thread should be free now, a reschedule happens
        forceScheduleNow();

        reporter.addDatapoint("ActiveProcesses", presentTime(), activeProcesses.size());
        reporter.addDatapoint("TotalProcesses", presentTime(), getProcessesCount());
        reporter.addDatapoint("Usage", presentTime(), (double) activeProcesses.size() / threadPoolSize);

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
        return workTotal / (threadPoolSize * capacityPerThread);
    }

    public double getCurrentUsage() {
        return (double) activeProcesses.size() / threadPoolSize;
    }

    /**
     * Forcibly stops all currently running and scheduled processes.
     */
    public synchronized void clear() {
        activeProcesses.forEach(CPUProcess::cancel);
        scheduler.clear();
    }


}

