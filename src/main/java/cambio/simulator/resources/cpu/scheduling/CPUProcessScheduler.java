package cambio.simulator.resources.cpu.scheduling;


import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.resources.cpu.CPUProcess;
import org.javatuples.Pair;

/**
 * Superclass that provides the interface for all CPU scheduling strategies.
 *
 * @see CPUProcess
 */
public abstract class CPUProcessScheduler {


    public CPUProcessScheduler(String name) {
    }

    /**
     * Enters the process into the scheduling queue.
     *
     * @param process {@code CPUProcess} that is to be scheduled
     * @see CPUProcess
     */
    public abstract void enterProcess(CPUProcess process);

    /**
     * Pulls the next {@code CPUProcess} to handle and its assigned time/work quantum.
     *
     * @return a pair containing the next {@code CPUProcess} to handle and its assigned time/work quantum.
     * @see CPUProcess
     */
    public abstract Pair<CPUProcess, Integer> retrieveNextProcess();


    /**
     * Pulls the next {@code CPUProcess} to handle and its assigned time/work quantum.<br> Prevents automatic
     * rescheduling of the process like in round-robin scheduling.
     *
     * <p>
     * This method is used to offer scheduling for multithreading. But requires manual rescheduling of unfinished
     * processes.
     *
     * @return a pair containing the next {@code CPUProcess} to handle and its assigned time quantum.
     * @see CPUProcess
     */
    public abstract Pair<CPUProcess, Integer> retrieveNextProcessNoReschedule();

    /**
     * Checks whether this scheduler currently has processes ready to be retrieved from scheduling.
     *
     * @return true if there is a process ready to schedule, false otherwise
     */
    public abstract boolean hasProcessesToSchedule();

    /**
     * Calculates the sum of all scheduled process's demand remainders.
     *
     * @return the sum of the demand remainder of all processes that are currently in queue.
     */
    public abstract int getTotalWorkDemand();

    /**
     * Clears all current processes from the scheduler.
     */
    public abstract void clear();

    /**
     * Counts the amount of processes that are currently scheduled.
     *
     * @return the amount of processes that are currently scheduled.
     */
    public abstract int size();
}
