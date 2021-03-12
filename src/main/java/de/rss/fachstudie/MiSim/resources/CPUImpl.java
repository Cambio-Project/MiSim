package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.Set;

public class CPUImpl extends ExternalEvent {

    /**
     * Represents a CPU owned by a Microservice Instant
     */
    public static final class OwnedCPU extends CPUImpl {
        private final MicroserviceInstance owner;

        public OwnedCPU(Model model, String name, boolean showInTrace, int capacity, MicroserviceInstance owner) {
            super(model, name, showInTrace, capacity);
            this.owner = owner;
        }

        public OwnedCPU(Model model, String name, boolean showInTrace, int capacity, int threadPoolSize, MicroserviceInstance owner) {
            super(model, name, showInTrace, capacity, threadPoolSize);
            this.owner = owner;
        }

        public OwnedCPU(Model model, String name, boolean showInTrace, int capacity, CPUProcessScheduler scheduler, MicroserviceInstance owner) {
            super(model, name, showInTrace, capacity, scheduler);
            this.owner = owner;
        }

        public OwnedCPU(Model model, String name, boolean showInTrace, int capacity, CPUProcessScheduler scheduler, int threadPoolSize, MicroserviceInstance owner) {
            super(model, name, showInTrace, capacity, scheduler, threadPoolSize);
            this.owner = owner;
        }
    }

    private static final int DEFAULT_THREADPOOLSIZE = 4;

    private final MultiDataPointReporter reporter;
    private final CPUProcessScheduler scheduler;
    private final int capacity_per_thread; //computation capacity of one thread in one (1.0) simulation time unit

    private final int threadPoolSize; //counts the current size of the thread pool, just in case its atomic
    private final Set<CPUProcess> activeProcesses;


    public CPUImpl(Model model, String name, boolean showInTrace, int capacity) {
        this(model, name, showInTrace, capacity, DEFAULT_THREADPOOLSIZE);
    }

    public CPUImpl(Model model, String name, boolean showInTrace, int capacity, int threadPoolSize) {
        this(model, name, showInTrace, capacity, new RoundRobinScheduler(name + "_scheduler"), threadPoolSize);
    }

    public CPUImpl(Model model, String name, boolean showInTrace, int capacity, CPUProcessScheduler scheduler) {
        this(model, name, showInTrace, capacity, scheduler, DEFAULT_THREADPOOLSIZE);
    }

    public CPUImpl(Model model, String name, boolean showInTrace, int capacity, CPUProcessScheduler scheduler, final int threadPoolSize) {
        super(model, name, showInTrace);
        String[] names = name.split("_");
        this.scheduler = scheduler;
        this.capacity_per_thread = capacity;
        this.threadPoolSize = threadPoolSize;
        activeProcesses = new HashSet<>(threadPoolSize);
        reporter = new MultiDataPointReporter(String.format("C%s_[%s]_", names[0], names[1]));
    }

    public void submitProcess(CPUProcess process) {
        scheduler.enterProcess(process);
        if (hasProcessAndThreadReady()) {
            forceScheduleNow();
        }
        reporter.addDatapoint("TotalProcesses", presentTime(), getProcessesCount());
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        while (hasProcessAndThreadReady()) {
            Pair<CPUProcess, Integer> next = scheduler.retrieveNextProcessNoReschedule();
            CPUProcess nextProcess = next.getValue0();
            int nextTotalDemand = next.getValue1();

            nextProcess.stampCurrentBurstStarted(presentTime());
            TimeSpan processBurstDuration = new TimeSpan(nextTotalDemand / (double) capacity_per_thread);

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
        return scheduler.hasThreadsToSchedule() && this.hasThreadsAvailable();
    }

    private int getPerThreadCapacity() {
        return capacity_per_thread;
    }

    private boolean hasThreadsAvailable() {
        return activeProcesses.size() < threadPoolSize;
    }


    void onBurstFinished(CPUProcess process) {
        if (process.getDemandRemainder() > 0) {
            scheduler.enterProcess(process); //if process is not finished reschedule it
        }

        activeProcesses.remove(process); //the process whose burst finished is not active anymore

        //since at least one thread should be free now, a reschedule happens
        forceScheduleNow();

        reporter.addDatapoint("ActiveProcesses", presentTime(), activeProcesses.size());
        reporter.addDatapoint("TotalProcesses", presentTime(), getProcessesCount());
        reporter.addDatapoint("Usage", presentTime(), activeProcesses.size() / (double) threadPoolSize);

    }

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


    public double getCurrentRelativeWorkDemand() {
        int totalQueuedWorkRemainder = scheduler.getTotalWorkDemand();
        double activeWorkRemainder = activeProcesses.stream().mapToDouble(value -> value.getDemandRemainder(presentTime(), capacity_per_thread)).sum();
        double workTotal = totalQueuedWorkRemainder + activeWorkRemainder;
        return workTotal / (threadPoolSize * capacity_per_thread);
    }

    /**
     * Forcibly stops all currently running and scheduled processes.
     */
    public synchronized void clear() {
        activeProcesses.forEach(CPUProcess::cancel);
        scheduler.clear();
    }


}

