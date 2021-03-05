package de.rss.fachstudie.MiSim.resources;


import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import org.javatuples.Pair;

/**
 * Superclass that provides the interface for all scheduling strategies.
 */
public abstract class CPUProcessScheduler {

    protected MultiDataPointReporter reporter;

    public CPUProcessScheduler(String name) {
        reporter = new MultiDataPointReporter(name);
    }

    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    public abstract void enterProcess(CPUProcess process);

    /**
     * Pulls the next Process to handle and its assigned time/work quantum.
     * <p>
     * For more complicated schedulers like a {@link RoundRobinScheduler} or {@link MLFQScheduler} this method does
     *
     * @return a pair containing the next process to handle and its assigned time/work quantum.
     */
    public abstract Pair<CPUProcess, Integer> retrieveNextProcess();


    /**
     * Pulls the next Process to handle and its assigned time/work quantum.<br> Prevents automatic rescheduling of the
     * process like in round robin scheduling.
     * <p>
     * This method is used to offer scheduling for multithreading. But requires manual rescheduling of unfinished
     * processes.
     *
     * @return a pair containing the next process to handle and its assigned time quantum.
     */
    public abstract Pair<CPUProcess, Integer> retrieveNextProcessNoReschedule();

    /**
     * @return true if there is a thread ready to schedule, false otherwise
     */
    public abstract boolean hasThreadsToSchedule();

    /**
     * @return the sum of the demand remainder of all processes that are currently in queue.
     */
    public abstract int getTotalWorkDemand();

    /**
     * Clears all current processes from the scheduler
     */
    public abstract void clear();
}
