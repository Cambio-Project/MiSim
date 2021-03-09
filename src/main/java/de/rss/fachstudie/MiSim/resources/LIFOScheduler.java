package de.rss.fachstudie.MiSim.resources;

import org.javatuples.Pair;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * <b>L</b>ast <b>i</b>n <b>f</b>irst <b>o</b>ut scheduler.
 * <p>
 * Schedules all entered processes in the reversed order that they arrived in. The processes that entered last, will be
 * retrieved first. Always assigns the full work demand needed for a processes.
 *
 * @author Lion Wagner
 */
public class LIFOScheduler extends CPUProcessScheduler {

    private final Deque<CPUProcess> processes = new ArrayDeque<>();

    public LIFOScheduler(String name) {
        super(name);
    }


    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        if (!processes.contains(process))
            processes.push(process);
    }

    /**
     * Pulls the next Process to handle and how much demand should be accomplished.
     *
     * @return a pair containing the next process to handle and how much demand should be accomplished.
     */
    @Override
    public synchronized Pair<CPUProcess, Integer> retrieveNextProcess() {
        if (processes.isEmpty()) {
            return null;
        }

        int demand = processes.peek().getDemandTotal();
        return new Pair<>(processes.pop(), demand);
    }

    /**
     * Pulls the next Process to handle and its assigned time/work quantum.<br> Prevents automatic rescheduling of the
     * process like in round robin scheduling.
     * <p>
     * This method is used to offer scheduling for multithreading.
     *
     * @return a pair containing the next process to handle and its assigned time quantum.
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcessNoReschedule() {
        return retrieveNextProcess();
    }

    /**
     * @return true if there is a thread ready to schedule, false otherwise
     */
    @Override
    public boolean hasThreadsToSchedule() {
        return !processes.isEmpty();
    }

    /**
     * @return the sum of the demand remainder of all processes that are currently in queue.
     */
    @Override
    public int getTotalWorkDemand() {
        return processes.stream().mapToInt(CPUProcess::getDemandRemainder).sum();
    }

    /**
     * Clears all current processes from the scheduler
     */
    @Override
    public void clear() {
        processes.clear();
    }


    @Override
    public int size() {
        return processes.size();
    }
}
