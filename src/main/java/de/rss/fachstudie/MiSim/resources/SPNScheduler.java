package de.rss.fachstudie.MiSim.resources;

import desmoj.core.simulator.Model;
import org.javatuples.Pair;

import java.util.Collections;
import java.util.LinkedList;

/**
 * <b>S</b>hortest <b>P</b>roces <b>n</b>ext scheduler.
 * <p>
 * Schedules all entered processes by their left over work unit demand.
 * Always retrieves the process that hast he least work left first.
 * Always assigns the full work demand needed for a processes.
 *
 * @author Lion Wagner
 */
public class SPNScheduler extends CPUProcessScheduler {

    /**
     * Set of CPUProcesses, sorted by left over demand (natural sorting of CPUProcess)
     */
    private final LinkedList<CPUProcess> processes = new LinkedList<>();

    public SPNScheduler(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
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
}
