package cambio.simulator.resources.cpu.scheduling;

import java.util.*;

import cambio.simulator.resources.cpu.CPUProcess;
import org.javatuples.Pair;

/**
 * <b>S</b>elf <b>a</b>djusting <b>R</b>ound <b>R</b>obin (SARR) scheduler with dynamic median-based quantum
 * calculation.
 *
 * <p>
 * Algorithm developed by Ram Matarneh: <i>Self-Adjustment Time Quantum in Round Robin Algorithm Depending on Burst Time
 * of the Now Running Processes</i>
 *
 * <p>
 * Executes a round robin scheduling where the assigned work quantum is the median of all current left over work demands
 * of all entered processes. The quantum is updated each time a new processes enters or all processes had a round with
 * the current quantum. This provides a shorter average waiting and turn-around time than the fixed quantum round robin.
 * Further, this round robin scheduler adjust dynamically to demand sizes that can heavily vary between
 * experiments/architecture descriptions.
 *
 * <p>
 * Keeps processes in order (first come first serve). And ensures fairness.
 *
 * @author Lion Wagner
 * @see <a href=https://www.researchgate.net/publication/40832774_Self-Adjustment_Time_Quantum_in_Round_Robin_Algorithm_Depending_on_Burst_Time_of_the_Now_Running_Processes>SARR
 *     algorithm</a>
 */
public final class RoundRobinScheduler extends CPUProcessScheduler {

    //TODO: 25 is the default value given by the paper its there to prevent too many context switches
    //However, this might vary from CPU to CPU, depending on total thread capacity. Therefore, this should actually
    //made dynamic at some point.
    private static final int MINIMUM_QUANTUM = 25;

    private final Queue<CPUProcess> processes = new ArrayDeque<>();
    private final HashSet<CPUProcess> executedWithCurrentQuantum = new HashSet<>();
    private int currentQuantum;
    private boolean updateQuantum = true;

    public RoundRobinScheduler(String name) {
        super(name);
    }

    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public void enterProcess(CPUProcess process) {
        processes.add(process);
        updateQuantum = true;
    }

    /**
     * Pulls the next Process to handle and its assigned time/work quantum.
     *
     * @return a pair containing the next process to handle and its assigned time/work quantum.
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcess() {
        if (updateQuantum) {
            updateQuantum();
        }

        CPUProcess nextProcess = processes.poll();
        if (nextProcess == null) {
            return null;
        }

        executedWithCurrentQuantum.add(nextProcess);

        int nextDemand = nextProcess.getDemandRemainder();

        Pair<CPUProcess, Integer> output;
        if (nextDemand <= currentQuantum) {
            output = new Pair<>(nextProcess, nextDemand);
        } else {
            processes.add(nextProcess); //put at end of Queue
            output = new Pair<>(nextProcess, currentQuantum);
        }

        if (executedWithCurrentQuantum.contains(processes.peek())) {
            updateQuantum = true;
        }
        return output;

    }

    /**
     * Interface used by Multi Level Feedback queues.
     *
     * <p>
     * Does not put the process back into the Queue.
     *
     * @return a pair containing the next process to handle and its assigned time quantum.
     */
    @Override
    public Pair<CPUProcess, Integer> retrieveNextProcessNoReschedule() {
        Pair<CPUProcess, Integer> nextTarget = retrieveNextProcess();
        if (nextTarget == null) {
            return null;
        }
        processes.remove(nextTarget.getValue0());
        return nextTarget;
    }


    private void updateQuantum() {
        executedWithCurrentQuantum.clear();

        if (processes.isEmpty()) {
            currentQuantum = MINIMUM_QUANTUM;
            return;
        }

        List<CPUProcess> list = new ArrayList<>(processes);
        Collections.sort(list);

        int median;
        if (list.size() % 2 == 0) {
            int remainder1 = list.get((list.size() - 1) / 2).getDemandRemainder();
            int remainder2 = list.get((list.size() - 1) / 2 + 1).getDemandRemainder();
            median = (int) (Math.ceil(remainder1 + remainder2) / 2.0);
        } else {
            median = list.get(list.size() / 2).getDemandRemainder();
        }

        currentQuantum = Math.max(median, MINIMUM_QUANTUM);
        updateQuantum = false;
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
        executedWithCurrentQuantum.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return processes.size();
    }
}
