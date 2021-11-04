package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import desmoj.extensions.visualization2d.engine.Constants;
import desmoj.extensions.visualization2d.engine.command.CommandException;
import desmoj.extensions.visualization2d.engine.command.CommandSequence;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGrafic.ZoomEntry;
import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGraficException;

/**
 * Swing-Panel to animate a simulation. Will be called by ViewerFrame and ViewerApplet
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class ViewerPanel extends JPanel {
    public static final Font FONT_BIG = new Font("SansSerif", Font.PLAIN, 20);
    public static final Font FONT_MIDDLE = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font FONT_DEFAULT = new Font("SansSerif", Font.PLAIN, 10);
    public static final long REPAINT_DELAY = 100;
    public static final Color HEADER_BG_COLOR = new Color(220, 220, 220);
    public static final Color STATUS_BG_COLOR = new Color(220, 220, 220);
    public static final Color STATUS_MSG_COLOR = Color.red;
    public static final Color INFO_HEADER_FG_COLOR = Color.red;
    public static final Color INFO_HEADER_BG_COLOR = Color.yellow;
    public static final Color INFO_TEXT_FG_COLOR = Color.black;
    public static final Color INFO_TEXT_BG_COLOR = Color.white;
    public static final Color INFO_OLD_FG_COLOR = Color.gray;
    public static final int TimeFlowMode_CONTINIUM = 1;
    public static final int TimeFlowMode_STEP_FLOW = 2;
    public static final int TimeFlowMode_STEP_SINGLE = 3;
    private static final long serialVersionUID = 1L;
    //public String[]    TimeFlowMode_VALUES		= {"continues time flow", "step flow", "single steps"};
    private static ViewerPanel viewer = null;
    private static PrintWriter logWriter = null;
    protected String[] timeModeVal = null;
    //Eventhandler fuer speedField und speedSlider arbeiten nur, wenn speedHandler true
    protected boolean speedHandler = true;
    //Eventhandler fuer zoomField und zoomSlider arbeiten nur, wenn zoomHandler true
    protected boolean zoomHandler = true;
    private LanguageSupport language = null;
    private DesmoJ_Info desmoJinfo = null;
    private CommandSequence cmdSequence = null;
    private double simulationSpeed;
    private SimulationThread simulationThread = null;
    private SimulationTime simulationTime = null;
    private Model model = null;
    private BufferedReader dataReader = null;
    private URL dataFile = null;
    private URL viewerIconDir = null;
    private URL snapShotDir = null;
    private boolean isApplication = true;
    private AppletContext appletContext = null;
    private int timeFlowMode = 0;
    private String defaultCmdPath = null;
    private String defaultIconPath = null;
    private String defaultSnapShotPath = null;
    private JLabel nameLabel = null;
    private JLabel startLabel = null;
    private JLabel endLabel = null;
    private JLabel timeLabel = null;
    private JLabel zeitzoneLabel = null;
    private JLabel timeFlowLabel = null;
    private JTextField speedField = null;
    private JSlider speedSlider = null;
    private JTextField zoomField = null;
    private JSlider zoomSlider = null;
    private JButton startButton = null;
    private JButton stopButton = null;
    private JButton pauseButton = null;
    private JMenuItem startItem = null;
    private JMenuItem stopItem = null;
    private JMenuItem pauseItem = null;
    private JPanel header = null;
    private JPanel center = null;
    private JPanel status = null;
    private JLabel statusMessage = null;
    private JTextField coordinateXField = null;
    private JTextField coordinateYField = null;
    private InfoPane infoPane = null;
    private JMenuBar menuBar = null;
    private JMenuItem projectItem = null;
    private Vector<Component> menueBarIcons = null;


    /**
     * starts viewer application
     *
     * @param cmdFile               URL of cmds-file thats opens automaticly, null otherwise
     * @param simulationIconPathURL URL of simulation icon directory
     * @param appletContext         null, when this panel is part of a JFrame
     * @param locale                locale of this panel
     */
    public ViewerPanel(URL cmdFile, URL simulationIconDir, AppletContext appletContext, Locale locale) {
        this(cmdFile, simulationIconDir, null, appletContext, locale);
    }

    /**
     * starts viewer application
     *
     * @param cmdFile               URL of cmds-file thats opens automaticly, null otherwise
     * @param simulationIconPathURL URL of simulation icon directory
     * @param snapshot              URL of snapshot directory
     * @param appletContext         null, when this panel is part of a JFrame
     * @param locale                locale of this panel
     */
    public ViewerPanel(URL cmdFile, URL simulationIconDir, URL snapshotDir, AppletContext appletContext,
                       Locale locale) {

        ViewerPanel.viewer = this;
        this.addHierarchyBoundsListener(new ResizeListener());
        this.language = new LanguageSupport(locale);
        this.desmoJinfo = new DesmoJ_Info();
        this.timeModeVal = new String[3];
        this.timeModeVal[0] = this.language.getString("Mode_CONTINIUM");
        this.timeModeVal[1] = this.language.getString("Mode_STEP_FLOW");
        this.timeModeVal[2] = this.language.getString("Mode_STEP_SINGLE");
        this.dataFile = cmdFile;
        this.viewerIconDir = simulationIconDir;
        this.snapShotDir = snapShotDir;
        this.appletContext = appletContext;
        this.isApplication = (appletContext == null);
        this.timeFlowMode = ViewerPanel.TimeFlowMode_STEP_FLOW;
        CoordinatenListener listener = new CoordinatenListener(this);
        this.model = new Model(viewerIconDir, listener, this);
        this.simulationTime = new SimulationTime(0, 1, 1.0, null, this.language.getLocale());
        this.model.setSimulationTime(simulationTime);
        this.model.setValid(false);

        this.infoPane = new InfoPane(this);

        this.setLayout(new BorderLayout());
        this.header = this.createHeaderPanel();
        this.add(BorderLayout.NORTH, this.header);
        this.status = this.createStatusPanel();
        this.add(BorderLayout.SOUTH, this.status);
        this.center = this.createCenterPanel();
        this.add(BorderLayout.CENTER, this.center);

        this.setVisible(true);

        this.defaultCmdPath = null;
        this.defaultIconPath = null;
        this.defaultSnapShotPath = null;

        // init snapShot directory
        if (this.dataFile != null && this.isApplication && this.snapShotDir == null) {
            String path = this.viewerIconDir.getPath();
            int last = path.substring(0, path.length() - 1).lastIndexOf('/');
            File projectData = new File(path.substring(0, last));
            if (projectData.isDirectory() && projectData.canWrite()) {
                File snapShot = new File(projectData, "SnapShot");
                if (!snapShot.exists()) {
                    snapShot.mkdir();
                }
                try {
                    this.snapShotDir = snapShot.toURI().toURL();
                } catch (MalformedURLException e) {
                    System.out.println("SnapshotDir is invalid URL: " + snapShot.getName());
                    this.snapShotDir = null;
                }
            }
        }
    }

    /**
     * InfoPane is a window to show data of an entity
     *
     * @return InfoPane
     */
    public static InfoPane getInfoPane() {
        InfoPane out = null;
        if (viewer != null) {
            out = viewer.infoPane;
        }
        return out;
    }

    /**
     * get's the logWriter
     *
     * @return PrintWriter for logging
     */
    public static PrintWriter getLogWriter() {
        return ViewerPanel.logWriter;
    }

    /**
     * set default path for Data->open aund Data->icon menue. When this paths are null, the menue have no function used
     * only for aplication version (not for applets)
     *
     * @param defaultCmdPath      with this path starts Data->open menue
     * @param defaultIconPath     with this path starts Data->icon menue
     * @param defaultSnapShotPath with this path starts Data->snapshot menue
     */
    public void setDefaultPath(String defaultCmdPath, String defaultIconPath, String defaultSnapShotPath) {
        this.defaultCmdPath = defaultCmdPath;
        this.defaultIconPath = defaultIconPath;
        this.defaultSnapShotPath = defaultSnapShotPath;
    }

    /**
     * set default path for Data->open aund Data->icon menue. When this paths are null, the menue have no function used
     * only for aplication version (not for applets)
     *
     * @param defaultCmdPath  with this path starts Data->open menue
     * @param defaultIconPath with this path starts Data->icon menue and Data->snapshot menue
     */
    public void setDefaultPath(String defaultCmdPath, String defaultIconPath) {
        this.setDefaultPath(defaultCmdPath, defaultIconPath, defaultIconPath);
    }

    /**
     * opens cmdFile, must be called behind constructor.
     *
     * @throws IOException
     */
    protected void fileOpen() throws IOException {
        this.fileOpen(this.dataFile);
    }

    /**
     * opens a cmds-file, it's call fileReset()
     *
     * @param file cmds-file
     * @throws IOException
     */
    private void fileOpen(URL cmdFile) throws IOException {
        //System.out.println("FileOpen begin "+cmdFile);
        if (this.viewerIconDir != null) {
            if (cmdFile != null) {
                if (this.isApplication) {
                    if (cmdFile.getPath().endsWith(Constants.FILE_EXTENSION_CMD)) {
                        this.dataFile = cmdFile;
                        this.fileReset();
                        this.infoPane.reset();
                    } else {
                        this.setStatusMessage(this.language.getString("StatusMsg_09"));
                    }
                } else {
                    // Applets koennen den dataFile als byte[] bekommen und haben dann keine File_Extension
                    this.dataFile = cmdFile;
                    this.fileReset();
                    this.infoPane.reset();
                }
                this.lastCall();
            } else {
                this.setStatusMessage(this.language.getString("StatusMsg_07"));
            }
        } else {
            this.setStatusMessage(this.language.getString("StatusMsg_08"));
        }
        //System.out.println("FileOpen end ");
    }

    /**
     * this is fileReset doing: close simulationThread, close and reopen cmds-file, close and reopen log-file, read and
     * execute model-init-commands, show new model in gui, initialize simulation-time
     *
     * @throws IOException
     */
    private void fileReset() throws IOException {
        // SimulationsThread beenden
        if (this.simulationThread != null) {
            this.simulationThread.interrupt();
            try {
                this.simulationThread.join();
            } catch (InterruptedException e) {
            }
            this.simulationThread = null;
        }
        // Datafile schliessen und wieder oeffnen
        if (this.dataReader != null) {
            this.dataReader.close();
            this.dataReader = null;
        }
        this.dataReader = new BufferedReader(new InputStreamReader(this.dataFile.openStream()));

        if (ViewerPanel.logWriter != null) {
            ViewerPanel.logWriter.close();
            ViewerPanel.logWriter = null;
        }
        if (this.isApplication) {
            int index = this.dataFile.getPath().indexOf(Constants.FILE_EXTENSION_CMD);
            String logName = this.dataFile.getPath().substring(0, index) + Constants.FILE_EXTENSION_LOG;
            logName = java.net.URLDecoder.decode(logName, "UTF-8");
            ViewerPanel.logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logName)));
        }
        this.cmdSequence = new CommandSequence(this.model, logWriter);

        // Modell einlesen (Init)
        this.simulationTime = new SimulationTime(0, 1, 1.0, null, this.language.getLocale());
        this.model.setSimulationTime(simulationTime);
        this.model.reset();
        executeInitCommands();
        // nun werden noch die cmds aus doInitialSchedules() ausgefuehrt
        long firstStepTime = executeRunCommands(this.model.getSimulationBegin());
        this.model.createModelGrafic();
        this.model.setValid(true);
        this.desmoJinfo.update(model);
        this.updateMenuBar();

        // neues Modell in GUI anzeigen und Simulationsuhr initzialisieren
        this.remove(this.center);
        this.center = this.createCenterPanel();
        this.add(BorderLayout.CENTER, this.center);
        this.setModelName(this.model.getModelName());
        this.simulationTime = new SimulationTime(this.model.getSimulationBegin(),
            this.model.getSimulationEnd(), this.model.getSimulationSpeed(),
            this.model.getTimeZone(), this.language.getLocale());
        this.model.setSimulationTime(this.simulationTime);
        this.setSimulationSpeed(this.model.getSimulationSpeed());
        this.updateSimulationTimeBounds(true);
        this.coordinateXField.setText("");
        this.coordinateYField.setText("");

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);

        if (startItem != null) {
            startItem.setEnabled(true);
        }
        if (stopItem != null) {
            stopItem.setEnabled(false);
        }
        if (pauseItem != null) {
            pauseItem.setEnabled(false);
        }


        if (this.model.getModelGrafic().getBoundsExtern() != null) {
            // SimulationsThread starten
            //this.model.setRunPhase(true);
            this.setStatusMessage(this.language.getString("StatusMsg_00"));
            this.simulationThread = new SimulationThread(this, firstStepTime);
            this.simulationThread.start();
        }
        // animation initialization
        simulationTime.start();
        ViewerPanel.getInfoPane().refresh();
        ViewerPanel.getInfoPane().revalidate();
        ViewerPanel.getInfoPane().repaint();
        simulationTime.pause();

    }

    /**
     * close simulationThread, close data-file,, close log-file, reset model, remove model from gui
     *
     * @throws ModelGraficException
     * @throws IOException
     */
    protected void fileClose() throws ModelGraficException, IOException {
        // SimulationsThread beenden
        if (this.simulationThread != null) {
            this.simulationThread.interrupt();
            try {
                this.simulationThread.join();
            } catch (InterruptedException e) {
            }
            this.simulationThread = null;
        }
        // Datenfile schliessen
        if (this.dataReader != null) {
            this.dataReader.close();
            this.dataReader = null;
        }
        if (ViewerPanel.logWriter != null) {
            ViewerPanel.logWriter.close();
            ViewerPanel.logWriter = null;
        }


        // Modell reseten und ungueltig setzen
        this.model.reset();
        this.model.setValid(false);
        this.updateMenuBar();

        // altes Modell aus GUI entfernen
        this.infoPane.reset();
        this.remove(this.center);
        this.center = this.createCenterPanel();
        this.add(BorderLayout.CENTER, this.center);
        this.setModelName("");
        this.simulationTime = null;
        this.model.setSimulationTime(null);
        this.updateSimulationTimeBounds(false);
        this.coordinateXField.setText("");
        this.coordinateYField.setText("");
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        startItem.setEnabled(false);
        stopItem.setEnabled(false);
        pauseItem.setEnabled(false);
        this.setStatusMessage(this.language.getString("StatusMsg_06"));
    }

    /**
     * used when start button is pressed
     */
    protected void reload() {
        // alte zoom- und speed- Werte speichern
        Hashtable<String, ZoomEntry> zoom = model.getModelGrafic().getZoomProperty();
        double speed = getSimulationSpeed();
        // Animation neu laden
        try {
            fileReset();
        } catch (Exception e1) {
            setStatusMessage(e1.getMessage());
            e1.printStackTrace(ViewerPanel.getLogWriter());
            ViewerPanel.getLogWriter().close();
            e1.printStackTrace();
        }
        // alte zoom- und speed- Werte setzen
        setSimulationSpeed(speed);
        model.getModelGrafic().setZoomProperty(zoom);
        this.lastCall();
    }

    /**
     * sorgt dafuer, dass nach der ModelGrafic Neu- Initialisierung der ZoomCenterPoint in der Mitte des selektierten
     * View angezeigt wird. Analogon zum ChangeEventHandler beim Wechseln der ViewGrafic. Wird mit
     * SwingUtilities.invokeLater() als letzte Anweisung in der SwingEventQueue ausgefuehrt. Voraussetzungen fuer den
     * Aufruf: - Size des ViewerPanel muss feststehen (Nach dem ViewerFrame Konstruktor. Wird aufgerufen von: - nach der
     * ViewerFrame Konstruktor, damit die Anzeige nach dem Neustart stimmt - in fileOpen, damit die Anzeige nach dem
     * manuellen oeffnen stimmt - in reload, damit die Anzeige nach dem Betaetigen des Start-Buttons stimmt.
     */
    public void lastCall() {
        //System.out.println("lastCall begin");
        Runnable t1 = new Runnable() {
            public void run() {
                if (model.isValid()) {
                    double z = getSimulationZoom();
                    setSimulationZoom(z);
                    setSimulationZoomGUI(z);
                }
            }
        };
        SwingUtilities.invokeLater(t1);


    }

    /**
     * Execute commands from init-phase, used by fileReset()
     *
     * @return Time of first command in run-phase
     */
    protected long executeInitCommands() {
        long out = 0;
        String msg = this.language.getString("StatusMsg_14");
        try {
            if (this.dataReader != null) {
                this.setStatusMessage(this.language.getString("StatusMsg_01"));
                out = this.cmdSequence.readInit(this.dataReader);
                this.setStatusMessage(this.language.getString("StatusMsg_02"));
            }
        } catch (ModelGraficException e) {
            this.setStatusMessage(msg);
            e.printStackTrace();
        } catch (ModelException e) {
            this.setStatusMessage(msg);
            e.printStackTrace();
        } catch (CommandException e) {
            this.setStatusMessage(msg);
            e.printStackTrace();
        } catch (IOException e) {
            this.setStatusMessage(msg);
            e.printStackTrace();
        }
        return out;

    }

    /**
     * Aktualisiert die angezeigte Simulationszeit, used by SimulationThread Fuehrt Commands bis zur aktuellen
     * Simulationszeit aus (RunPhase)
     *
     * @return Time des naechsten Command
     */
    protected long executeRunCommands(long until) {
        long out = until;
        String msg = this.language.getString("StatusMsg_14");
        try {
            this.writeStatusMsg();
            this.updateSimulationTime(this.model.isValid());
            out = this.cmdSequence.readUntilTime(this.dataReader, until);
        } catch (ModelGraficException e) {
            e.printStackTrace();
            new Stop().actionPerformed(null);
            this.setStatusMessage(msg);
        } catch (ModelException e) {
            e.printStackTrace();
            new Stop().actionPerformed(null);
            this.setStatusMessage(msg);
        } catch (CommandException e) {
            e.printStackTrace();
            new Stop().actionPerformed(null);
            this.setStatusMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            new Stop().actionPerformed(null);
            this.setStatusMessage(msg);
        }
        return out;
    }

    protected void updateDynamic(long time) {
        if (this.model.getModelGrafic() != null) {
            this.model.getModelGrafic().updateDynamic(time);
        }
    }

    public LanguageSupport getLanguage() {
        return this.language;
    }

    protected DesmoJ_Info getDesmoJ_Info() {
        return this.desmoJinfo;
    }

    protected AppletContext getAppletContext() {
        return this.appletContext;
    }

    protected String getViewerName() {
        return this.language.getString("VISUALIZATION_Name");
    }

    protected void writeStatusMsg() {
        if (this.simulationTime.isRunning()) {
            this.setStatusMessage(this.language.getString("StatusMsg_03"));
        } else {
            this.setStatusMessage(this.language.getString("StatusMsg_04"));
        }
        if (this.simulationTime.isRunning() && this.simulationTime.isPause()) {
            this.setStatusMessage(this.language.getString("StatusMsg_05"));
        }
    }

    public Model getModel() {
        return this.model;
    }

    /**
     * set model-name and repaint it
     *
     * @param name
     */
    public void setModelName(String name) {
        nameLabel.setText(name);
        nameLabel.repaint(ViewerPanel.REPAINT_DELAY);
    }

    /**
     * set status-message and repaint it
     *
     * @param message
     */
    public void setStatusMessage(String message) {
        //System.out.println("setStatusMessage: "+message);
        if (this.statusMessage != null) {
            this.statusMessage.setText(message);
            this.statusMessage.repaint();
        }
    }

    /**
     * set coordinate point to show in status line
     *
     * @param p
     */
    public void setCoordinatePoint(Point p) {
        coordinateXField.setText(Integer.toString(p.x));
        coordinateYField.setText(Integer.toString(p.y));
    }

    /**
     * repaint SimulationTimeBounds
     *
     * @param valid when true use data from SimulationTime, else ""
     */
    public void updateSimulationTimeBounds(boolean valid) {
        String start = "", end = "", time = "";
        if (valid && this.simulationTime != null) {
            start =
                SimulationTime.getTimeString(this.simulationTime.getSimulationStart(), SimulationTime.SHOW_DATE_TIME);
            end = SimulationTime.getTimeString(this.simulationTime.getSimulationEnd(), SimulationTime.SHOW_DATE_TIME);
            time = SimulationTime.getTimeString(this.simulationTime.getSimulationTime(),
                SimulationTime.SHOW_DAY_DATE_TIME_MILLIS_DST);
        }
        if (this.timeLabel != null && this.startLabel != null &&
            this.endLabel != null && this.zeitzoneLabel != null) {
            this.startLabel.setText(start);
            this.startLabel.repaint();
            this.endLabel.setText(end);
            this.endLabel.repaint();
            this.timeLabel.setText(time);
            this.timeLabel.repaint();
            this.zeitzoneLabel.setText(SimulationTime.getTimeZoneString());
            this.zeitzoneLabel.repaint();
            //System.out.println(this.startLabel.getText()+"  "+this.endLabel.getText()+"  "+this.timeLabel.getText());
        }
    }

    /**
     * repaint SimulationTime
     *
     * @param valid when true use data from SimulationTime, else ""
     */
    public void updateSimulationTime(boolean valid) {
        String time = "";
        if (valid && this.simulationTime != null) {
            time = SimulationTime.getTimeString(this.getSimulationTime(), SimulationTime.SHOW_DAY_DATE_TIME_MILLIS_DST);
        }
        if (this.timeLabel != null) {
            this.timeLabel.setText(time);
            this.timeLabel.repaint();
            this.simulationTime.isRunning();
            //System.out.println(this.timeLabel.getText());
        }
        if (this.timeFlowLabel != null) {
            String value = "undefined";
            if (this.timeFlowMode >= 1 && this.timeFlowMode <= 3) {
                value = this.timeModeVal[this.timeFlowMode - 1];
            }
            this.timeFlowLabel.setText("TimeFlowMode: " + value);
            this.timeFlowLabel.repaint();
        }
    }

    /**
     * get simulation-time-instance created in fileReset()
     *
     * @return simulation-time-instance, stored in Viewer
     */
    public SimulationTime getSimulationTimeInstance() {
        return this.simulationTime;
    }

    /**
     * get actual simulation-time
     *
     * @return simulation-time, stored in Model
     */
    public long getSimulationTime() {
        return this.model.getSimulationTime().getSimulationTime();
    }

    /**
     * update of speedField and speedSlider in gui
     *
     * @param speed
     */
    public void setSimulationSpeedGUI(double speed) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        String format = "#0.0";
        if (0.1 <= speed && speed < 1.0) {
            format = "#0.0";
        } else if (0.01 <= speed && speed < 0.1) {
            format = "#0.00";
        } else if (0.001 <= speed && speed < 0.01) {
            format = "#0.000";
        } else if (0.0001 <= speed && speed < 0.001) {
            format = "#0.0000";
        } else if (speed < 0.0001) {
            format = "#0.00000";
        }
        df.applyPattern(format);
        //System.out.println("Viewer.setSimulationSpeedGUI   "+df.format(speed)+"   "+speed);
        this.speedHandler = false;
        speedField.setText(df.format(speed));
        speedSlider.setValue((int) Math.rint(Math.log(speed)));
        this.speedHandler = true;
    }

    /**
     * get simulation-speed-value stored in viewer application
     *
     * @return simulation-speed, stored in Viewer
     */
    public double getSimulationSpeed() {
        return this.simulationSpeed;
    }

    /**
     * set simulation-speed-value end update gui
     *
     * @param speed
     */
    public void setSimulationSpeed(double speed) {
        this.simulationSpeed = speed;
        this.getSimulationTimeInstance().setSpeed(speed);
        this.setSimulationSpeedGUI(speed);
    }

    /**
     * update of zoomField and zoomSlider in gui
     *
     * @param zoom
     */
    public void setSimulationZoomGUI(double zoom) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        df.applyPattern("#0.00");
        //System.out.println("Viewer.setSimulationZoomGUI   "+df.format(zoom));
        this.zoomHandler = false;
        zoomField.setText(df.format(zoom));
        zoomSlider.setValue(10 * (int) Math.rint(Math.log(zoom)));
        this.zoomHandler = true;
    }

    /**
     * get simulation-zoom-value
     *
     * @return simulation-zoom-value, stored in Viewer
     */
    public double getSimulationZoom() {
        return model.getModelGrafic().getSelectedView().getZoomFactor();
    }

    /**
     * set simulation-zoom-value without update of zoomField and zoomSlider
     *
     * @param zoom
     * @throws ModelGraficException
     */
    public void setSimulationZoom(double zoom) throws ModelGraficException {
        //System.out.println("ViewerPanel.setSimulationZoom  zoom: "+zoom+"   valid: "+model.isValid());
        if (this.model.isValid()) {
            model.getModelGrafic().setZoomFactor(zoom, this.getSimulationZoomCenter(),
                simulationTime.getSimulationTime());
        }
    }

    public Point getSimulationZoomCenter() {
        return this.getModel().getModelGrafic().getSelectedView().getZoomPoint();
    }

    public void setSimulationZoomCenter(Point p) {
        this.getModel().getModelGrafic().setZoomFactor(getSimulationZoom(), p,
            simulationTime.getSimulationTime());
    }

    /**
     * set time-flow-mode, possible values can you find in constant declarations
     *
     * @return time-flow-mode, stored in Viewer
     */
    public int getTimeFlowMode() {
        return this.timeFlowMode;
    }

    /**
     * set time-flow-mode, possible values can you find in constant declarations
     *
     * @param timeFlowMode
     */
    public void setTimeFlowMode(int timeFlowMode) {
        this.timeFlowMode = timeFlowMode;
    }

    public SimulationThread getSimulationThread() {
        return this.simulationThread;
    }

    protected void setSimulationThreadNull() {
        this.simulationThread = null;
    }

    /**
     * create center-panel of gui. It includes the animation.
     *
     * @return CenterPanel
     * @throws ModelGraficException
     */
    private JPanel createCenterPanel() throws ModelGraficException {
        JPanel out;
        //System.out.println("Viewer.createCenterPanel    model.valid: "+model.isValid()+"   "+model.getModelGrafic());
        if (this.model.isValid()) {
			/*
			ModelGrafic mg = this.model.getModelGrafic();
			this.setSimulationZoom(this.getSimulationZoom());
			System.out.println("zoom  "+this.getSimulationZoom()+"   "+mg.getSelectedViewId()+"  "+mg.getSelectedView());
			*/
            //this.createModelGrafic().reset();
            //this.setSimulationZoom(1.0);
            out = model.getModelGrafic();
        } else {
            JPanel leer = new JPanel();
            leer.setBackground(Color.white);
            leer.setSize(1000, 1000);
            leer.setPreferredSize(getSize());
            out = leer;
        }
        return out;
    }

    /**
     * create header-panel of gui. It includes informations and control-elements.
     *
     * @return HeaderPanel
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setBackground(ViewerPanel.HEADER_BG_COLOR);
        header.setLayout(new GridLayout(1, 3));
        //header Left
        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BorderLayout());
        header.add(headerLeft);
        JPanel headerLeftTop = new JPanel();
        headerLeftTop.setLayout(new BorderLayout());
        headerLeftTop.setBackground(HEADER_BG_COLOR);
        headerLeft.add(BorderLayout.NORTH, headerLeftTop);
        zoomSlider = new JSlider(JSlider.HORIZONTAL, -20, 20, 0);
        zoomSlider.setSnapToTicks(false);
        zoomSlider.setBackground(header.getBackground());
        zoomSlider.addChangeListener(new ZoomSlider());
        zoomField = new JTextField("1.0", 5);
        zoomField.setLocale(Locale.US);
        zoomField.setColumns(5);
        zoomField.setText("1.0");
        zoomField.setBackground(header.getBackground());
        zoomField.addActionListener(new ZoomField());
        headerLeftTop.add(BorderLayout.WEST,
            new JLabel(this.language.getString("Screen_Zoom")));
        headerLeftTop.add(BorderLayout.CENTER, zoomSlider);
        headerLeftTop.add(BorderLayout.EAST, zoomField);
        JPanel headerLeftCenter = new JPanel();
        headerLeftCenter.setLayout(new GridLayout(1, 1));
        headerLeftCenter.setBackground(header.getBackground());
        headerLeft.add(BorderLayout.CENTER, headerLeftCenter);
        nameLabel = new JLabel("");
        nameLabel.setFont(ViewerPanel.FONT_MIDDLE);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setVerticalAlignment(SwingConstants.CENTER);
        nameLabel.setBackground(ViewerPanel.HEADER_BG_COLOR);
        nameLabel.setOpaque(true);
        JScrollPane nameScroll = new JScrollPane(nameLabel);
        nameScroll.setBackground(ViewerPanel.HEADER_BG_COLOR);
        nameScroll.setOpaque(true);
        nameScroll.setPreferredSize(new Dimension(120, 35));
        javax.swing.JScrollBar sb = nameScroll.getHorizontalScrollBar();
        sb.setPreferredSize(new Dimension(sb.getPreferredSize().width, 5));
        headerLeftCenter.add(nameScroll);

        //header Middle
        Box headerMiddle = Box.createVerticalBox();
        header.add(headerMiddle);

        // Zeitzone
        this.zeitzoneLabel = new JLabel(this.language.getString("Screen_Timezone"));
        this.zeitzoneLabel.setFont(FONT_SMALL);
        this.zeitzoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerMiddle.add(this.zeitzoneLabel);
        headerMiddle.add(Box.createVerticalGlue());

        // TimeLabel
        timeLabel = new JLabel("");
        timeLabel.setFont(ViewerPanel.FONT_MIDDLE);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerMiddle.add(Box.createVerticalGlue());
        headerMiddle.add(timeLabel);
        headerMiddle.add(Box.createVerticalGlue());

        // TimeFlow Label
        timeFlowLabel = new JLabel(this.language.getString("Screen_Mode"));
        timeFlowLabel.setFont(ViewerPanel.FONT_SMALL);
        timeFlowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerMiddle.add(timeFlowLabel);

        //header Right
        JPanel headerRight = new JPanel();
        headerRight.setLayout(new BorderLayout());
        header.add(headerRight);
        JPanel headerRightTop = new JPanel();
        headerRightTop.setBackground(header.getBackground());
        headerRightTop.setLayout(new BorderLayout());
        headerRight.add(BorderLayout.NORTH, headerRightTop);
        speedSlider = new JSlider(JSlider.HORIZONTAL, -7, 10, 0);
        speedSlider.setBackground(header.getBackground());
        speedSlider.addChangeListener(new SpeedSlider());
        speedField = new JTextField("1.0", 5);
        speedField.setBackground(header.getBackground());
        speedField.addActionListener(new SpeedField());
        headerRightTop.add(BorderLayout.WEST,
            new JLabel(this.language.getString("Screen_Speed")));
        headerRightTop.add(BorderLayout.CENTER, speedSlider);
        headerRightTop.add(BorderLayout.EAST, speedField);

        JPanel headerRightCenter = new JPanel();
        headerRightCenter.setBackground(header.getBackground());
        headerRightCenter.setLayout(new GridLayout(1, 3));
        headerRight.add(headerRightCenter, BorderLayout.CENTER);
        ImageIcon start = new ImageIcon(this.language.getInternURL("viewer_start_icon"));
        this.startButton = new JButton(start);
        this.startButton.setFont(ViewerPanel.FONT_MIDDLE);
        this.startButton.setEnabled(false);
        this.startButton.addActionListener(new Start());
        headerRightCenter.add(this.startButton);
        ImageIcon pause = new ImageIcon(this.language.getInternURL("viewer_pause_icon"));
        this.pauseButton = new JButton(pause);
        this.pauseButton.setFont(ViewerPanel.FONT_MIDDLE);
        this.pauseButton.setEnabled(false);
        this.pauseButton.addActionListener(new Pause());
        headerRightCenter.add(this.pauseButton);
        ImageIcon stop = new ImageIcon(this.language.getInternURL("viewer_stop_icon"));
        this.stopButton = new JButton(stop);
        this.stopButton.setFont(ViewerPanel.FONT_MIDDLE);
        this.stopButton.setEnabled(false);
        this.stopButton.addActionListener(new Stop());
        headerRightCenter.add(this.stopButton);

        JPanel headerRightBottom = new JPanel();
        headerRightBottom.setBackground(header.getBackground());
        headerRightBottom.setLayout(new GridLayout(1, 1));
        headerRight.add(headerRightBottom, BorderLayout.SOUTH);
        // startLabel, endLabel
        Box headerRightTime = Box.createHorizontalBox();
        JLabel beginBez = new JLabel(this.language.getString("Screen_Begin") + " ");
        beginBez.setFont(ViewerPanel.FONT_SMALL);
        headerRightTime.add(beginBez);
        startLabel = new JLabel("");
        startLabel.setFont(ViewerPanel.FONT_SMALL);
        headerRightTime.add(startLabel);
        headerRightTime.add(Box.createHorizontalGlue());
        JLabel endBez = new JLabel(" " + this.language.getString("Screen_End") + " ");
        endBez.setFont(ViewerPanel.FONT_SMALL);
        headerRightTime.add(endBez);
        endLabel = new JLabel("");
        endLabel.setFont(ViewerPanel.FONT_SMALL);
        headerRightTime.add(endLabel);
        headerRightBottom.add(headerRightTime);

        return header;
    }


    /**
     * create status-panel of gui. It includes some status-information.
     *
     * @return StatusPanel
     */
    private JPanel createStatusPanel() {
        JPanel status = new JPanel();
        status.setBackground(ViewerPanel.STATUS_BG_COLOR);
        status.setLayout(new BorderLayout());
        JLabel statusLabel = new JLabel(this.language.getString("Screen_Status"));
        statusLabel.setFont(ViewerPanel.FONT_MIDDLE);
        status.add(BorderLayout.WEST, statusLabel);
        JPanel statusCenter = new JPanel();
        status.add(BorderLayout.CENTER, statusCenter);
        statusCenter.setLayout(new FlowLayout(FlowLayout.CENTER));
        statusCenter.setBackground(STATUS_BG_COLOR);
        statusMessage = new JLabel();
        statusMessage.setFont(FONT_MIDDLE);
        statusMessage.setForeground(STATUS_MSG_COLOR);
        statusCenter.add(statusMessage);
        JPanel koordinaten = new JPanel();
        koordinaten.setLayout(new FlowLayout());
        koordinaten.setBackground(ViewerPanel.STATUS_BG_COLOR);
        status.add(BorderLayout.EAST, koordinaten);
        coordinateXField = new JTextField("", 5);
        coordinateXField.setEditable(false);
        coordinateXField.setHorizontalAlignment(JTextField.RIGHT);
        coordinateXField.setBackground(ViewerPanel.STATUS_BG_COLOR);
        coordinateYField = new JTextField("", 5);
        coordinateYField.setEditable(false);
        coordinateYField.setHorizontalAlignment(JTextField.LEFT);
        coordinateYField.setBackground(ViewerPanel.STATUS_BG_COLOR);
        koordinaten.add(coordinateXField);
        koordinaten.add(new JLabel("/"));
        koordinaten.add(coordinateYField);
        return status;
    }

    /**
     * create menue of ViewerFrame and ViewerApplet.
     *
     * @param exitListener ActionListener, which close the ViewerFrame when null, Data menuBar has no exitItem
     * @param isApplet     when true, there is no "Data" menuBar
     * @return
     */
    protected JMenuBar createMenueBar(ActionListener exitListener) {
        //System.out.println("createMenueBar");
        this.menuBar = new JMenuBar();
        if (this.isApplication) {
            JMenu data = new JMenu(this.language.getString("Menu_Data"));
            menuBar.add(data);
            JMenuItem fileOpen = new JMenuItem(this.language.getString("Menu_DataOpen"));
            fileOpen.addActionListener(new FileOpen());
            data.add(fileOpen);
            JMenuItem fileClose = new JMenuItem(this.language.getString("Menu_DataClose"));
            fileClose.addActionListener(new FileClose());
            data.add(fileClose);
            JMenuItem iconDirectory = new JMenuItem(this.language.getString("Menu_DataIconDir"));
            iconDirectory.addActionListener(new IconDirectory());
            data.add(iconDirectory);
            JMenuItem snapShotDirectory = new JMenuItem(this.language.getString("Menu_DataSnapShotDir"));
            snapShotDirectory.addActionListener(new SnapShotDirectory());
            data.add(snapShotDirectory);
            if (exitListener != null) {
                data.addSeparator();
                JMenuItem exit = new JMenuItem(this.language.getString("Menu_DataExit"));
                exit.addActionListener(exitListener);
                data.add(exit);
            }
        }


        JMenu simulation = new JMenu(this.language.getString("Menu_Simulation"));
        menuBar.add(simulation);
        ImageIcon start = new ImageIcon(this.language.getInternURL("viewer_start_icon"));
        startItem = new JMenuItem(this.language.getString("Menu_SimulationStart"), start);
        startItem.setEnabled(false);
        startItem.addActionListener(new Start());
        simulation.add(startItem);
        ImageIcon pause = new ImageIcon(this.language.getInternURL("viewer_pause_icon"));
        pauseItem = new JMenuItem(this.language.getString("Menu_SimulationPause"), pause);
        pauseItem.setEnabled(false);
        pauseItem.addActionListener(new Pause());
        simulation.add(pauseItem);
        ImageIcon stop = new ImageIcon(this.language.getInternURL("viewer_stop_icon"));
        stopItem = new JMenuItem(this.language.getString("Menu_SimulationStop"), stop);
        stopItem.setEnabled(false);
        stopItem.addActionListener(new Stop());
        simulation.add(stopItem);
        JMenuItem speed = new JMenuItem(this.language.getString("Menu_SimulationSpeed"));
        speed.addActionListener(new SpeedMenu());
        simulation.add(speed);
        simulation.addSeparator();
        JMenuItem timeFlow = new JMenuItem(this.language.getString("Menu_SimulationMode"));
        timeFlow.addActionListener(new TimeFlow());
        simulation.add(timeFlow);

        JMenu view = new JMenu(this.language.getString("Menu_View"));
        menuBar.add(view);
        JMenuItem zoomItem = new JMenuItem(this.language.getString("Menu_ViewZoom"));
        zoomItem.addActionListener(new ZoomMenu());
        view.add(zoomItem);
        JMenuItem snapShotSelItem = new JMenuItem(this.language.getString("Menu_SnapShotSel"));
        snapShotSelItem.setEnabled(isApplication && this.snapShotDir != null);
        snapShotSelItem.addActionListener(new SnapShot(false));
        view.add(snapShotSelItem);
        JMenuItem snapShotAllItem = new JMenuItem(this.language.getString("Menu_SnapShotAll"));
        snapShotAllItem.setEnabled(isApplication && this.snapShotDir != null);
        snapShotAllItem.addActionListener(new SnapShot(true));
        view.add(snapShotAllItem);

        JMenu help = new JMenu(this.language.getString("Menu_Help"));
        menuBar.add(help);
        HelpDialog helpDialog = new HelpDialog(this);
        URL onlineHelp = helpDialog.getExternURL("VISUALIZATION_HELP_URL");
        JMenuItem onlineHelpItem = new JMenuItem(this.language.getString("Menu_HelpOnline"));
        onlineHelpItem.addActionListener(new Click(onlineHelp, this.getAppletContext()));
        help.add(onlineHelpItem);
        JMenuItem offlineHelpItem = new JMenuItem(this.language.getString("Menu_HelpOffline"));
        offlineHelpItem.addActionListener(helpDialog);
        help.add(offlineHelpItem);
        help.addSeparator();
        this.projectItem = new JMenuItem(this.language.getString("Menu_HelpProject"));
        this.projectItem.addActionListener(new AboutModelDialog(this));
        help.add(this.projectItem);
        JMenuItem aboutItem = new JMenuItem(this.language.getString("Menu_HelpFramework"));
        aboutItem.addActionListener(new AboutEngineDialog(this));
        help.add(aboutItem);

        this.updateMenuBar();
        return menuBar;
    }

    /**
     * must be called by opening and closing a model
     */
    private void updateMenuBar() {
        //System.out.println("updateMenuBar");
        if (this.menueBarIcons == null) {
            this.menueBarIcons = new Vector<Component>();
        }
        AboutModelDialog aboutModelDialog =
            (AboutModelDialog) this.projectItem.getActionListeners()[0];
        ImageIcon projectIcon = aboutModelDialog.getProjectMenueIcon();

        this.projectItem.setEnabled(this.getModel().isValid());
        this.projectItem.revalidate();

        Iterator<Component> it = menueBarIcons.iterator();
        while (it.hasNext()) {
            this.menuBar.remove(it.next());
        }
        //System.out.println("Without Icons Nr of Components: "+this.menuBar.getMenuCount());
        menueBarIcons.clear();
        Component comp;
        // Icons rechts einfuegen
        comp = Box.createHorizontalGlue();
        menuBar.add(comp);
        menueBarIcons.add(comp);
        if (projectIcon != null) {
            JLabel project = new JLabel(projectIcon);
            menuBar.add(project);
            menueBarIcons.add(project);
            comp = Box.createRigidArea(new Dimension(10, 10));
            menuBar.add(comp);
            menueBarIcons.add(comp);
        }
        JLabel u_hamburg = new JLabel();
        u_hamburg.setIcon(new ImageIcon(this.language.getInternURL("u_hamburg_menue_icon")));
        menuBar.add(u_hamburg);
        menueBarIcons.add(u_hamburg);
        comp = Box.createRigidArea(new Dimension(10, 10));
        menuBar.add(comp);
        menueBarIcons.add(comp);
	    /*
		JLabel desmoJ = new JLabel();
		desmoJ.setIcon(new ImageIcon(this.language.getInternURL("desmoJ_icon")));
	    menuBar.add(desmoJ);
	    menueBarIcons.add(desmoJ);
	    comp = Box.createRigidArea(new Dimension(10,10));
	    menuBar.add(comp);
	    menueBarIcons.add(comp);
	    */
        JLabel th_wildau = new JLabel();
        th_wildau.setIcon(new ImageIcon(this.language.getInternURL("th_wildau_menue_icon")));
        menuBar.add(th_wildau);
        menueBarIcons.add(th_wildau);
        //System.out.println("With    Icons Nr of Components: "+this.menuBar.getMenuCount());

        menuBar.revalidate();
        menuBar.repaint();
    }

    protected int getMenuBarHeight() {
        return this.menuBar.getHeight();
    }


    /**
     * ActionListener of TimeFlow-Menue-Item
     *
     * @author Christian
     */
    class TimeFlow implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //System.out.println("begin: "+viewer.getTimeFlow());
            String message = "please, choose a Time Flow";
            String title = "Time Flow";
            int messageType = JOptionPane.QUESTION_MESSAGE;
            Icon icon = null;
            String[] selectionValues = viewer.timeModeVal;
            String defaultValue;
            switch (viewer.getTimeFlowMode()) {
                case ViewerPanel.TimeFlowMode_CONTINIUM:
                    defaultValue = selectionValues[0];
                    break;
                case ViewerPanel.TimeFlowMode_STEP_FLOW:
                    defaultValue = selectionValues[1];
                    break;
                case ViewerPanel.TimeFlowMode_STEP_SINGLE:
                    defaultValue = selectionValues[2];
                    break;
                default:
                    defaultValue = selectionValues[1];
                    viewer.setTimeFlowMode(ViewerPanel.TimeFlowMode_STEP_FLOW);
                    break;
            }
            //System.out.println("default: "+defaultValue);
            String input = (String) JOptionPane.showInputDialog(viewer, message,
                title, messageType, icon, selectionValues, defaultValue);
            if (input == null) {
                input = selectionValues[viewer.getTimeFlowMode() - 1];
            }
            if (input.equals(selectionValues[0])) {
                viewer.setTimeFlowMode(ViewerPanel.TimeFlowMode_CONTINIUM);
            } else if (input.equals(selectionValues[1])) {
                viewer.setTimeFlowMode(ViewerPanel.TimeFlowMode_STEP_FLOW);
            } else if (input.equals(selectionValues[2])) {
                viewer.setTimeFlowMode(ViewerPanel.TimeFlowMode_STEP_SINGLE);
            } else {
                viewer.setTimeFlowMode(ViewerPanel.TimeFlowMode_CONTINIUM);
            }
            //System.out.println("end  : "+viewer.getTimeFlow());
        }
    }

    /**
     * ActionListener of SimulationSpeed-Menue-Item
     *
     * @author Christian
     */
    class SpeedMenu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double speed = getSimulationSpeed();
            String input;
            String message = "please, select a speed factor of simulation.";
            String defaultValue = speedField.getText();
            boolean ok = false;
            do {
                input = JOptionPane.showInputDialog(viewer, message, defaultValue);
                ok = true;
                try {
                    if (input != null) {
                        speed = Double.parseDouble(input);
                    }
                } catch (NumberFormatException ex) {
                    ok = false;
                }
            } while (!ok);
            if (ok && input != null) {
                speed = Math.abs(speed);
                setSimulationSpeed(speed);
                setSimulationSpeedGUI(speed);
            }
            //System.out.println(speedField.getText());
        }
    }

    /**
     * ChangeListener of SimulationSpeed-Slider in Header-Panel
     *
     * @author Christian
     */
    class SpeedSlider implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (speedHandler) {
                double speed = Math.exp(speedSlider.getValue());
                setSimulationSpeed(speed);
                setSimulationSpeedGUI(speed);
            }
        }
    }

    /**
     * ActionListener of SimulationSpeed-TextField in Header-Panel
     *
     * @author Christian
     */
    class SpeedField implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (speedHandler) {
                double speed = 1.0;
                try {
                    speed = Double.parseDouble(speedField.getText());
                } catch (NumberFormatException ex) {
                    speed = 1.0;
                }
                speed = Math.abs(speed);
                setSimulationSpeed(speed);
                setSimulationSpeedGUI(speed);
            }
        }
    }

    /**
     * ActionListener of SimulationZoom-MenueItem
     *
     * @author Christian
     */
    class ZoomMenu implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double zoom = getSimulationZoom();
            String input;
            String message = "please, select zoom factor of simulation.";
            String defaultValue = zoomField.getText();
            boolean ok = false;
            do {
                input = JOptionPane.showInputDialog(viewer, message, defaultValue);
                ok = true;
                try {
                    if (input != null) {
                        zoom = Double.parseDouble(input);
                    }
                } catch (NumberFormatException ex) {
                    ok = false;
                }
            } while (!ok);
            if (ok && input != null) {
                zoom = Math.abs(zoom);
                zoom = Math.max(0.00001, zoom);
                try {
                    setSimulationZoom(zoom);
                } catch (ModelGraficException e1) {
                    setStatusMessage(e1.getMessage());
                    e1.printStackTrace(ViewerPanel.getLogWriter());
                    ViewerPanel.getLogWriter().close();
                    e1.printStackTrace();
                }
                setSimulationZoomGUI(zoom);
            }
            //System.out.println(zoomField.getText());
        }
    }

    /**
     * ActionListener for writing SnapShots into directory snapshot
     *
     * @author Christian
     */
    class SnapShot implements ActionListener {

        // create snapshots from all views or only from the actual selected view
        boolean all;

        public SnapShot(boolean all) {
            this.all = all;
        }

        public void actionPerformed(ActionEvent e) {
            File snapShot = null;
            try {
                snapShot = new File(snapShotDir.toURI());
            } catch (URISyntaxException e2) {
                snapShot = new File(snapShotDir.getPath());
            }
            if (snapShot != null && snapShot.isDirectory() && snapShot.canWrite()) {

                // The simulation is paused while creating snapshots
                boolean isPause = simulationTime.isPause();
                if (!isPause) {
                    simulationTime.pause();
                }

                String time =
                    SimulationTime.getTimeString(getSimulationTime(), SimulationTime.SHOW_DAY_DATE_TIME_MILLIS_DST);
                time = this.adjustFileName(time);
                // Subdirectory to store all images of a snapshot
                File directory = new File(snapShot, time);
                if (directory.isFile()) {
                    System.out.println(
                        "Error: " + directory.getAbsolutePath() + " is a file. A directory is expected.");
                    if (!isPause) {
                        simulationTime.cont();
                    }
                    return;
                }
                if (directory.isDirectory()) {
                    // delete content
                    File[] content = directory.listFiles();
                    for (int i = 0; i < content.length; i++) {
                        content[i].delete();
                    }
                } else {
                    directory.mkdir();
                }
                try {
                    boolean rc = model.getModelGrafic().makeSnapShot(directory, this.all);
                    if (rc) {
                        System.out.println("Snapshot is written into " + directory.getAbsolutePath());
                    } else {
                        System.out.println("Snapshot directory isn't writeable dir: " + directory.getAbsolutePath());
                    }
                } catch (ModelGraficException e1) {
                    System.out.println("Error by creating Snapshot");
                    e1.printStackTrace();
                } catch (IOException e1) {
                    System.out.println("Error by creating Snapshot");
                    e1.printStackTrace();
                }

                if (!isPause) {
                    simulationTime.cont();
                }
            }
        }

        private String adjustFileName(String name) {
            name = name.replace(' ', '_');
            name = name.replace(',', '_');
            name = name.replace('.', '-');
            name = name.replace(':', '-');
            name = name.replace('/', '%');
            name = name.replace('\\', '%');
            return name;
        }
    }

    /**
     * ChangeListener for SimulationZoom of Slider in Header-Panel
     *
     * @author Christian
     */
    class ZoomSlider implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (zoomHandler) {
                // JSlider liefert nur sinnvolle Werte wenn getValueIsAdjusting() == true
                if (zoomSlider.getValueIsAdjusting()) {
                    double zoom = Math.exp((double) zoomSlider.getValue() / 10.0);
                    //System.out.println("Call from ZoomSlider   zoom: "+zoom);
                    try {
                        setSimulationZoom(zoom);
                    } catch (ModelGraficException e1) {
                        setStatusMessage(e1.getMessage());
                        e1.printStackTrace(ViewerPanel.getLogWriter());
                        ViewerPanel.getLogWriter().close();
                        e1.printStackTrace();
                    }
                    setSimulationZoomGUI(zoom);
                }
            }
        }
    }

    /**
     * ActionListener for SimulationZoom of TextField in Header-Panel
     *
     * @author Christian
     */
    class ZoomField implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (zoomHandler) {
                double zoom = 1.0;
                try {
                    zoom = Double.parseDouble(zoomField.getText());
                } catch (NumberFormatException ex) {
                    zoom = 1.0;
                }
                zoom = Math.abs(zoom);
                zoom = Math.max(0.00001, zoom);
                //System.out.println("Call from ZoomField");
                try {
                    setSimulationZoom(zoom);
                } catch (ModelGraficException e1) {
                    setStatusMessage(e1.getMessage());
                    e1.printStackTrace(ViewerPanel.getLogWriter());
                    ViewerPanel.getLogWriter().close();
                    e1.printStackTrace();
                }
                setSimulationZoomGUI(zoom);
            }
        }
    }

    /**
     * ActionListener of FileOpen-Menue-Item
     *
     * @author Christian
     */
    class FileOpen implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("FileOpenClass begin      "+viewer.defaultCmdPath);
            if (viewer.defaultCmdPath != null) {
                JFileChooser fc = new JFileChooser(viewer.defaultCmdPath);
                fc.setDialogTitle("select a cmd File");
                fc.setFileFilter(new CmdsFileFilter());
                File out = null;
                int returnVal = fc.showOpenDialog(viewer);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    out = fc.getSelectedFile();
                }
                if (out != null) {
                    if (out.getParent() != null) {
                        viewer.defaultCmdPath = out.getParent();
                    } else {
                        viewer.defaultCmdPath = out.getPath();
                    }

                    try {
                        viewer.dataFile = out.toURI().toURL();
                        viewer.fileOpen(viewer.dataFile);
                        String outName = out.getParentFile().getName() + "/" + out.getName();
                        viewer.setStatusMessage(
                            language.getString("StatusMsg_10") + outName + " (choose Icondir before)");
                    } catch (Exception e1) {
                        setStatusMessage(e1.getMessage());
                        e1.printStackTrace(ViewerPanel.getLogWriter());
                        ViewerPanel.getLogWriter().close();
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * ActionListener of FileClose-Menue-Item
     *
     * @author Christian
     */
    class FileClose implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("FileClose");
            try {
                fileClose();
            } catch (Exception e1) {
                setStatusMessage(e1.getMessage());
                e1.printStackTrace(ViewerPanel.getLogWriter());
                ViewerPanel.getLogWriter().close();
                e1.printStackTrace();
            }
        }
    }

    class IconDirectory implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("IconDirectory");
            if (viewer.defaultIconPath != null) {
                JFileChooser fc = new JFileChooser(viewer.defaultIconPath);
                fc.setDialogTitle("select a Icon Directory");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setFileFilter(new IconDirFilter());
                File out = null;
                int returnVal = fc.showOpenDialog(viewer);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    out = fc.getSelectedFile();
                }
                if (out != null) {
                    if (out.getParent() != null) {
                        viewer.defaultIconPath = out.getParent();
                    } else {
                        viewer.defaultIconPath = out.getPath();
                    }

                    try {
                        viewerIconDir = out.toURI().toURL();
                        viewer.model.setSimulationIconDir(viewerIconDir);
                        String outName = out.getParentFile().getName() + "/" + out.getName();
                        viewer.setStatusMessage(language.getString("StatusMsg_11") + outName);
                        //viewer.fileOpen(viewer.dataFile);
                    } catch (MalformedURLException e2) {
                        viewer.setStatusMessage(language.getString("StatusMsg_12"));
                    }
                }
            }
        }
    }

    class SnapShotDirectory implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("SnapShotDirectory");
            if (viewer.defaultSnapShotPath != null) {
                JFileChooser fc = new JFileChooser(viewer.defaultSnapShotPath);
                fc.setDialogTitle("select a SnapShot Directory");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setFileFilter(new SnapShotDirFilter());
                File out = null;
                int returnVal = fc.showOpenDialog(viewer);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    out = fc.getSelectedFile();
                }
                if (out != null) {
                    if (out.getParent() != null) {
                        viewer.defaultSnapShotPath = out.getParent();
                    } else {
                        viewer.defaultSnapShotPath = out.getPath();
                    }

                    try {
                        snapShotDir = out.toURI().toURL();
                        String outName = out.getParentFile().getName() + "/" + out.getName();
                        viewer.setStatusMessage(language.getString("StatusMsg_15") + outName);
                    } catch (MalformedURLException e2) {
                        viewer.setStatusMessage(language.getString("StatusMsg_16"));
                    }
                }
            }
        }
    }


    /**
     * ActionListener of Start-Button in Header-Panel
     *
     * @author Christian
     */
    class Start implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("Animation Start");
            simulationTime.cont();
            if (viewer.getTimeFlowMode() == ViewerPanel.TimeFlowMode_STEP_SINGLE) {
                // singleStep Mode
                startButton.setEnabled(true);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(false);

                startItem.setEnabled(true);
                stopItem.setEnabled(true);
                pauseItem.setEnabled(false);

            } else {
                // stepflow or continiusFlow Mode
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(true);

                startItem.setEnabled(false);
                stopItem.setEnabled(true);
                pauseItem.setEnabled(true);

            }


        }
    }

    /**
     * ActionListener of Stop-Button in Header-Panel
     *
     * @author Christian
     */
    class Stop implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("Stop pressed");
            simulationTime.stop();
            reload();
            simulationTime.start();
            ViewerPanel.getInfoPane().refresh();
            ViewerPanel.getInfoPane().revalidate();
            ViewerPanel.getInfoPane().repaint();
            simulationTime.pause();


            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);

            startItem.setEnabled(true);
            stopItem.setEnabled(false);
            pauseItem.setEnabled(false);

            lastCall();
            validate();
        }
    }

    /**
     * ActionListener of Pause-Button in Header-Panel
     *
     * @author Christian
     */
    class Pause implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //System.out.println("Pause pressed");
            simulationTime.pause();
            startButton.setEnabled(true);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(false);

            startItem.setEnabled(true);
            stopItem.setEnabled(true);
            pauseItem.setEnabled(false);
        }
    }

    /**
     * Management von Fenstergroessen
     *
     * @author Christian
     */
    class ResizeListener implements HierarchyBoundsListener {

        public void ancestorMoved(HierarchyEvent arg0) {
        }

        public void ancestorResized(HierarchyEvent arg0) {
            // zeigt den Zoomcenter in der Mitte des Bildausschnittes an
            if (viewer.getModel().isValid()) {
                Hashtable<String, ZoomEntry> zoom = model.getModelGrafic().getZoomProperty();
                model.getModelGrafic().setZoomProperty(zoom);
                viewer.lastCall();
            }
        }

    }


}
