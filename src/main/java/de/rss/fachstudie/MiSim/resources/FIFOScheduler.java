package de.rss.fachstudie.MiSim.resources;

import desmoj.core.simulator.Model;
import org.javatuples.Pair;

import java.util.LinkedList;
import java.util.Queue;

/**
 * <b>F</b>irst <b>i</b>n <b>f</b>irst <b>o</b>ut scheduler.
 * <p>
 * Schedules all entered processes in the same order that they arrived in. Always assigns the full work demand needed
 * for a processes.
 *
 * @author Lion Wagner
 */
public class FIFOScheduler extends CPUProcessScheduler {

    private final Queue<CPUProcess> processes = new LinkedList<>();

    public FIFOScheduler(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }


    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        if (!processes.contains(process))
            processes.add(process);
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
        return new Pair<>(processes.poll(), demand);
    }
}
