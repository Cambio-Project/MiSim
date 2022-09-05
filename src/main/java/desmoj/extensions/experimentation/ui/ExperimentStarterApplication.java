package desmoj.extensions.experimentation.ui;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import desmoj.core.simulator.Experiment;
import desmoj.extensions.experimentation.util.XMLFilter;

/**
 * GUI for the Experiment Starter when used as application.
 *
 * @author Nicolas Knaak, Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class ExperimentStarterApplication extends JFrame implements
    ExperimentStarterGUI {
    /** Default window title */
    private final static String TITLE = "DESMO-J " + Experiment.getDesmoJVersion() + " Experiment Starter";
    /* components of the GUI */
    JTabbedPane tabbedPane = new JTabbedPane();
    ObserverDesktop observerDesktop;
    BorderLayout borderLayout1 = new BorderLayout();
    SettingsPanel settingsPanel;
    JPanel runnerPanel = new JPanel();
    JButton startButton = new JButton();
    JButton stopButton = new JButton();
    JToggleButton pauseButton = new JToggleButton();
    JPanel runnerButtonPanel = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    JPanel progressPanel = new JPanel();
    JButton resetButton = new JButton();
    JButton newButton = new JButton();
    JButton loadButton = new JButton();
    JButton saveButton = new JButton();
    GridLayout gridLayout1 = new GridLayout();
    JPanel jPanel1 = new JPanel();
    JLabel timeLabel = new JLabel();
    GridLayout gridLayout2 = new GridLayout();
    JLabel runLabel = new JLabel();
    JProgressBar progressBar = new JProgressBar();
    ReportsPanel reportsPanel = new ReportsPanel();
    BrowserPanel manualPanel = new BrowserPanel(false);
    /** The current Experiment Starter to use */
    private ExperimentStarter experimentStarter;

    /**
     * Creates a new experiment starter application without loading a model
     */
    public ExperimentStarterApplication() {
        this(null, null);
    }

    /**
     * Creates a new experiment starter application. The experiment starter dynamically instantiates a model and an
     * experiment runner from the specified classes.
     *
     * @param modelClass     the model class to be loaded
     * @param expRunnerClass the experiment runner class to be loaded.
     */
    public ExperimentStarterApplication(Class modelClass, Class expRunnerClass) {
        this(modelClass, expRunnerClass, null);
    }

    /**
     * Creates a new experiment starter application with command line parameters passed to the model. The experiment
     * starter dynamically instantiates a model and an experiment runner from the specified classes.
     *
     * @param modelClass     the model class to be loaded
     * @param expRunnerClass the experiment runner class to be loaded.
     * @param args           command line parameters to pass to the model
     */
    public ExperimentStarterApplication(Class modelClass, Class expRunnerClass, String[] args) {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        experimentStarter = new ExperimentStarter(this, modelClass,
            expRunnerClass, args);
        experimentStarter.observerDesktop = observerDesktop;
        experimentStarter.resetModel();
    }

    /**
     * Creates a new experiment starter application. On initialization the experiment starter loads the specified
     * experiment configuartion file.
     *
     * @param confFileName Names of the (XML-)configuration file to be loaded.
     */
    public ExperimentStarterApplication(String confFileName) {
        try {
            jbInit();
            experimentStarter = new ExperimentStarter(this, confFileName);
            experimentStarter.observerDesktop = observerDesktop;
            experimentStarter.resetModel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Runs the GUI. */
    public static void main(String[] argv) {
        ExperimentStarterApplication gui = new ExperimentStarterApplication();
        gui.setVisible(true);
    }

    /** init the GUI */
    public void jbInit() throws Exception {

        this.setTitle(TITLE);
        this.observerDesktop = new ObserverDesktop();
        this.settingsPanel = new SettingsPanel();
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                reportsPanel.reportStylerPanel.deleteXSLFiles();
                System.exit(0);
            }
        });

        this.getContentPane().setLayout(borderLayout1);
        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (experimentStarter.runsBatch) {
                    experimentStarter.runBatch(experimentStarter.docNode);
                } else {
                    experimentStarter.startExperiment();
                }
            }
        });

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.loadParameters();
            }
        });

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.saveParameters();
            }
        });

        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.pauseExperiment(pauseButton.isSelected());
            }
        });
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.stopExperiment();
            }
        });
        stopButton.setText("Stop");
        pauseButton.setText("Pause");
        flowLayout1.setAlignment(FlowLayout.LEFT);
        runnerButtonPanel.setLayout(flowLayout1);
        progressPanel.setLayout(gridLayout1);
        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.resetModel();
            }
        });
        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                experimentStarter.createNewExperiment();
            }
        });

        timeLabel.setText("SimulatedTime: 0.0");
        jPanel1.setLayout(gridLayout2);
        gridLayout2.setColumns(2);
        runLabel.setText("RunTime: 0.0");
        this.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        ClassLoader classLoader = this.getClass().getClassLoader();
        manualPanel.setPage(classLoader
            .getResource("desmoj/extensions/experimentation/ui/htmlFiles/manualApplication.html"));

        tabbedPane.add(settingsPanel, "Settings");
        tabbedPane.add(observerDesktop, "Observers");
        tabbedPane.add(reportsPanel, "Reports");
        tabbedPane.add(manualPanel, "Manual");

        this.getContentPane().add(runnerButtonPanel, BorderLayout.NORTH);
        this.getContentPane().add(progressPanel, BorderLayout.SOUTH);

        runnerButtonPanel.add(newButton, null);
        runnerButtonPanel.add(loadButton, null);
        runnerButtonPanel.add(saveButton, null);
        runnerButtonPanel.add(resetButton, null);
        runnerButtonPanel.add(startButton, null);
        runnerButtonPanel.add(stopButton, null);
        runnerButtonPanel.add(pauseButton, null);
        progressPanel.add(jPanel1, null);
        jPanel1.add(timeLabel, null);
        jPanel1.add(runLabel, null);
        progressPanel.add(progressBar, null);

        this.setSize(800, 600);

        // Init button status
        newButton.setEnabled(true);
        loadButton.setEnabled(false);
        saveButton.setEnabled(true);
        resetButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }

    /**
     * Called by ExperimentStarter when the currently active experiment's SimClock is advanced
     *
     * @param currentTime String: the current simulation time
     * @param timePercent double: percentage of the simulation done
     * @param startTime   long: the start time of the simulation
     */
    public void clockAdvanced(String currentTime, double timePercent,
                              long startTime) {
        progressBar.setValue((int) timePercent);
        timeLabel.setText("SimulatedTime: " + currentTime);
        runLabel.setText("RunTime: " + (System.currentTimeMillis() - startTime)
            / 1000.0);
    }

    /** Called by the ExperimentStarter when a new model is loades */
    public void loadModel() {
        LoadModelDialog lmd = new LoadModelDialog();
        lmd.setVisible(true);
        experimentStarter.modelClass = lmd.getModelClass();
        experimentStarter.expRunnerClass = lmd.getExpRunnerClass();
    }

    /** opens a Swing dialog to select a configuration file to load from * */
    public String loadDialog() {
        return loadDialog(null);
    }

    /***************************************************************************
     * opens a Swing dialog to select a configuration file to load from
     *
     * @param currentFilename
     *            String: the filename of the configuration in use
     **************************************************************************/
    public String loadDialog(String currentFilename) {
        String filename = currentFilename;

        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new XMLFilter());
        jfc.setSize(500, 400);
        if (currentFilename != null) {
            jfc.setSelectedFile(new File(currentFilename));
        }
        int result = jfc.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return currentFilename;
        }
        File file = jfc.getSelectedFile();
        if (file != null) {
            filename = file.getAbsolutePath();
        }
        return filename;
    }

    /** opens a Swing dialog to select a configuration file to save in * */
    public String saveDialog() {
        return saveDialog(null);
    }

    /***************************************************************************
     * opens a Swing dialog to select a configuration file to save in
     *
     * @param currentFilename
     *            String: the filename of the configuration in use
     **************************************************************************/
    public String saveDialog(String currentFilename) {
        String filename = null;
        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new XMLFilter());
        jfc.setSize(500, 400);
        if (currentFilename != null) {
            jfc.setSelectedFile(new File(currentFilename));
        }
        int result = jfc.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            filename = null;
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            if (file != null) {
                filename = file.getAbsolutePath();
            }
        }
        return filename;
    }

    /**
     * Resets the user interface on creating, loading or resetting an experiment.
     *
     * @param filename          String: the name of the configuration file in use
     * @param runsBatch         boolean: <code>true</code> if a batch run is to be made,
     *                          <code>false</code> for single experiment runs
     * @param modelAccessPoints AccessPointTableModel: the access points for the model settings
     * @param expAccessPoints   AccessPointTableModel: the access points for the experiment settings
     * @param modelName         Sting: the name of the model in use
     */
    public void resetGUI(String filename, boolean runsBatch,
                         AccessPointTableModel modelAccessPoints, AccessPointTableModel expAccessPoints,
                         String modelName) {
        loadButton.setEnabled(true);
        startButton.setEnabled(true);
        newButton.setEnabled(true);

        if (!runsBatch) {
            saveButton.setEnabled(true);
            resetButton.setEnabled(true);
            this.settingsPanel.modelTable.setEnabled(true);
            this.settingsPanel.expTable.setEnabled(true);
            tabbedPane.setComponentAt(0, settingsPanel);
            settingsPanel.modelTable.setModel(modelAccessPoints);
            settingsPanel.expTable.setModel(expAccessPoints);
        } else {
            saveButton.setEnabled(false);
            resetButton.setEnabled(false);
            tabbedPane.setComponentAt(0, new JLabel("Batch run from file "
                + filename));
        }

        tabbedPane.repaint();

        progressBar.setValue(0);
        timeLabel.setText("SimulatedTime: 0.0");
        runLabel.setText("RunTime: 0.0");
        String text = "";
        if (filename != null) {
            text += " - " + filename;
            if (runsBatch) {
                text += " (Batch)";
            }
        } else if (modelName != null) {
            text += " - " + modelName;
        }
        this.setTitle(TITLE + text);

        // Clean observer desktop
        IGraphicalObserver[] observers = observerDesktop.getChildren();
        for (int i = 0; i < observers.length; i++) {
            observers[i].deregister();
        }
        observerDesktop.resetOffset();
    }

    /**
     * Called when experiment is started or resumed. Implemented for ExperimentListener
     */
    public void setRunning() {
        startButton.setEnabled(false);
        newButton.setEnabled(false);
        resetButton.setEnabled(false);
        stopButton.setEnabled(true);
        pauseButton.setEnabled(true);
        pauseButton.setSelected(false);
        this.settingsPanel.expTable.setEnabled(false);
        this.settingsPanel.modelTable.setEnabled(false);
    }

    /** Called when experiment is paused. Implemented for ExperimentListener */
    public void setPaused() {
        this.settingsPanel.modelTable.setEnabled(true);
    }

    /**
     * Called when experiment is stopped. Implemented for ExperimentListener
     *
     * @param currentTime      String: the current simulation time
     * @param startTime        long: the starting time of the experiment run
     * @param experimentValues String: the current values of the experiment access points
     * @param outputPath       String: the path the experiment output is written to
     * @param appendixes       String[]: the file endings of the four output files (.html, .txt, .xml)
     */
    public void setStopped(String currentTime, long startTime,
                           String experimentValues, String outputPath, String[] appendixes) {
        newButton.setEnabled(true);
        resetButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        pauseButton.setSelected(false);
        progressBar.setValue(100);
        timeLabel.setText("SimulatedTime: " + currentTime);
        runLabel.setText("RunTime: " + (System.currentTimeMillis() - startTime)
            / 1000.0);
        reportsPanel.urlTreePanel.createNode(experimentValues, "file:"
            + outputPath, appendixes);
    }

}