package de.rss.fachstudie.MiSim.resources.cpu.scheduling;

import java.util.Collections;
import java.util.LinkedList;

import de.rss.fachstudie.MiSim.resources.cpu.CPUProcess;
import org.javatuples.Pair;

/**
 * Scheduler, that schedules all entered processes by their left over work unit demand. Always retrieves the process
 * that hast he least work left first. Always assigns the full work demand needed for a processes.
 *
 * @author Lion Wagner
 */
public class ShortestJobNextScheduler extends CPUProcessScheduler {

    /**
     * Set of CPUProcesses, sorted by left over demand (natural sorting of CPUProcess).
     */
    private final LinkedList<CPUProcess> processes = new LinkedList<>();

    public ShortestJobNextScheduler(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        if (!processes.contains(process)) {
            processes.add(process);
            Collections.sort(processes);
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcessNoReschedule() {
        return retrieveNextProcess();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProcessesToSchedule() {
        return !processes.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalWorkDemand() {
        return processes.stream().mapToInt(CPUProcess::getDemandRemainder).sum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        processes.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return processes.size();
    }
}
