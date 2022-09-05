package desmoj.extensions.experimentation.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.util.ExperimentListener;
import desmoj.core.util.SimClockListener;
import desmoj.core.util.SimRunEvent;
import desmoj.core.util.SimRunListener;
import desmoj.extensions.experimentation.util.AccessUtil;
import desmoj.extensions.experimentation.util.ExperimentRunner;
import desmoj.extensions.experimentation.util.FileUtil;
import desmoj.extensions.experimentation.util.Run;
import desmoj.extensions.xml.util.DocumentReader;
import desmoj.extensions.xml.util.XMLHelper;
import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple DESMO-J experimentation environment to use in connection with a GUI. These GUI's must implement the
 * Interface {@link ExperimentStarterGUI ExperimentStarterGUI}.
 *
 * @author Nicolas Knaak, Ruth Meyer, Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class ExperimentStarter
    // extends JFrame
    implements SimClockListener, ExperimentListener {
    /** Default window title */
    private final static String TITLE = "DESMOJ Experiment Starter";
    /** place to display graphical Observers */
    protected ObserverDesktop observerDesktop;
    /** current experiment runner class */
    Class expRunnerClass;

    /** current model class */
    Class modelClass;
    /** Node of current document */
    Node docNode;
    /** Indicates that a batch was run last */
    boolean runsBatch = false;
    /** The currently active model */
    private Model model;
    /** The current experiment runner */
    private ExperimentRunner expRunner;
    /** Model constants, e.g. passed from the command line */
    private String[] modelConstants;
    /** TableModel for model parameters */
    private AccessPointTableModel modelAccessPoints;
    /** Table model for experiment parameteres. */
    private AccessPointTableModel expAccessPoints;
    /** Start time for next experiment */
    private long startTime;
    /** Filename of current parameter set */
    private String filename;
    /** Indicates change of the configuration file */
    private boolean filenameChanged = false;
    /** the GUI to be used */
    private ExperimentStarterGUI experimentGUI;

    /**
     * Creates a new Launcher with the default title
     *
     * @param gui ExperimentStarterGUI: the GUI to be used with the ExperimentStarter
     */
    public ExperimentStarter(ExperimentStarterGUI gui) {
        this(gui, null, null, null);
    }

    /**
     * Creates a new Launcher and sets the current ModelFactory
     *
     * @param gui            ExperimentStarterGUI: the GUI to be used with the ExperimentStarter
     * @param modelClass     Class: The Class the contains the Model to be run with the ExperimentStarter. Must be a
     *                       subclass of {@link Model Model}
     * @param expRunnerClass Class: Must refer to the RunnerClass that belongs to modelClass
     */
    public ExperimentStarter(ExperimentStarterGUI gui, Class modelClass,
                             Class expRunnerClass) {
        this(gui, modelClass, expRunnerClass, null);
    }

    /**
     * Creates a new Launcher and sets the current ModelFactory such a command line parameters are passed to the model.
     *
     * @param gui            ExperimentStarterGUI: the GUI to be used with the ExperimentStarter
     * @param modelClass     Class: The Class the contains the Model to be run with the ExperimentStarter. Must be a
     *                       subclass of {@link Model Model}
     * @param expRunnerClass Class: Must refer to the RunnerClass that belongs to modelClass
     * @param args           String[]: Command line parameters to pass to the model; will be made available to the model
     *                       via its parameter manager as <code>cmdparam</code>
     */
    public ExperimentStarter(ExperimentStarterGUI gui, Class modelClass,
                             Class expRunnerClass, String[] args) {
        // Modified by Nick Knaak (3.11.2004)
        // Reason: NullPointerException when starting ExperimentStarter without
        // passing model class
        if (modelClass != null && expRunnerClass != null) {
            experimentGUI = gui;
            if (!Model.class.isAssignableFrom(modelClass)) {
                throw new RuntimeException(
                    "Model class passed to launcher is no subclass of desmoj.core.simulator.Model");
            } else if (!ExperimentRunner.class.isAssignableFrom(expRunnerClass)) {
                throw new RuntimeException(
                    "Experiment runner class passed to launcher is no subclass of desmoj.util.ExperimentRunner");
            } else {
                this.modelClass = modelClass;
                this.expRunnerClass = expRunnerClass;
                // resetModel();
            }
        }
        this.modelConstants = args;
    }

    /**
     * Creates Launcher with an experiment- or batchfile
     *
     * @param gui          ExperimentStarterGUI: the GUI to be used with the ExperimentStarter
     * @param confFileName Sring: must refer to an XML-File that contains the information for the batch run.
     */
    public ExperimentStarter(ExperimentStarterGUI gui, String confFileName) {
        this.filename = confFileName;
        experimentGUI = gui;
        loadDocNode();
        // resetModel();
    }

    /**
     * Called when the currently active experiment's SimClock is advanced
     *
     * @param e SimRunEvent: A SimRunEvent.
     */
    public void clockAdvanced(SimRunEvent e) {
        TimeInstant currentTime = e.getCurrentTime();
        double timePercent = (currentTime.getTimeAsDouble() / expRunner
            .getExperiment().getStopTime().getTimeAsDouble()) * 100;
        experimentGUI.clockAdvanced(currentTime.toString(3), timePercent,
            startTime);
    }

    /**
     * Creates a new experiment by loading a model and an experiment runner. Called when NEW button is pressed.
     */
    void createNewExperiment() {
        experimentGUI.loadModel();
        if (model != null) {
            if (expRunnerClass == null) {
                expRunnerClass = ExperimentRunner.class;
            }
            filename = null;
            docNode = null;
            resetModel();
        }
    }

    /**
     * Inits a new experiment run from a given model and experiment runner class: model and experiment runner are
     * initialized. Parameter tables are connected to model parameter and experiment setting access points.
     */
    void initNewExperimentRun() {

        try {
            if (expRunnerClass != null && modelClass != null) {

                model = (Model) modelClass.newInstance();
                if (this.modelConstants != null) {
                    model.getParameterManager()
                        .initializeModelParameter(String[].class, "cmdparam", this.modelConstants);
                }
                expRunner = (ExperimentRunner) expRunnerClass.newInstance();
                expRunner.setModel(model);

                expAccessPoints = new AccessPointTableModel(expRunner
                    .getExperimentSettings());
                modelAccessPoints = new AccessPointTableModel(expRunner
                    .getModelParameters());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the currently active model by creating a new model instance of the current model class. Inits all model-
     * and experiment parameters according to the current experiment runner.
     */
    void resetModel() {
        try {
            if (docNode != null) {
                if (docNode.getNodeName().equals("run")) {
                    readExperimentRunFromNode(docNode);
                }
            } else {
                initNewExperimentRun();
            }
            experimentGUI.resetGUI(filename, runsBatch, modelAccessPoints, expAccessPoints,
                model.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Loads a parameter set from an XML file opened by the user */
    void loadParameters() {
        String newFilename = experimentGUI.loadDialog(filename);
        if (newFilename != null && !newFilename.equals(filename)) {
            filename = newFilename;
            filenameChanged = true;
            loadDocNode();
        }
    }

    /** Loads a parameter set from a URL-definied XML file */
    void loadParameters(URL url) {
        String expSet = "experimentSettings.xml";
        FileUtil.copy(url, expSet);
        if (!filename.equals(expSet)) {
            filename = expSet;
            filenameChanged = true;
            loadDocNode();
        }
    }

    /**
     * Loads the DOM node representing the document currently open in the experiment launcher
     */
    void loadDocNode() {
        try {
            Document d = DocumentReader.getInstance().readDoc(filename);
            docNode = d.getDocumentElement();
            if (docNode.getNodeName().equals("run")) {
                runsBatch = false;
                readExperimentRunFromNode(docNode);
            } else if (docNode.getNodeName().equals("batch")) {
                runsBatch = true;
            }
            experimentGUI.resetGUI(filename, runsBatch, modelAccessPoints, expAccessPoints,
                model.getName());
        } catch (Exception e) {
            e.printStackTrace();
            filename = null;
            docNode = null;
            runsBatch = false;
        }
    }

    /**
     * Saves the current Parameters into an XML File. To do so a DOM-Tree with the current Parameters is constructed.
     * This tree will be serialized by use of XML_Util.serializeDocument and saved into the file selected via
     * experimentGUI.saveDialog.
     *
     * @author Gunnar Kiesel
     */
    void saveParameters() {
        // select file to save parameters in
        String newFilename = experimentGUI.saveDialog(filename);
        if (newFilename == null) {
            return;
        }
        filenameChanged = true;
        filename = newFilename;
        File file = new File(filename);

        // create DOM-tree
        Document document = XMLHelper.createDocument();
        Element run = document.createElement("run");
        run.setAttribute("model", modelClass.getName());
        run.setAttribute("expRunner", expRunnerClass.getName());
        document.appendChild(run);
        Element exp = document.createElement("exp");
        run.appendChild(exp);
        expAccessPoints.setValues();
        Map expMap = expAccessPoints.getAccessPoints();
        String[] expnames = AccessUtil.getAccessPointNames(expMap);
        Object[] expvalues = AccessUtil.getAccessPointValues(expMap);
        for (int i = 0; i < expnames.length; i++) {
            XMLHelper.addElement(document, exp, "param", "name", expnames[i],
                "value", expvalues[i].toString());
        }
        Element modelelement = document.createElement("model");
        run.appendChild(modelelement);
        modelAccessPoints.setValues();
        Map modelMap = modelAccessPoints.getAccessPoints();
        String[] modelnames = AccessUtil.getAccessPointNames(modelMap);
        Object[] modelvalues = AccessUtil.getAccessPointValues(modelMap);
        for (int i = 0; i < modelnames.length; i++) {
            XMLHelper.addElement(document, modelelement, "param", "name",
                modelnames[i], "value", modelvalues[i].toString());
        }
        // save DOM-tree to XML file
        try {
            XMLHelper.serializeDocument(document, new FileWriter(file));
            filename = file.getAbsolutePath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** Initializes an experiment run from an xml file */
    void readExperimentRunFromNode(Node root) {
        try {
            Run run = new Run();
            run.readFromNode((Element) root);

            expRunner = run.getExperimentRunner();
            model = run.getModel();
            expRunnerClass = expRunner.getClass();
            modelClass = model.getClass();
            expAccessPoints = new AccessPointTableModel(expRunner
                .getExperimentSettings());
            modelAccessPoints = new AccessPointTableModel(expRunner.getModelParameters());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Starts the current experiment for the first time */
    void startExperiment() {
        expAccessPoints.setValues();
        modelAccessPoints.setValues();

        expRunner.addSimClockListener(this);
        expRunner.addExperimentListener(this);
        expRunner.init();
        SimRunListener[] l = expRunner.createSimRunListeners(observerDesktop);
        setListeners(l);
        expRunner.registerMessageReceivers();
        startTime = System.currentTimeMillis();
        expRunner.start();
    }

    /**
     * Runs batch specified in the given DOM node
     *
     * @param a DOM node representing an xml batch file
     */
    void runBatch(final Node batch) {

        final Map defaultExpSettings = new HashMap();
        final Map defaultModelParams = new HashMap();

        Node settings = DOMUtil.getFirstChildElement(batch, "exp");
        Node params = DOMUtil.getFirstChildElement(batch, "model");

        if (settings != null) {
            Run.readParamList(settings, defaultExpSettings);
        }
        if (params != null) {
            Run.readParamList(params, defaultModelParams);
        }

        // Getting default model factory from node <modelfactory class=.../>
        String defaultModelN = DOMUtil.getAttrValue((Element) batch, "model");
        if (defaultModelN.equals("")) {
            defaultModelN = null;
        }
        String defaultExpRunnerN = DOMUtil.getAttrValue((Element) batch,
            "expRunner");
        if (defaultExpRunnerN == null || defaultExpRunnerN.equals("")) {
            defaultExpRunnerN = "desmoj.extensions.experimentation.util.ExperimentRunner";
        }
        final String defaultModelName = defaultModelN;
        final String defaultExpRunnerName = defaultExpRunnerN;

        // Create the default model

        System.out.println("* Processing batch...\n");

        Runnable r = new Runnable() {
            public void run() {

                // Read list of runs
                int count = 0;
                NodeList runs = batch.getChildNodes();
                for (int i = 0; i < runs.getLength(); i++) {
                    HashMap expSettings = new HashMap(defaultExpSettings);
                    HashMap modelParams = new HashMap(defaultModelParams);
                    Node nextDesc = runs.item(i);
                    if (nextDesc.getNodeName().equals("run")) {
                        count++;
                        System.out.println("* Initializing run no " + count);

                        try {

                            if (defaultModelName != null) {
                                modelClass = Class.forName(defaultModelName);
                            }
                            if (defaultExpRunnerName != null) {
                                expRunnerClass = Class
                                    .forName(defaultExpRunnerName);
                            }
                            model = (Model) modelClass.newInstance();
                            expRunner = (ExperimentRunner) expRunnerClass
                                .newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Run run = new Run(model, expRunner, expSettings,
                            modelParams, count);
                        run.readFromNode((Element) nextDesc);

                        expRunner = run.getExperimentRunner();
                        model = run.getModel();
                        expRunnerClass = expRunner.getClass();
                        modelClass = model.getClass();
                        expAccessPoints = new AccessPointTableModel(expRunner
                            .getExperimentSettings());
                        modelAccessPoints = new AccessPointTableModel(expRunner
                            .getModelParameters());
                        startExperiment();
                        try {
                            expRunner.getThread().join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        if (i < runs.getLength() - 1) {
                            experimentGUI.resetGUI(filename, runsBatch,
                                modelAccessPoints, expAccessPoints, model.getName());
                        }
                    }
                }
                System.out.println("* Batch completed.");
            }
        };
        new Thread(r).start();
    }

    /** Registers SimRunListeners with the current experiment runner */
    protected void setListeners(SimRunListener[] listeners) {
        if (listeners == null) {
            return;
        }
        for (int i = 0; i < listeners.length; i++) {
            SimRunListener l = listeners[i];
            expRunner.addSimRunListener(l);
        }
    }

    /**
     * Stops the current experiment (forever). Called when STOP button is pressed
     */
    void stopExperiment() {
        expRunner.stopExperiment();
    }

    /**
     * Pauses and resumes the currentExperiment (called when PAUSE button is switched).
     */
    void pauseExperiment(boolean isSelected) {
        expRunner.setPaused(isSelected);
    }

    /**
     * Called when experiment is started or resumed. Implemented for ExperimentListener
     */
    public void experimentRunning(SimRunEvent e) {
        experimentGUI.setRunning();
    }

    /**
     * Called when experiment is stopped. Implemented for ExperimentListener
     */
    public void experimentStopped(SimRunEvent e) {
        Experiment exp = expRunner.getExperiment();
        Map expMap = expAccessPoints.getAccessPoints();
        Object[] expvalues = AccessUtil.getAccessPointValues(expMap);
        String currentTime = model.presentTime().toString(3);
        String experimentValues = expvalues[AccessUtil.getIndexof("name", expMap)]
            .toString();
        String outputPath = exp.getOutputPath();
        List<List<String>> appendixes = exp.getOutputAppendixes();
        String[] appendixesUsed = {appendixes.get(0).get(0),
            appendixes.get(1).get(0),
            appendixes.get(2).get(0),
            appendixes.get(3).get(0)};  // only use the first one (GUI can only set one output file type per channel)
        experimentGUI.setStopped(currentTime, startTime, experimentValues,
            outputPath, appendixesUsed);
    }

    /** Called when experiment is paused. Implemented for ExperimentListener */
    public void experimentPaused(SimRunEvent e) {
        experimentGUI.setPaused();
        // this.settingsPanel.modelTable.setEnabled(true);
    }

    /** Creates an URL from the given Experiment's output path */
    protected URL getReportURL(Experiment exp) {
        try {
            String prefix = exp.getOutputPath();
            if (!prefix.equals("")) {
                prefix += "/";
            }
            return new URL("file:" + prefix + exp.getName());
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }
}