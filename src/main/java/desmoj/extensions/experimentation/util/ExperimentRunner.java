package desmoj.extensions.experimentation.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import desmoj.core.report.Reporter;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.util.AccessPoint;
import desmoj.core.util.ExperimentListener;
import desmoj.core.util.ExperimentParameter;
import desmoj.core.util.Parameterizable;
import desmoj.core.util.SimClockListener;
import desmoj.core.util.SimRunEvent;
import desmoj.core.util.SimRunListener;
import desmoj.extensions.experimentation.ui.GraphicalObserverContext;

/**
 * A thread a DesmoJ-Experiment can run in. The experiment runner can notifiy registered ExperimentListeners when the
 * assigned experiment is (re)started, stopped or temporarily paused and registered SimClockListeners when the
 * simulation clock is set forth.
 *
 * @author Nicolas Knaak
 * @author edited 6.1.2004 by Gunnar Kiesel (seperate output classes for all output Types)
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */
public class ExperimentRunner implements Observer, Runnable, Parameterizable {

    /** Status: model not connected to experiment */
    public static final int CREATED = 0;
    /** Status: Model created an connected but experiment not started yet */
    public static final int INITIALIZED = 1;
    /** Status: Experiment currently running */
    public static final int RUNNING = 2;
    /** Status: Experiment temporarily paused */
    public static final int PAUSED = 3;
    /** Status: Experiment finally stopped */
    public static final int STOPPED = 4;
    /** Experiment setting "name" */
    public final static String EXP_NAME = "name";
    /** Experiment setting "outputPath" */
    public final static String EXP_OUTPUT_PATH = "outputPath";
    /** Experiment setting "referenceTime" */
    public final static String EXP_EPSILON = "epsilon";
    /** Experiment setting "referenceTime" */
    public final static String EXP_REF_TIME = "referenceTime";
    /** Experiment setting "referenceUnit" */
    public final static String EXP_REF_UNIT = "referenceUnit";
    /** Experiment setting "startTime" */
    public final static String EXP_START_TIME = "startTime";
    /** Experiment setting "stopTime" */
    public final static String EXP_STOP_TIME = "stopTime";
    /** Experiment setting "showProgressBar" */
    public final static String EXP_SHOW_PROG_BAR = "showProgressBar";
    /** Experiment setting "traceStartTime" */
    public final static String EXP_TRACE_START = "traceStartTime";
    /** Experiment setting "traceStopTime" */
    public final static String EXP_TRACE_STOP = "traceStopTime";
    /** Experiment setting "randomizeConcurrentEvents" */
    public final static String EXP_RAND_EVENTS = "randomizeConcurrentEvents";
    /** Experiment setting "reportOutputType" */
    public final static String EXP_R_OUTTYPE = "reportOutputType";
    /** Experiment setting "traceOutputType" */
    public final static String EXP_T_OUTTYPE = "traceOutputType";

    /** Experiment setting "formatter" */
    // public final static String EXP_FORMATTER = "formatter";
    /** Experiment setting "errorOutputType" */
    public final static String EXP_E_OUTTYPE = "errorOutputType";
    /** Experiment setting "debugOutputType" */
    public final static String EXP_D_OUTTYPE = "debugOutputType";
    /** The start time for the experiment running in this thread */
    private TimeInstant startTime;
    /** The stop time specified in the experiment options */
    private TimeInstant stopTime;
    /** The experiment to be run */
    private Experiment experiment;
    /** Vector of listeners to the current experiment's running status changes */
    private Vector experimentListeners = null;
    /** Vector of listeners to the contained experiment's SimClock */
    private Vector simClockListeners = null;
    /** The contained experiment's status */
    private int status;
    /** Flag indicating if a report is to be drawn after the experiment stopped */
    private final boolean reportIsOn = true;
    /**
     * Millisecond part of delay between steps of the scheduler. Necessary for online observation of experiments .
     */
    private long delayMillis = 0;
    /**
     * Nanosecond part of delay between steps of the scheduler. Necessary for online observation of experiments.
     */
    private int delayNanos = 0;
    /**
     * This flag is set to true if the delay time between 2 steps of the scheduler is not 0.
     */
    private boolean hasDelay = false;
    /** A semaphore used to interactively suspend and resume this experiment */
    private final Lock lock = new Lock();
    /** The thread the experiment should run in */
    private final Thread myThread;
    /** Model parameters */
    private Map<String, AccessPoint> modelParams;
    /** Experiment parameter names and values */
    private Map<String, AccessPoint> expSettings;
    /** SimRunEvent sent to all listeners of current experiment */
    private SimRunEvent simRunEvent;
    /** An object providing the experiment runner's report. */
    private Reportable reportProvider;
    /** The model currently active */
    private Model model;

    /** Creates a new experiment runner for the given model */
    public ExperimentRunner(Model model) {
        this.model = model;
        this.myThread = new Thread(this, toString());
        this.startTime = null;
        this.stopTime = null;
        this.status = CREATED;

        if (model != null) {
            if (model instanceof Parameterizable) {
                modelParams = ((Parameterizable) model).createParameters();
            } else {
                modelParams = new TreeMap<String, AccessPoint>();
            }

            expSettings = createParameters();
        }
    }

    /**
     * Creates a new experiment runner that is not connected to a model yet. The model must be set consequently using
     * the <code>setModel()</code> method.
     */
    public ExperimentRunner() {
        this(null);
    }

    /** @return model running in this experiment runner */
    public Model getModel() {
        return model;
    }

    /**
     * Sets the current model
     *
     * @param model a desmoj.Model
     */
    public void setModel(Model model) {
        this.model = model;
        if (model instanceof Parameterizable) {
            modelParams = ((Parameterizable) model).createParameters();
        } else {
            modelParams = new TreeMap<String, AccessPoint>();
        }
        expSettings = createParameters();
    }

    /** Initializes the experiment before it is run in the thread */
    public void init() {
        reportProvider = new ReportProvider(this);
        experiment = createExperiment();
        experiment.getSimClock().addObserver(this);
        registerMessageReceivers();
        simRunEvent = new SimRunEvent(experiment);
        this.status = INITIALIZED;
    }

    /**
     * Inits the experiment runner from a map of model and experiment settings
     *
     * @param expSettingsMap map of experiment settings
     * @param modelParamMap  map of model parameters
     */
    public void initParameters(Map<String, AccessPoint> expSettingsMap, Map<String, AccessPoint> modelParamMap) {
        AccessUtil.init(modelParams, modelParamMap);
        AccessUtil.init(expSettings, expSettingsMap);
    }

    /** @return the experiment running in this experiment runner */
    public Experiment getExperiment() {
        return experiment;
    }

    /** @return the SimTime the current experiment starts at */
    public TimeInstant getStartTime() {
        return startTime;
    }

    /** @return the time the current experiment will finally stop */
    public TimeInstant getStopTime() {
        return stopTime;
    }

    /** Starts this thread and the contained experiment with start time 0.0 */
    public void start() {
        if (status <= INITIALIZED) {
            lock.acquire();
            if (status == CREATED) {
                System.out
                    .println("** WARNING: ExperimentRunner was not initialized before start. Performing init().");
                init();
            }
            myThread.start();
            lock.release();
        }
    }

    /** Stops the experiment running in this experiment runner (forever). */
    public void stopExperiment() {
        if (status == RUNNING || status == PAUSED) {
            experiment.stop();
            setStatus(STOPPED);
        }
    }

    /** Runs the contained experiment by calling it's appropriate start method */
    public void run() {
        setStatus(RUNNING);
        if (startTime != null) {
            experiment.start(startTime);
        } else {
            experiment.start();
        }
        while (status != STOPPED) {
            lock.acquire();
            lock.release();
            TimeInstant currentTime = experiment.getModel().presentTime();
            if (currentTime.getTimeInEpsilon() >= stopTime.getTimeInEpsilon()
                || experiment.isAborted()) {
                setStatus(STOPPED);
            } else {
                experiment.proceed();
            }
        }
        finishExperiment();
    }

    /**
     * Lets the experiment report (if desired) and closes all output streams by calling
     * <code>Experiment.finish()</code>
     */
    public void finishExperiment() {
        if (reportIsOn) {
            experiment.report();
        }
        experiment.finish();
    }

    /**
     * This method is implemented for interface observer. It is called by the assigned experiment's SimClock every time
     * it's value is increased. The methods tests and waits for the runners semaphore. If a delay time is set, the
     * runner sleeps for the given delay time afterwards.
     *
     * @param o     an observable object
     * @param value the updated value
     */
    public void update(Observable o, Object value) {
        if (simClockListeners != null) {
            for (Iterator i = simClockListeners.iterator(); i.hasNext(); ) {
                SimClockListener l = (SimClockListener) i.next();
                l.clockAdvanced(simRunEvent);
            }
        }
        if (hasDelay) {
            try {
                Thread.sleep(delayMillis, delayNanos);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * (Un)pauses running the assigned experiment by requiring and releasing the semaphore tested in the experiments
     * proceed loop.
     *
     * @param pause boolean value indicating pause (<code>true</code>) or restart (<code>false</code>).
     */
    public void setPaused(boolean pause) {
        if (status == RUNNING && pause) {
            lock.acquire();
            experiment.stop();
            setStatus(PAUSED);
        } else if (status == PAUSED && !pause) {
            setStatus(RUNNING);
            lock.release();
        }
    }

    /**
     * Returns the millisecond part of the delay between each step of the scheduler
     *
     * @return A long value representing the delay time in milliseconds
     */
    public long getDelayMillis() {
        return delayMillis;
    }

    /**
     * Returns the nanosecond part of the delay between each step of the scheduler
     *
     * @return A long value representing the delay time in milliseconds
     */
    public int getDelayNanos() {
        return delayNanos;
    }

    /**
     * Sets the delay between each step of the scheduler.
     *
     * @param delay: Delay time in milliseconds as a long value
     */
    public void setDelay(long millis, int nanos) {
        delayMillis = millis;
        delayNanos = nanos;
        hasDelay = millis + nanos != 0;
    }

    /** Adds a listener to the contained experiment's running status */
    public void addExperimentListener(ExperimentListener l) {
        if (experimentListeners == null) {
            experimentListeners = new Vector();
        }
        if (!experimentListeners.contains(l)) {
            experimentListeners.add(l);
        }
    }

    /** Removes an experiment listener */
    public void removeExperimentListener(ExperimentListener l) {
        if (experimentListeners != null) {
            experimentListeners.remove(l);
        }
    }

    /** Adds a listener to the contained experiment's sim clock */
    public void addSimClockListener(SimClockListener l) {
        if (simClockListeners == null) {
            simClockListeners = new Vector();
        }
        if (!simClockListeners.contains(l)) {
            simClockListeners.add(l);
        }
    }

    /** Removes a SimClock listener */
    public void removeSimClockListener(SimClockListener l) {
        if (simClockListeners != null) {
            simClockListeners.remove(l);
        }
    }

    /**
     * adds a new SimRunListener. Reacts only on SimClockListener and ExperimentListener objects. This method is called
     * from ExperimentRunner.setListeners().
     *
     * @author Ruth Meyer
     */
    public void addSimRunListener(SimRunListener l) {
        // can only handle SimClock- and ExperimentListener
        if (l instanceof SimClockListener) {
            addSimClockListener((SimClockListener) l);
        }
        if (l instanceof ExperimentListener) {
            addExperimentListener((ExperimentListener) l);
        }
    }

    /** Sets the experiment's status and notifies registered ExperimentListeners */
    protected void setStatus(int status) {
        this.status = status;
        if (experimentListeners != null) {
            SimRunEvent e = new SimRunEvent(this.getExperiment());
            for (Iterator i = experimentListeners.iterator(); i.hasNext(); ) {
                ExperimentListener l = (ExperimentListener) i.next();
                switch (this.status) {
                    case RUNNING:
                        l.experimentRunning(e);
                        break;
                    case STOPPED:
                        l.experimentStopped(e);
                        break;
                    case PAUSED:
                        l.experimentPaused(e);
                        break;
                }
            }
        }
    }

    /**
     * Returns an array of model parameters for this experiment run The first row contains names, the second row
     * contains values.
     */
    public Object[][] getModelParameterArray() {
        Object[][] mp = new Object[2][modelParams.size()];
        if (modelParams != null) {
            mp[0] = AccessUtil.getAccessPointNames(modelParams);
            mp[1] = AccessUtil.getAccessPointValues(modelParams);
        }
        return mp;
    }

    /**
     * Returns an array of experiment settings for this experiment run The first row contains names, the second row
     * contains values.
     */
    public Object[][] getExperimentSettingsArray() {
        Object[][] ep = new Object[2][expSettings.size()];
        if (expSettings != null) {
            ep[0] = AccessUtil.getAccessPointNames(expSettings);
            ep[1] = AccessUtil.getAccessPointValues(expSettings);
        }
        return ep;
    }

    public Map<String, AccessPoint> getModelParameters() {
        return this.modelParams;
    }

    public Map<String, AccessPoint> getExperimentSettings() {
        return this.expSettings;
    }

    public Thread getThread() {
        return myThread;
    }

    /**
     * creates and initializes an experiment with the parameters in expParams. Connects the experiment to the model.
     */
    protected Experiment createExperiment() {
        Experiment e = null;
        //System.out.println("ExpSettings: " + expSettings);
        String name = AccessUtil.getStringValue(EXP_NAME, expSettings);
        String outputPath = AccessUtil.getValue(EXP_OUTPUT_PATH,
            expSettings).toString();
        String reportOutputType = AccessUtil.getStringValue(EXP_R_OUTTYPE,
            expSettings);
        String traceOutputType = AccessUtil.getStringValue(EXP_T_OUTTYPE,
            expSettings);
        String errorOutputType = AccessUtil.getStringValue(EXP_E_OUTTYPE,
            expSettings);
        String debugOutputType = AccessUtil.getStringValue(EXP_D_OUTTYPE,
            expSettings);

        TimeUnit eps = AccessUtil.getTimeUnitValue(EXP_EPSILON, expSettings);
        TimeUnit ref = AccessUtil.getTimeUnitValue(EXP_REF_UNIT, expSettings);
        Experiment.setEpsilon(eps);
        Experiment.setReferenceUnit(ref);

        e = new Experiment(name, outputPath,
            null,
            reportOutputType,
            traceOutputType, errorOutputType, debugOutputType);
        model.connectToExperiment(e);

        if (AccessUtil
            .getBooleanValue(EXP_RAND_EVENTS, expSettings)) {
            e.randomizeConcurrentEvents(true);
        }
        e.setShowProgressBar(AccessUtil.getBooleanValue(EXP_SHOW_PROG_BAR,
            expSettings));
        e.traceOn(new TimeInstant(AccessUtil.getDoubleValue(EXP_TRACE_START, expSettings)));
        e.traceOff(new TimeInstant(AccessUtil.getDoubleValue(EXP_TRACE_STOP, expSettings)));
        stopTime = new TimeInstant(AccessUtil.getDoubleValue(EXP_STOP_TIME, expSettings));
        startTime = new TimeInstant(AccessUtil.getDoubleValue(EXP_START_TIME, expSettings));
        e.stop(stopTime);
        return e;
    }

    /** @return a map of experiment parameters */
    public Map<String, AccessPoint> createParameters() {
        Map<String, AccessPoint> xp = new TreeMap<String, AccessPoint>(new ExperimentParameterComparator());
        // Default-Werte fuer die wichtigsten Experiment-Parameter erzeugen
        xp.put(EXP_NAME, new ExperimentParameter(EXP_NAME, model.getName()
            + "Experiment"));
        xp.put(EXP_OUTPUT_PATH, new ExperimentParameter(EXP_OUTPUT_PATH,
            new Filename("./", true)));
        //		xp.put(EXP_REF_TIME, new ExperimentParameter(EXP_REF_TIME,
        //				"1.1.1970 00:00:00"));
        xp.put(EXP_REF_UNIT, new ExperimentParameter(EXP_REF_UNIT,
            TimeUnit.SECONDS));
        xp.put(EXP_EPSILON, new ExperimentParameter(EXP_EPSILON,
            TimeUnit.MICROSECONDS));
        xp.put(EXP_START_TIME, new ExperimentParameter(EXP_START_TIME,
            new Double(0.0)));
        xp.put(EXP_STOP_TIME, new ExperimentParameter(EXP_STOP_TIME,
            new Double(0.0)));
        xp.put(EXP_SHOW_PROG_BAR, new ExperimentParameter(EXP_SHOW_PROG_BAR,
            Boolean.valueOf(false)));
        xp.put(EXP_TRACE_START, new ExperimentParameter(EXP_TRACE_START,
            new Double(0.0)));
        xp.put(EXP_TRACE_STOP, new ExperimentParameter(EXP_TRACE_STOP,
            new Double(0.0)));
        xp.put(EXP_RAND_EVENTS, new ExperimentParameter(EXP_RAND_EVENTS,
            Boolean.valueOf(false)));
        xp.put(EXP_R_OUTTYPE, new ExperimentParameter(EXP_R_OUTTYPE,
            Experiment.DEFAULT_REPORT_OUTPUT_TYPE));
        xp.put(EXP_T_OUTTYPE, new ExperimentParameter(EXP_T_OUTTYPE,
            Experiment.DEFAULT_TRACE_OUTPUT_TYPE));
        xp.put(EXP_E_OUTTYPE, new ExperimentParameter(EXP_E_OUTTYPE,
            Experiment.DEFAULT_ERROR_OUTPUT_TYPE));
        xp.put(EXP_D_OUTTYPE, new ExperimentParameter(EXP_D_OUTTYPE,
            Experiment.DEFAULT_DEBUG_OUTPUT_TYPE));
        return xp;
    }

    /** Registers new message receivers as output channels of the experiment */
    public void registerMessageReceivers() {
    }

    /**
     * Should return a new set of listeners to the experiment run represented by the given experiment runner in an
     * array. Returns an array of size 0 by default.
     */
    public SimRunListener[] createSimRunListeners(GraphicalObserverContext c) {
        return new SimRunListener[0];
    }

    private static class Lock {

        private boolean l;

        /** Creates a new Semaphore of size 1 */
        public Lock() {
            l = true;
        }

        /**
         * Tries to occupy the semaphore. If the semaphore is not available the calling thread has to wait in a lock.
         */
        public synchronized void acquire() {
            while (l == false) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            l = false;
        }

        /**
         * Releases the semaphore and notifies threads waiting in lock.
         */
        public synchronized void release() {
            l = true;
            // if (del > 0)
            notifyAll();
        }
    }

    /**
     * An implementation of Java.util.Comparator&lt;String&gt; for comparing experiment parameter names. The only
     * difference from the default String comparison is that output types are always considered larger than non-output
     * types so that they appear as group after all other parameters in the experiment parameter panel.
     */
    private static class ExperimentParameterComparator implements java.util.Comparator<String> {
        public int compare(String o1, String o2) {

            boolean o1IsOutType = o1.equals(EXP_R_OUTTYPE) || o1.equals(EXP_T_OUTTYPE)
                || o1.equals(EXP_D_OUTTYPE) || o1.equals(EXP_E_OUTTYPE);
            boolean o2IsOutType = o2.equals(EXP_R_OUTTYPE) || o2.equals(EXP_T_OUTTYPE)
                || o2.equals(EXP_D_OUTTYPE) || o2.equals(EXP_E_OUTTYPE);

            // exactly one of the Strings is an output type parameter:
            // consider the output type parameter larger
            if (o1IsOutType && !o2IsOutType) {
                return 1;
            }
            if (!o1IsOutType && o2IsOutType) {
                return -1;
            }

            // neither of them or both of them are output type parameter:
            // use default String comparison
            return o1.compareTo(o2);
        }
    }

    /**
     * An inner class providing the experiment runner's report. The report contains all model parameter and experiment
     * settings of the current experiment
     */
    public static class ReportProvider extends Reportable {

        private final ExperimentRunner runner;

        public ReportProvider(ExperimentRunner runner) {
            super(runner.model, "ExperimentRunner", true, false);
            this.runner = runner;
        }

        public Reporter createDefaultReporter() {
            return new ExperimentParameterReporter(this);
        }

        public ExperimentRunner getExperimentRunner() {
            return runner;
        }
    }
}