package de.rss.fachstudie.MiSim.resources;


import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import org.javatuples.Pair;

/**
 * Superclass that provides the interface for all scheduling strategies.
 */
public abstract class CPUProcessScheduler extends Entity {

    protected MultiDataPointReporter reporter;

    public CPUProcessScheduler(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
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
     *
     * @return a pair containing the next process to handle and its assigned time/work quantum.
     */
    public abstract Pair<CPUProcess, Integer> retrieveNextProcess();

}
