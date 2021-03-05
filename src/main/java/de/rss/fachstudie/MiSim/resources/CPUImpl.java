package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Pair;

import java.util.HashSet;
import java.util.Set;

public final class CPUImpl extends ExternalEvent {

    private static final int DEFAULT_THREADPOOLSIZE = 3;

    private final MultiDataPointReporter reporter;
    private final CPUProcessScheduler scheduler;
    private final int capacity; //computation capacity of one thread in one (1.0) simulation time unit

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
        reporter = new MultiDataPointReporter(name);
        this.scheduler = scheduler;
        this.capacity = capacity;
        this.threadPoolSize = threadPoolSize;
        activeProcesses = new HashSet<>(threadPoolSize);
    }

    public void submitProcess(CPUProcess process) {
        scheduler.enterProcess(process);
        if (hasProcessAndThreadReady()) {
            forceScheduleNow();
        }
    }

    public void submitRequest(Request request) {
        submitProcess(new CPUProcess(request));
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        while (hasProcessAndThreadReady()) {
            Pair<CPUProcess, Integer> next = scheduler.retrieveNextProcessNoReschedule();
            CPUProcess nextProcess = next.getValue0();
            int nextTotalDemand = next.getValue1();

            nextProcess.stampCurrentBurstStarted(presentTime());
            TimeSpan processBurstDuration = new TimeSpan(nextTotalDemand / (double) capacity);

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
    }

    private boolean hasProcessAndThreadReady() {
        return scheduler.hasThreadsToSchedule() && this.hasThreadsAvailable();
    }

    private int getPerThreadCapacity() {
        return capacity;
    }

    private boolean hasThreadsAvailable() {
        return activeProcesses.size() < threadPoolSize;
    }


    void onBurstFinished(CPUProcess process) {
        if (process.getDemandRemainder() > 0) {
            scheduler.enterProcess(process); //if process is not finished reschedule it
        } else {
            activeProcesses.remove(process); //otherwise the process is not active anymore
        }

        //since at least one thread should be free now, a reschedule happens
        forceScheduleNow();
    }

    private void forceScheduleNow() {
        if (this.isScheduled()) {
            this.reSchedule(presentTime());
        } else {
            this.schedule(presentTime());
        }
    }

    public double getCurrentRelativeWorkDemand() {
        int totalQueuedWorkRemainder = scheduler.getTotalWorkDemand();
        double activeWorkRemainder = activeProcesses.stream().mapToDouble(value -> value.getDemandRemainder(presentTime(), capacity)).sum();
        double workTotal = totalQueuedWorkRemainder + activeWorkRemainder;
        return workTotal / (threadPoolSize * capacity);
    }
}

