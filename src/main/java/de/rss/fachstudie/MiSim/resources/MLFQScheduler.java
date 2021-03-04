package de.rss.fachstudie.MiSim.resources;

import desmoj.core.simulator.Model;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a <b>M</b>ulti-<b>L</b>evel <b>F</b>eedback <b>Q</b>ueue scheduler.
 * <p>
 * Entering processes will be put into a high priority round robin queue. If the processes is not completed after the
 * first round it drops down to next lower level (also a round robin queue). This can continue until the processes
 * arrives at the lowest level. There, a normal round robin scheduling is employed.
 * <p>
 * This scheduler always prioritizes higher levels over lower levels. Therefore, short and recently arrived processes
 * are prioritized, while long running processes are handled with lower priority. Fairness is not guaranteed  with this
 * implementation.
 * <p>
 * The number of Queues can be adjusted by using the "level" parameter of the constructor. Default value is 3.
 *
 * @author Lion Wagner
 */
public class MLFQScheduler extends CPUProcessScheduler {

    List<RoundRobinScheduler> queues = new ArrayList<>();

    public MLFQScheduler(Model model, String name, boolean showInTrace) {
        this(model, name, showInTrace, 3);
    }

    public MLFQScheduler(Model model, String name, boolean showInTrace, int levels) {
        super(model, name, showInTrace);
        assert levels > 0;

        for (int i = 0; i < levels; i++) {
            queues.add(new RoundRobinScheduler(model, name + "_Queue" + i, false));
        }
    }


    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public void enterProcess(CPUProcess process) {
        queues.get(0).enterProcess(process);
    }

    /**
     * Pulls the next Process to handle and how much demand should be accomplished.
     *
     * @return a pair containing the next process to handle and how much demand should be accomplished.
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcess() {
        Pair<CPUProcess, Integer> next = null;

        for (int i = 0; i < queues.size() - 1; i++) {
            RoundRobinScheduler queue = queues.get(i);
            next = queue.retrieveNextProcessNoRotate();
            if (next != null) {
                if (next.getValue0().getDemandRemainder() > next.getValue1()) { //if Process will not finish in the current burst put it a queue lower
                    queues.get(i + 1).enterProcess(next.getValue0());
                }
                break;
            }
        }

        if (next == null)
            next = queues.get(queues.size() - 1).retrieveNextProcess();
        return next;
    }
}
