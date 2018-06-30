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

    private boolean hasCircuitBreaker = false;
    private CircuitBreaker.State cbState = CircuitBreaker.State.OPEN;
    private boolean trialSent = false;
    private Thread trialThread = null;
    private double circuitBreakerTriggered = 0;
    private double sleepWindow = 0;
    private double timeout = 0;
    private double rollingWindow = 0;
    private double requestVolumeThreshold = 0;
    private double errorThresholdPercentage = 0;
    private double maxResponseTime = 0;

    // Circuit breaker stats
    private double requestVolume = 0;
    private double errorCount = 0;
    private double rollingWindowStarted = 0;

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
        hasCircuitBreaker = operation.hasCircuitBreaker();
        CircuitBreaker circuitBreaker = null;
        if (hasCircuitBreaker) {

            // Start first rolling window
            if (rollingWindowStarted == 0) {
                rollingWindowStarted = model.presentTime().getTimeAsDouble();
            }

            // Get circuit breaker data
            circuitBreaker = operation.getCircuitBreaker();
            timeout = circuitBreaker.getTimeout();
            sleepWindow = circuitBreaker.getSleepWindow();
            rollingWindow = circuitBreaker.getRollingWindow();
            requestVolumeThreshold = circuitBreaker.getRequestVolumeThreshold();
            errorThresholdPercentage = circuitBreaker.getErrorThresholdPercentage();

            checkForCircuitBreaker();
        }

        // Operation doesn't have a circuit breaker or the circuit is closed or the trial should be send
        if (!hasCircuitBreaker || (cbState == CircuitBreaker.State.CLOSED || (cbState == CircuitBreaker.State.HALF_OPEN && !trialSent))) {
            // Circuit is half open -> Let one request through
            if (!trialSent && cbState == CircuitBreaker.State.HALF_OPEN) {
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
            // Circuit is open -> fallback/fail fast
            double last = 0;
            List<Double> values = model.circuitBreakerStatistics.get(id).get(sid).getDataValues();
            if (values != null)
                last = values.get(values.size() - 1);
            model.circuitBreakerStatistics.get(id).get(sid).update(last + 1);

            // Kill Thread
            thread.scheduleEndEvent();
            thread.getMobject().killDependencies();
            activeThreads.remove(thread);
        }

        // Shift from waiting queue to the active queue
        if (hasThreadQueue) {
            int freeSlots = threadPoolSize - activeThreads.size();
            for (int index = 0; index < freeSlots; ++index) {
                activeThreads.insert(waitingThreads.first());
                waitingThreads.removeFirst();
            }
        }

        // Increment request volume
        requestVolume++;

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

        // Check for each thread if the timeout limit has been reached
        for (Thread thread : activeThreads) {
            // Thread timed out
            if ((lastThreadEntry - thread.getCreationTime()) > timeout) {
                // Add failure to stats
                errorCount++;
            }
        }

        // Circuit is closed
        if (cbState == CircuitBreaker.State.CLOSED) {
            // Check if we've reached the requestVolumeThreshold
            if (requestVolume >= requestVolumeThreshold) {
                double errorPercentage = calculateErrorPercentage();

                // Check if we've reached the errorThresholdPercentage
                if (errorPercentage >= errorThresholdPercentage) {
                    // Threshold reached -> open the circuit
                    cbState = CircuitBreaker.State.OPEN;
                    circuitBreakerTriggered = lastThreadEntry;
                }
            }
        }

        // Circuit is open
        if (cbState == CircuitBreaker.State.OPEN) {
            // Sleep window expired -> Set state to half open
            if (lastThreadEntry > (circuitBreakerTriggered + sleepWindow)) {
                cbState = CircuitBreaker.State.HALF_OPEN;
            }
        }

        // Circuit is half open
        if (cbState == CircuitBreaker.State.HALF_OPEN) {
            // Check if trial has been sent
            if (trialThread != null) {
                double trialStart = trialThread.getCreationTime();

                // Trial is handled -> set state to closed
                if (trialThread.getDemand() == 0) {
                    cbState = CircuitBreaker.State.CLOSED;
                    trialSent = false;
                }

                // Trial timed out -> set state to open
                if ((lastThreadEntry - trialStart) > timeout) {
                    cbState = CircuitBreaker.State.OPEN;
                    circuitBreakerTriggered = lastThreadEntry;
                    trialSent = false;
                    // Trial thread will get killed together with the other threads that timed out at the end of this
                    // method
                }
            }
        }

        // Kill timed out threads
        for (Thread thread : activeThreads) {
            // Thread timed out
            if ((lastThreadEntry - thread.getCreationTime()) > timeout) {
                // Kill thread (fallback/fail fast)
                thread.scheduleEndEvent();
                thread.getMobject().killDependencies();
                activeThreads.remove(thread);
            }
        }

        // Reset stats if new rolling window
        if ((lastThreadEntry - rollingWindowStarted) > rollingWindow) {
            requestVolume = 0;
            errorCount = 0;
            rollingWindowStarted = lastThreadEntry;
        }

        // -------------------------------------------- OLD --------------------------------------------------
//        for (Thread activeThread : activeThreads) {
//            if (activeThread.getCreationTime() > circuitBreakerTriggered) {
//                if (model.presentTime().getTimeAsDouble() - activeThread.getCreationTime() > maxResponseTime) {
//                    maxResponseTime = model.presentTime().getTimeAsDouble() - activeThread.getCreationTime();
//                }
//            }
//        }
//
//        // Circuit closed and response timed out -> Open circuit
//        if (maxResponseTime > timeout && cbState == CircuitBreaker.State.CLOSED) {
//            // Open circuit
//            cbState = CircuitBreaker.State.OPEN;
//            maxResponseTime = 0;
//            circuitBreakerTriggered = model.presentTime().getTimeAsDouble();
//        }
//
//        // Circuit is open and sleep window is over -> Set circuit to half open
//        if (cbState == CircuitBreaker.State.OPEN && model.presentTime().getTimeAsDouble() > (circuitBreakerTriggered + sleepWindow)) {
//            cbState = CircuitBreaker.State.HALF_OPEN;
//        }
//
//        // Circuit is half open and trial is over -> decide if circuit should remain open or should be closed again
//        if (cbState == CircuitBreaker.State.HALF_OPEN && trialThread != null && trialThread.getDemand() == 0) {
//            trialSent = false;
//            if (model.presentTime().getTimeAsDouble() - trialThread.getCreationTime() < timeout) {
//                // Request didn't time out -> close circuit again
//                cbState = CircuitBreaker.State.CLOSED;
//            } else {
//                // Request timed out -> keep circuit open
//                cbState = CircuitBreaker.State.OPEN;
//                circuitBreakerTriggered = model.presentTime().getTimeAsDouble();
//            }
//        }
        // -------------------------------------------- OLD --------------------------------------------------
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

    private double calculateErrorPercentage() {
        return errorCount / requestVolume;
    }
}

