package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.entities.patterns.Pattern;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CPU extends Event<Thread> {
    private MainModel model;
    private int id = -1;
    private int sid = -1;
    private int capacity = 0;
    private double robinTime = 10;
    private double cycleTime = 0;
    private double lastThreadEntry;
    private double smallestThread = 0.0;
    private Map<Double, Double> cpuUsageMean;

    private Queue<Thread> activeThreads;
    private Queue<Thread> waitingThreads;
    private Queue<Thread> existingThreads;
    private boolean hasThreadPool = false;
    private int threadPoolSize = 0;
    private boolean hasThreadQueue = false;
    private int threadQueueSize = 0;

    private List<CircuitBreakerData> circuitBreakerDataList;

    private List<String> openCircuits;
    private double delay = 0;

    public CPU(Model owner, String name, boolean showInTrace, int id, int sid, int capacity) {
        super(owner, name, showInTrace);

        model = (MainModel) owner;
        this.id = id;
        this.sid = sid;
        this.capacity = capacity;
        lastThreadEntry = 0;
        cpuUsageMean = new TreeMap<>();
        existingThreads = new Queue<>(owner, "", false, false);

        if (model.allMicroservices.get(id).hasPattern("Thread Pool")) {
            Pattern threadPool = model.allMicroservices.get(id).getPattern("Thread Pool");
            if (threadPool.getArguments().length > 0) {
                threadPoolSize = threadPool.getArgument(0);
            } else {
                // Default
                threadPoolSize = 10;
            }
            activeThreads = new Queue<>(owner, "", QueueBased.FIFO, threadPoolSize, false, false);
            hasThreadPool = true;
            if (threadPool.getArguments().length > 1) {
                threadQueueSize = threadPool.getArgument(1);
            } else {
                // Default
                threadQueueSize = 10;
            }
            waitingThreads = new Queue<>(owner, "", QueueBased.FIFO, threadQueueSize, false, false);
            hasThreadQueue = true;
        } else {
            activeThreads = new Queue<>(owner, "", false, false);
        }

        circuitBreakerDataList = new ArrayList<CircuitBreakerData>();
        openCircuits = new ArrayList<String>();
    }

    @Override
    public void eventRoutine(Thread threadToEnd) throws SuspendExecution {
        sendTraceNote("Working on Operation " + threadToEnd.getOperation() +  ", left over demand=" + threadToEnd.getDemand());
        for (Thread thread : activeThreads) {
            thread.subtractDemand((int) smallestThread);
            if (thread.getDemand() == 0 || thread == threadToEnd) {
                thread.scheduleEndEvent();
                activeThreads.remove(thread);
            }
        }
        calculateMin();
    }

    public void addThread(Thread thread, Operation operation) {
        // update all threads that are currently in the active queue
        int robins = (int) Math.round((model.presentTime().getTimeAsDouble() - lastThreadEntry) * 1000 / robinTime);
        for (int i = 0; i < robins; i++) {
            if (activeThreads.size() > 0) {
                Thread activeThread = activeThreads.get(i % activeThreads.size());
                if (activeThread.getDemand() == 0) {
                    activeThread.scheduleEndEvent();
                    activeThreads.remove(activeThread);
                } else {
                    activeThread.subtractDemand(robinTime);
                }
            }
        }

        lastThreadEntry = this.model.presentTime().getTimeAsDouble();


        // check for patterns
        if (!hasThreadPool || activeThreads.size() < threadPoolSize) {
            // cpu has no thread pool, or the size of the thread pool is big enough
            activeThreads.insert(thread);
        } else {
            if (hasThreadQueue) {
                if (waitingThreads.size() < threadQueueSize) {

                    // a thread queue exists and the size is big enough
                    waitingThreads.insert(thread);
                } else {

                    // thread waiting queue is too big, send default response
                    thread.scheduleEndEvent();

                    // statistics
                    double last = 0;
                    List<Double> values = model.threadQueueStatistics.get(thread.getId()).get(thread.getSid()).getDataValues();
                    if (values != null)
                        last = values.get(values.size() - 1);
                    model.threadQueueStatistics.get(thread.getId()).get(thread.getSid()).update(last + 1);
                }
            } else {
                // thread pool is too big, send default response
                thread.scheduleEndEvent();

                // statistics
                double last = 0;
                List<Double> values = model.threadPoolStatistics.get(thread.getId()).get(thread.getSid()).getDataValues();
                if (values != null)
                    last = values.get(values.size() - 1);
                model.threadPoolStatistics.get(thread.getId()).get(thread.getSid()).update(last + 1);
            }
        }

        // Shift from waiting queue to the active queue
        if (hasThreadQueue) {
            int freeSlots = threadPoolSize - activeThreads.size();
            for (int index = 0; index < freeSlots; ++index) {
                activeThreads.insert(waitingThreads.first());
                waitingThreads.removeFirst();
            }
        }

        calculateMin();
    }


    /**
     * Calculates which Thread has the least amount of work left and reschedules the this event to that point in time
     */
    private void calculateMin() {
        Thread smallestThreadInstance = null;
        if (activeThreads.size() > 0) {
            smallestThreadInstance = activeThreads.get(0);
            smallestThread = smallestThreadInstance.getDemand();
            for (Thread t : activeThreads) {
                if (t.getDemand() < smallestThread) {
                    smallestThread = t.getDemand();
                    smallestThreadInstance = t;
                }
            }
        }

        if (!activeThreads.isEmpty()) {
            cycleTime = (activeThreads.size() * smallestThread) / capacity;

            if (isScheduled()) {
                reSchedule(new TimeInstant(cycleTime + model.presentTime().getTimeAsDouble() + delay, model.getTimeUnit()));
            } else {
                schedule(smallestThreadInstance, new TimeInstant(cycleTime + model.presentTime().getTimeAsDouble() + delay, model.getTimeUnit()));
            }
        }
    }

    public void checkCircuitBreakers() {

        if (circuitBreakerDataList.isEmpty()) {
            return;
        }

        double time = this.model.presentTime().getTimeAsDouble();

        // Check for each thread if the timeout limit has been reached
        if (existingThreads != null) {
            for (Thread thread : existingThreads) {
                if (thread != null && thread.getOperation().hasCircuitBreaker()) {
                    double threadCreationTime = thread.getCreationTime();
                    double threadLifeTime = time - threadCreationTime;

                    Operation threadOperation = thread.getOperation();

                    if (threadOperation != null) {
                        CircuitBreakerData threadOpCBData = getCircuitBreakerData(threadOperation.getName());
                        CircuitBreaker circuitBreaker = threadOperation.getCircuitBreaker();

                        if (circuitBreaker != null) {
                            // Thread timed out
                            if (threadLifeTime >= circuitBreaker.getTimeout()) {
                                // Add failure to stats
                                threadOpCBData.increaseErrorCount();
                            }
                        } else {
                            System.out.println("Operation that should have a circuit breaker doesn't have one");
                        }
                    }
                }
            }
        }

        for (CircuitBreakerData cbData : circuitBreakerDataList) {

            CircuitBreaker circuitBreaker = cbData.getOperation().getCircuitBreaker();

            // Circuit is closed
            if (cbData.getState() == CircuitBreaker.State.CLOSED) {
                // Check if circuit reached requestVolumeThreshold
                if (cbData.getRequestVolume() >= circuitBreaker.getRequestVolumeThreshold()) {
                    double errorPercentage = cbData.getErrorCount() / cbData.getRequestVolume();

                    // Check if we've reached the errorThresholdPercentage
                    if (errorPercentage >= circuitBreaker.getErrorThresholdPercentage()) {
                        // Threshold reached -> open the circuit
                        openCircuit(cbData);
                    }
                }
            }

            // Circuit is open
            if (cbData.getState() == CircuitBreaker.State.OPEN) {
                // Sleep window expired -> Set state to half open
                if (time > (cbData.getCbOpenTime() + circuitBreaker.getSleepWindow())) {
                    halfOpenCircuit(cbData);
                }
            }

            // Circuit is half open
            if (cbData.getState() == CircuitBreaker.State.HALF_OPEN) {
                // Check if trial has been sent
                if (cbData.getTrialThread() != null) {
                    double trialStartTime = cbData.getTrialThread().getCreationTime();

                    // Trial timed out -> Set state to open
                    if ((time - trialStartTime) > circuitBreaker.getTimeout()) {
                        openCircuit(cbData);
                        cbData.setTrialSent(false);
                        cbData.setTrialThread(null);
                    } else if (cbData.getTrialThread().getDemand() == 0) {
                        // Trial is handled -> Set state to closed
                        closeCircuit(cbData);
                    }
                }
            }
        }

        if (existingThreads != null) {
            for (Thread thread : existingThreads) {
                if (thread != null && thread.getOperation().hasCircuitBreaker()) {
                    double threadCreationTime = thread.getCreationTime();
                    double threadLifeTime = time - threadCreationTime;

                    Operation threadOperation = thread.getOperation();

                    if (threadOperation != null) {
                        CircuitBreaker circuitBreaker = threadOperation.getCircuitBreaker();
                        CircuitBreakerData cbData = getCircuitBreakerData(threadOperation.getName());

                        if (circuitBreaker != null) {

                            // Reset stats of new rolling window
                            if ((time - cbData.getRollingWindowStartTime()) > circuitBreaker.getRollingWindow()) {
                                cbData.setRequestVolume(0);
                                cbData.setErrorCount(0);
                                cbData.setRollingWindowStartTime(time);
                            }

                            // Thread timed out
                            if (threadLifeTime >= circuitBreaker.getTimeout()) {
                                thread.scheduleEndEvent();
                                if (activeThreads.contains(thread)) {
                                    activeThreads.remove(thread);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void openCircuit(CircuitBreakerData cbData) {
        String operationName = cbData.getOperation().getName();

        cbData.setState(CircuitBreaker.State.OPEN);
        cbData.setCbOpenTime(this.model.presentTime().getTimeAsDouble());

        if (!openCircuits.contains(operationName)) {
            openCircuits.add(operationName);
        }
    }

    private void closeCircuit(CircuitBreakerData cbData) {
        String operationName = cbData.getOperation().getName();

        cbData.setState(CircuitBreaker.State.CLOSED);
        cbData.setTrialSent(false);
        cbData.setTrialThread(null);

        if (openCircuits.contains(operationName)) {
            openCircuits.remove(operationName);
        }
    }

    private void halfOpenCircuit(CircuitBreakerData cbData) {
        String operationName = cbData.getOperation().getName();

        cbData.setState(CircuitBreaker.State.HALF_OPEN);
        if (openCircuits.contains(operationName)) {
            openCircuits.remove(operationName);
        }
    }

    public void releaseUnfinishedThreads() {
        for (int thread = activeThreads.size() - 1; thread >= 0; thread--) {
            activeThreads.get(thread).scheduleEndEvent();
        }
    }

    public Queue<Thread> getExistingThreads() {
        return existingThreads;
    }

    public void addExistingThread(Thread thread, Operation operation) {

        String operationName = operation.getName();

        // Create new circuitBreakerDataMap entry if operation has a circuitBreaker, but isn't represented in the map
        // yet
        if (operation.hasCircuitBreaker()) {

            CircuitBreakerData cbData = getCircuitBreakerData(operationName);

            if (cbData == null) {
                cbData = new CircuitBreakerData(operation);
                cbData.setRollingWindowStartTime(model.presentTime().getTimeAsDouble());
                circuitBreakerDataList.add(cbData);
            }

            // Operation has a circuit breaker
            // cbData is initialized
            // RollingWindow is started
        }

        checkCircuitBreakers();

        if (operation.hasCircuitBreaker()) {
            CircuitBreakerData cbData = getCircuitBreakerData(operation.getName());

            if (!(cbData.isTrialSent()) && (cbData.getState() == CircuitBreaker.State.HALF_OPEN)) {
                cbData.setTrialThread(thread);
                cbData.setTrialSent(true);
                existingThreads.insert(thread);

                if (!openCircuits.contains(operationName)) {
                    openCircuits.add(operationName);
                }

            } else if (cbData.getState() == CircuitBreaker.State.OPEN) {
                // Circuit is open -> fallback/fail fast
                double last = 0;
                List<Double> values = model.circuitBreakerStatistics.get(id).get(sid).getDataValues();
                if (values != null)
                    last = values.get(values.size() - 1);
                model.circuitBreakerStatistics.get(id).get(sid).update(last + 1);

                // Kill Thread
                thread.scheduleEndEvent();
            } else if (cbData.getState() == CircuitBreaker.State.CLOSED) {
                cbData.increaseRequestVolume();
                existingThreads.insert(thread);
            }
        } else {
            existingThreads.insert(thread);
        }
    }

    public void removeExistingThread(Thread thread) {
        existingThreads.remove(thread);
    }

    public Queue<Thread> getActiveThreads() {
        return activeThreads;
    }

    public int getCapacity() {
        return capacity;
    }

    public void collectUsage() {
        cpuUsageMean.put(model.presentTime().getTimeAsDouble(), getUsage());
    }

    public double getMeanUsage(double values) {
        int collected = 0;
        double usage = 0;
        List<Double> vals = new ArrayList<Double>(cpuUsageMean.keySet());
        for (int i = vals.size() - 1; i > 0; --i) {
            if (vals.get(i) < model.presentTime().getTimeAsDouble() - values)
                break;
            usage += cpuUsageMean.get(vals.get(i));
            collected++;
        }
        return usage / collected;
    }

    private double getUsage() {
        if (activeThreads.size() > 0)
            return 1.0;
        else
            return 0.0;
    }

    private CircuitBreakerData getCircuitBreakerData(String operationName) {
        for (CircuitBreakerData data : circuitBreakerDataList) {
            if (data.getOperation().getName().equals(operationName)) {
                return data;
            }
        }
        return null;
    }

    public List<String> getOpenCircuits() {
        return openCircuits;
    }

    public void applyDelay(double delay) {
        this.delay = delay;
    }
}

