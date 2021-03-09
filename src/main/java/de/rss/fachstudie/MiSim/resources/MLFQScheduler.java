package de.rss.fachstudie.MiSim.resources;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    private final List<RoundRobinScheduler> queues = new ArrayList<>();

    private final HashMap<CPUProcess, Integer> assigned_queue = new HashMap<>(); //holds the information in which queue a

    public MLFQScheduler(String name) {
        this(name, 3);
    }

    public MLFQScheduler(String name, int levels) {
        super(name);
        if (levels <= 0) throw new IllegalArgumentException("Level count has to be positive.");

        for (int i = 0; i < levels; i++) {
            queues.add(new RoundRobinScheduler(name + "_Queue" + i));
        }
    }


    /**
     * Enters the process into the scheduling queue.
     *
     * @param process Process that is to be scheduled
     */
    @Override
    public void enterProcess(CPUProcess process) {
        Objects.requireNonNull(process);
        if (assigned_queue.containsKey(process)) {
            queues.get(assigned_queue.get(process)).enterProcess(process);
        } else {
            queues.get(0).enterProcess(process);
            assigned_queue.put(process, 0);
        }
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
            next = queue.retrieveNextProcessNoReschedule();
            if (next != null) {
                //if Process will not finish in the current burst put it a queue lower
                if (next.getValue0().getDemandRemainder() > next.getValue1()) {
                    queues.get(i + 1).enterProcess(next.getValue0());
                    assigned_queue.put(next.getValue0(), i + 1);
                }
                //otherwise, it will be finished and assigned_queue does not need to hold the information any longer.
                assigned_queue.remove(next.getValue0());
                break;
            }
        }

        if (next == null) {
            next = queues.get(queues.size() - 1).retrieveNextProcess();
        }

        if (next != null) {
            assigned_queue.put(next.getValue0(), queues.size() - 1);
        }
        return next;
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
        //differences to retrieveNextProcess are marked with comments

        Pair<CPUProcess, Integer> next = null;

        for (int i = 0; i < queues.size() - 1; i++) {
            RoundRobinScheduler queue = queues.get(i);
            next = queue.retrieveNextProcessNoReschedule();
            if (next != null) {
                if (next.getValue0().getDemandRemainder() > next.getValue1()) {
                    //does not reschedule to lower queue here, instead tells the assigned_queue that it will be there on next manual enter
                    assigned_queue.put(next.getValue0(), i + 1);
                }
                assigned_queue.remove(next.getValue0());
                break;
            }
        }

        if (next == null) {
            next = queues.get(queues.size() - 1).retrieveNextProcessNoReschedule();//does not reschedule process
        }
        if (next != null) {
            assigned_queue.put(next.getValue0(), queues.size() - 1);
        }
        return next;
    }

    /**
     * @return true if there is a thread ready to schedule, false otherwise
     */
    @Override
    public boolean hasThreadsToSchedule() {
        return queues.stream().anyMatch(CPUProcessScheduler::hasThreadsToSchedule);
    }

    /**
     * @return the sum of the demand remainder of all processes that are currently in queue.
     */
    @Override
    public int getTotalWorkDemand() {
        return queues.stream().mapToInt(RoundRobinScheduler::getTotalWorkDemand).sum();
    }

    /**
     * Clears all current processes from the scheduler
     */
    @Override
    public void clear() {
        queues.forEach(RoundRobinScheduler::clear);
    }

    @Override
    public int size() {
        return queues.stream().mapToInt(CPUProcessScheduler::size).sum();
    }

}
