package de.rss.fachstudie.MiSim.resources;

import desmoj.core.simulator.Model;
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

    public LIFOScheduler(Model model, String name, boolean showInTrace) {
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
}
