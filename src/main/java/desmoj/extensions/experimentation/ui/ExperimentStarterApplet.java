package desmoj.extensions.experimentation.ui;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import desmoj.extensions.experimentation.util.XMLFilter;

/**
 * <p>
 * Title: ExperimentStarterApplicationGUI
 * </p>
 * <p>
 * Description: GUI for the Experiment Starter when used as applet
 * </p>
 * based on the ExperimentStarterApplication by Nicolas Knaack.
 *
 * @author Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class ExperimentStarterApplet extends JApplet implements
    ExperimentStarterGUI {
    /* components of the GUI */
    ReportsPanel reportsPanel = new ReportsPanel();

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

    JButton loadButton = new JButton();

    JButton saveButton = new JButton();

    GridLayout gridLayout1 = new GridLayout();

    JPanel jPanel1 = new JPanel();

    JLabel timeLabel = new JLabel();

    GridLayout gridLayout2 = new GridLayout();

    JLabel runLabel = new JLabel();

    JProgressBar progressBar = new JProgressBar();

    BrowserPanel manualPanel = new BrowserPanel(false);

    /** The current Experiment Starter to use */
    private ExperimentStarter experimentStarter;

    /**
     * The model and experimentRunner classes must be provided by the param tags in the applet tag
     */
    private String modelClassName;

    private String expClassName;

    /** the ClassLoader to load external resources * */
    private final ClassLoader classLoader = this.getClass().getClassLoader();

    /***************************************************************************
     * creates a new AppletGUI for the DESMO-J
     * {@link ExperimentStarter ExperimentStarter}
     **************************************************************************/
    public ExperimentStarterApplet() {
    }

    /** Runs the GUI. */
    public static void main(String[] argv) {
        ExperimentStarterApplet gui = new ExperimentStarterApplet();
        gui.setVisible(true);
    }

    /* init the GUI */
    public void init() {

        this.observerDesktop = new ObserverDesktop();
        this.settingsPanel = new SettingsPanel();

        /** get output parameters from the Applet Tag * */
        modelClassName = getParameter("modelClass");
        expClassName = getParameter("expRunnerClass");
        String reportParameters = getParameter("reportParamFile");
        String traceParameters = getParameter("traceParamFile");
        String experimentSettings = getParameter("settingsFile");

        /** If parameter files have been given set these to be used. * */
        if (reportParameters != null) {
            reportsPanel.reportStylerPanel.changeReportParamXSLFile(classLoader
                .getResource(reportParameters));
        }
        if (traceParameters != null) {
            reportsPanel.reportStylerPanel.changeTraceParamXSLFile(classLoader
                .getResource(traceParameters));
        }

        /** load the manual files * */
        manualPanel.setPage(classLoader
            .getResource("desmoj/extensions/experimentation/ui/htmlFiles/manualApplet.html"));

        tabbedPane.add(settingsPanel, "Settings", 0);
        tabbedPane.add(observerDesktop, "Observers", 1);
        tabbedPane.add(reportsPanel, "Reports", 2);
        tabbedPane.add(manualPanel, "Manual", 3);

        /***********************************************************************
         * to prevent problems with the dot interpretation in path names
         * user.home gets the same value as user.dir
         **********************************************************************/
        String userDir = System.getProperty("user.dir");
        System.setProperty("user.home", userDir);

        try {
            Class modelClass = Class.forName(modelClassName);
            Class expClass = Class.forName(expClassName);
            experimentStarter = new ExperimentStarter(this, modelClass,
                expClass);
        } catch (Exception e) {
            System.out.println(modelClassName + " or " + expClassName
                + " could not be loaded.");
            System.out.println("check Param tags.");
        }
        /** Load a settings xml-file if one has been given in the applet tag* */
        if (experimentSettings != null) {
            experimentStarter.loadParameters(classLoader
                .getResource(experimentSettings));
        }

        experimentStarter.observerDesktop = observerDesktop;
        experimentStarter.resetModel();

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
        timeLabel.setText("SimulatedTime: 0.0");
        jPanel1.setLayout(gridLayout2);
        gridLayout2.setColumns(2);
        runLabel.setText("RunTime: 0.0");
        this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        this.getContentPane().add(runnerButtonPanel, BorderLayout.NORTH);
        this.getContentPane().add(progressPanel, BorderLayout.SOUTH);

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
        loadButton.setEnabled(true);
        saveButton.setEnabled(true);
        resetButton.setEnabled(false);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }

    /***************************************************************************
     * this method will be executed when the applet is shut down. It will delete
     * files that have been used during runtime.
     **************************************************************************/
    public void destroy() {
        reportsPanel.reportStylerPanel.deleteXSLFiles();
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

    /** Called by the ExperimentStarter when a new model is loaded */
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