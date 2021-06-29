package cambio.simulator.resources.cpu.scheduling;

import java.util.ArrayDeque;
import java.util.Deque;

import cambio.simulator.resources.cpu.CPUProcess;
import org.javatuples.Pair;

/**
 * <b>L</b>ast <b>i</b>n <b>f</b>irst <b>o</b>ut scheduler.
 *
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
     * {@inheritDoc}
     */
    @Override
    public synchronized void enterProcess(CPUProcess process) {
        if (!processes.contains(process)) {
            processes.push(process);
        }
    }

    /**
     * {@inheritDoc}
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
