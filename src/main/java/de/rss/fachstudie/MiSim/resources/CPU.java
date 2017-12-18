package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.Pattern;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CPU extends Event<Thread> {
    enum CB_STATE  {OPEN, HALFOPEN, CLOSED};

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

    private boolean hasCircuitBreaker = false;
    private CB_STATE cbState = CB_STATE.OPEN;
    private boolean trialSent = false;
    private Thread trialThread = null;
    private double circuitBreakerTriggered = 0;
    private double retryTime = 0;
    private double responseTimelimit = 0;
    private double maxResponseTime = 0;

    public CPU(Model owner, String name, boolean showInTrace, int id, int sid, int capacity) {
        super(owner, name, showInTrace);

        model = (MainModel) owner;
        this.id = id;
        this.sid = sid;
        this.capacity = capacity;
        lastThreadEntry = 0;
        cpuUsageMean = new TreeMap<>();
        existingThreads = new Queue<Thread>(owner, "", false, false);

        if(model.allMicroservices.get(id).hasPattern("Thread Pool")) {
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
    }

    @Override
    public void eventRoutine(Thread threadToEnd) throws SuspendExecution {
        for(Thread thread : activeThreads) {
            thread.subtractDemand((int) smallestThread);
            if(thread.getDemand() == 0 || thread == threadToEnd) {
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
        hasCircuitBreaker = operation.hasPattern("Circuit Breaker");
        if (hasCircuitBreaker) {
            Pattern circuitBreaker = operation.getPattern("Circuit Breaker");
            if (responseTimelimit == 0) {
                if (circuitBreaker.getArguments().length > 0) {
                    responseTimelimit = circuitBreaker.getArguments()[0];
                } else {
                    // Default 10s
                    responseTimelimit = 10;
                }
                if (circuitBreaker.getArguments().length > 1) {
                    retryTime = circuitBreaker.getArguments()[1];
                } else {
                    // Default 1s
                    retryTime = 1;
                }
            }
            checkForCircuitBreaker();
        }

        if (!hasCircuitBreaker || (cbState == CB_STATE.CLOSED || (cbState == CB_STATE.HALFOPEN && !trialSent))) {
            if(!trialSent && cbState == CB_STATE.HALFOPEN) {
                trialThread = thread;
                trialSent = true;
            }

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
        } else {
            double last = 0;
            List<Double> values = model.circuitBreakerStatistics.get(id).get(sid).getDataValues();
            if (values != null)
                last = values.get(values.size() - 1);
            model.circuitBreakerStatistics.get(id).get(sid).update(last + 1);
            thread.scheduleEndEvent();
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

    private void calculateMin() {
        Thread smallestThreadInstance = null;
        if (activeThreads.size() > 0) {
            smallestThreadInstance = activeThreads.get(0);
            smallestThread = Double.POSITIVE_INFINITY;
            for (Thread t : activeThreads) {
                if (t.getDemand() < smallestThread) {
                    smallestThread = t.getDemand();
                    smallestThreadInstance = t;
                }
            }
        }

        // schedule to time when smallest thread is done
        if(!activeThreads.isEmpty()) {
            cycleTime = (activeThreads.size() * smallestThread) / capacity;

            if(isScheduled()) {
                reSchedule(new TimeInstant(cycleTime + model.presentTime().getTimeAsDouble(), model.getTimeUnit()));
            } else {
                schedule(smallestThreadInstance, new TimeInstant(cycleTime + model.presentTime().getTimeAsDouble(), model.getTimeUnit()));
            }
        }
    }

    private void checkForCircuitBreaker() {
        for (Thread activeThread : activeThreads) {
            if (activeThread.getCreationTime() > circuitBreakerTriggered) {
                if (model.presentTime().getTimeAsDouble() - activeThread.getCreationTime() > maxResponseTime) {
                    maxResponseTime = model.presentTime().getTimeAsDouble() - activeThread.getCreationTime();
                }
            }
        }

        if (maxResponseTime > responseTimelimit && cbState == CB_STATE.CLOSED) {
            cbState = CB_STATE.OPEN;
            maxResponseTime = 0;
            circuitBreakerTriggered = model.presentTime().getTimeAsDouble();
        }

        if (cbState == CB_STATE.OPEN && model.presentTime().getTimeAsDouble() > (circuitBreakerTriggered + retryTime)) {
            cbState = CB_STATE.HALFOPEN;
        }

        if (cbState == CB_STATE.HALFOPEN && trialThread != null && trialThread.getDemand() == 0) {
            trialSent = false;
            if (model.presentTime().getTimeAsDouble() - trialThread.getCreationTime() < responseTimelimit) {
                cbState = CB_STATE.CLOSED;
            } else {
                cbState = CB_STATE.HALFOPEN;
            }
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

    public void addExistingThread(Thread thread) {
        existingThreads.insert(thread);
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
}
