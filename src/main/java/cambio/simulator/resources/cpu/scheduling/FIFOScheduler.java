package cambio.simulator.resources.cpu.scheduling;

import java.util.LinkedList;
import java.util.Queue;

import cambio.simulator.resources.cpu.CPUProcess;
import org.javatuples.Pair;

/**
 * <b>F</b>irst <b>i</b>n <b>f</b>irst <b>o</b>ut scheduler.
 *
 * <p>
 * Schedules all entered processes in the same order that they arrived in. Always assigns the full work demand needed
 * for a processes.
 *
 * @author Lion Wagner
 */
public class FIFOScheduler extends CPUProcessScheduler {

    private final Queue<CPUProcess> processes = new LinkedList<>();

    private int totalWorkDemand = 0;

    public FIFOScheduler(String name) {
        super(name);
    }


    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        //if (!processes.contains(process))
        processes.add(process);
        totalWorkDemand += process.getDemandTotal();
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
        totalWorkDemand -= demand;
        return new Pair<>(processes.poll(), demand);
    }

    /**
     * Pulls the next Process to handle and its assigned time/work quantum.<br> Prevents automatic rescheduling of the
     * process like in round robin scheduling.
     *
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
        return totalWorkDemand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        processes.clear();
        totalWorkDemand = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return processes.size();
    }
}
