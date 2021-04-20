package de.rss.fachstudie.MiSim.resources.cpu.scheduling;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import org.javatuples.Pair;

import java.util.Collections;
import java.util.LinkedList;

/**
 * <b>S</b>hortest <b>P</b>roces <b>n</b>ext scheduler.
 * <p>
 * Schedules all entered processes by their left over work unit demand. Always retrieves the process that hast he least
 * work left first. Always assigns the full work demand needed for a processes.
 *
 * @author Lion Wagner
 */
public class SPNScheduler extends CPUProcessScheduler {

    /**
     * Set of CPUProcesses, sorted by left over demand (natural sorting of CPUProcess)
     */
    private final LinkedList<CPUProcess> processes = new LinkedList<>();

    public SPNScheduler(String name) {
        super(name);
    }

    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        if (!processes.contains(process)) {
            processes.add(process);
            Collections.sort(processes);
        }
    }

    /**
     * Pulls the next Process to handle and how much demand should be accomplished.
     *
     * @return a pair containing the next process to handle and how much demand should be accomplished.
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcess() {
        if (processes.isEmpty()) {
            return null;
        }
        CPUProcess next = processes.poll();
        int demand = next.getDemandTotal();
        return new Pair<>(next, demand);
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
