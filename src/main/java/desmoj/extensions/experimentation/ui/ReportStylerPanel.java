package desmoj.extensions.experimentation.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Pattern;

import desmoj.extensions.experimentation.util.FileUtil;
import desmoj.extensions.experimentation.util.XSLFilter;
import desmoj.extensions.xml.util.DocumentReader;
import desmoj.extensions.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * <p>
 * ReportStylerPanel
 * </p>
 * <p>
 * A JPanel that allows to make selections on which parts of a certain trace or report output shall be shown.
 * </p>
 *
 * @author Gunnar Kiesel, modified by Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class ReportStylerPanel extends JPanel {

    /** Position of xsl files in file system */
    private static final String XSL_PATH = "desmoj/extensions/xml/xslFiles/";
    /** the ClassLoader is needed to load the xsl Resources */
    ClassLoader classLoader = this.getClass().getClassLoader();
    /** GUI components * */
    JLabel experimentLabel = new JLabel("Settings");
    JComboBox entityBox = new JComboBox();
    JComboBox eventBox = new JComboBox();
    JComboBox reporterBox = new JComboBox();
    JFormattedTextField startTimeField = new JFormattedTextField();
    JFormattedTextField stopTimeField = new JFormattedTextField();
    JTabbedPane tabbedReportPane = new JTabbedPane();
    JScrollPane reportScrollPane = new JScrollPane();
    JTabbedPane tabbedTracePane = new JTabbedPane();
    JScrollPane traceScrollPane = new JScrollPane();
    JPanel selectionPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane();
    JSplitPane splitPane = new JSplitPane();
    JPanel traceButtonsPanel = new JPanel();
    JPanel reportButtonsPanel = new JPanel();
    JPanel reportXMLPanel = new JPanel();
    JButton applyTraceSettings = new JButton("apply");
    JButton loadTraceSettings = new JButton("load");
    JButton saveTraceSettings = new JButton("save");
    JButton applyReportSettings = new JButton("apply");
    JButton loadReportSettings = new JButton("load");
    JButton saveReportSettings = new JButton("save");
    JButton setReportXSLFile = new JButton("set Report Stylesheet");
    /**
     * set the default xsl stylesheets and coresponding param stylsheets
     */
    private final String sourceReportXSLFilename = XSL_PATH + "reportHTML10R15P.xsl";
    private final String sourceTraceXSLFilename = XSL_PATH + "traceHTML.xsl";
    private final String sourceReportParamXSLFilename = XSL_PATH + "reportHTMLParam.xsl";
    private final String sourceTraceParamXSLFilename = XSL_PATH + "traceHTMLParam.xsl";
    /** the xsl style sheets in use */
    private final String userDir = System.getProperty("user.dir") + "/";
    private String reportXSLFilename = userDir + "reportHTML.xsl";
    private String traceXSLFilename = userDir + "traceHTML.xsl";
    private final String reportParamXSLFilename = userDir + "reportHTMLParam.xsl";
    private final String traceParamXSLFilename = userDir + "traceHTMLParam.xsl";
    private String externReportParamFile;
    private String externTraceParamFile;
    /** the xml trace and report files currently worked with* */
    private String reportXMLFilename;
    private String traceXMLFilename;
    /**
     * set the maximum numbers for reporters and reporter-parameters that can be selected.
     */
    private int maxReporters = 10;
    JLabel maxReportersLabel = new JLabel("max. Reporters: " + maxReporters);
    private int maxParameters = 15;
    JLabel maxParametersLabel = new JLabel("max. Parameters: " + maxParameters);

    /** Creates a new ReportStyler */
    public ReportStylerPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Initializes the user interface */
    private void jbInit() throws Exception {
        FileUtil.copy(classLoader.getResource(sourceReportXSLFilename),
            reportXSLFilename);
        FileUtil.copy(classLoader.getResource(sourceTraceXSLFilename),
            traceXSLFilename);
        FileUtil.copy(classLoader.getResource(sourceReportParamXSLFilename),
            reportParamXSLFilename);
        FileUtil.copy(classLoader.getResource(sourceTraceParamXSLFilename),
            traceParamXSLFilename);

        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);

        applyTraceSettings.setVisible(false);
        applyTraceSettings.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyTraceSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeTraceParameters();
                createTraceHTML();
            }
        });
        loadTraceSettings.setVisible(false);
        loadTraceSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                externTraceParamFile = loadDialog(externTraceParamFile);
                loadTraceParameters(externTraceParamFile);
            }
        });
        saveTraceSettings.setVisible(false);
        saveTraceSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                externTraceParamFile = saveDialog(externTraceParamFile);
                writeTraceParameters(externTraceParamFile);
            }
        });
        traceButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        traceButtonsPanel.add(applyTraceSettings);
        traceButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        traceButtonsPanel.add(loadTraceSettings);
        traceButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        traceButtonsPanel.add(saveTraceSettings);
        traceButtonsPanel.setLayout(new BoxLayout(traceButtonsPanel,
            BoxLayout.X_AXIS));
        traceButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        applyReportSettings.setVisible(false);
        applyReportSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeReportParameters();
                createReportHTML();
            }
        });
        loadReportSettings.setVisible(false);
        loadReportSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                externReportParamFile = loadDialog(externReportParamFile);
                loadReportParameters(externReportParamFile);
            }
        });
        saveReportSettings.setVisible(false);
        saveReportSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                externReportParamFile = saveDialog(externReportParamFile);
                writeReportParameters(externReportParamFile);
            }
        });
        reportButtonsPanel.setLayout(new BoxLayout(reportButtonsPanel,
            BoxLayout.X_AXIS));
        reportButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        reportButtonsPanel.add(applyReportSettings);
        reportButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        reportButtonsPanel.add(loadReportSettings);
        reportButtonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        reportButtonsPanel.add(saveReportSettings);
        reportButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        setReportXSLFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setReportXSLFile(loadDialog(reportXSLFilename));
            }
        });
        reportXMLPanel
            .setLayout(new BoxLayout(reportXMLPanel, BoxLayout.X_AXIS));
        reportXMLPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        reportXMLPanel.add(setReportXSLFile);
        reportXMLPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        reportXMLPanel.add(maxReportersLabel);
        reportXMLPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        reportXMLPanel.add(maxParametersLabel);
        reportXMLPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tabbedTracePane.setAlignmentX(Component.LEFT_ALIGNMENT);
        tabbedReportPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectionPanel
            .setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.add(experimentLabel);
        selectionPanel.add(new JLabel("Trace Settings"));
        selectionPanel.add(traceButtonsPanel);
        selectionPanel.add(tabbedTracePane);
        selectionPanel.add(new JLabel("Report Settings"));
        selectionPanel.add(reportButtonsPanel);
        selectionPanel.add(tabbedReportPane);
        selectionPanel.add(reportXMLPanel);

        scrollPane.getViewport().add(selectionPanel, null);
    }

    /**
     * set the xsl style sheet to be used for the reports
     *
     * @param filename String: The name of the XSL file the report-ouptut is to be formated with.
     */
    public void setReportXSLFile(String filename) {
        maxReportersLabel.setVisible(false);
        maxParametersLabel.setVisible(false);
        try {
            Document doc = DocumentReader.getInstance().readDoc(filename);
            Element root = doc.getDocumentElement();
            if (root.getNodeName() == "xsl:stylesheet") {
                NodeList list = root.getElementsByTagName("xsl:param");
                for (int i = 0; i < list.getLength(); i++) {
                    Element element = (Element) list.item(i);
                    if (element.getAttribute("name").equals("maxReporters")) {
                        maxReporters = Integer.parseInt(element
                            .getAttribute("select"));
                        maxReportersLabel.setText("max. Reporters: "
                            + maxReporters);
                    }
                    if (element.getAttribute("name").equals("maxParameters")) {
                        maxParameters = Integer.parseInt(element
                            .getAttribute("select"));
                        maxParametersLabel.setText("max. Parameters: "
                            + maxParameters);
                    }
                }
                reportXSLFilename = filename;
            } else {
                System.out.println("file " + filename
                    + " is not a valid xsl:stylesheet!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        maxReportersLabel.setVisible(true);
        maxParametersLabel.setVisible(true);
    }

    /**
     * set the xsl style sheet to be used for the traces
     *
     * @param filename String: The name of the XSL file the trace-ouptut is to be formated with.
     */
    public void setTraceXSLFile(String filename) {
        traceXSLFilename = filename;
    }

    /**
     * set the xml-report currently used
     *
     * @param filename String: The name of the report-output XML file.
     */
    public void setReportXMLFile(String filename) {
        reportXMLFilename = filename;
    }

    /**
     * set the xml-trace currently used
     *
     * @param filename String: The name of the trace-output XML file.
     */
    public void setTraceXMLFile(String filename) {
        traceXMLFilename = filename;
    }

    /**
     * replace the report-parameters currently used
     *
     * @param filename String: The name of the a new report-parameter XSL file.
     */
    public void changeReportParamXSLFile(URL filename) {
        FileUtil.copy(filename, reportParamXSLFilename);
    }

    /**
     * replace the trace-parameters currently used
     *
     * @param filename String: The name of the a new trace-parameter XSL file.
     */
    public void changeTraceParamXSLFile(URL filename) {
        FileUtil.copy(filename, traceParamXSLFilename);
    }

    /**
     * updates the current view of the Panel with values for baseFilename. basefilname gives the path to xml-files
     * without the _trace.xml / _report.xml endings
     */
    protected void updateView(String baseFilename, String traceFilename,
                              String reportFilename) {
        String[] experimentName = baseFilename.split("/");
        experimentLabel.setText("Settings for Experiment "
            + experimentName[experimentName.length - 1]);
        if (traceFilename.endsWith(".xml")) {
            setTraceXMLFile(traceFilename);
            initializeTracePanel(traceFilename);
            loadTraceParameters();
        } else {
            hideTracePanel();
        }
        if (reportFilename.endsWith(".xml")) {
            setReportXMLFile(reportFilename);
            initializeReportPanel(reportFilename);
            loadReportParameters();
        } else {
            hideReportPanel();
        }
        this.repaint(0);
    }

    /** delete the files copied at the initialisation * */
    protected void deleteXSLFiles() {
        FileUtil.deleteFile(reportXSLFilename);
        FileUtil.deleteFile(traceXSLFilename);
        FileUtil.deleteFile(reportParamXSLFilename);
        FileUtil.deleteFile(traceParamXSLFilename);
    }

    /** create the HTML output out of the XML-files.* */
    private void createTraceHTML() {
        String traceOutputFilename = traceXMLFilename.replaceFirst(".xml",
            ".html");
        XMLHelper.applyXSL(new File(traceXMLFilename), new File(
            traceXSLFilename), traceOutputFilename);
    }

    private void createReportHTML() {
        String reportOutputFilename = reportXMLFilename.replaceFirst(".xml",
            ".html");
        XMLHelper.applyXSL(new File(reportXMLFilename), new File(
            reportXSLFilename), reportOutputFilename);
    }

    /**
     * this function returns a vector with the elements startTime and stopTime, a vector with the list of entities and a
     * vector with the list of events of the trace in the file xmlTraceFilename.
     */
    private void initializeTracePanel(String xmlTraceFilename) {
        tabbedTracePane.removeAll();
        eventBox.removeAllItems();
        eventBox.addItem("all events");
        eventBox.addItem("no events");
        entityBox.removeAllItems();
        entityBox.addItem("all entities");
        entityBox.addItem("no entities");
        Document doc = null;
        try {
            doc = DocumentReader.getInstance().readDoc(
                xmlTraceFilename);
        } catch (IOException e) {
            e.printStackTrace();
            hideTracePanel();
            return;
        } catch (SAXException e) {
            e.printStackTrace();
            hideTracePanel();
            return;
        }
        Element root = doc.getDocumentElement();
        if (root.getNodeName() != "trace") {
            JLabel errorMsg = new JLabel(xmlTraceFilename
                + " is not a valid xml-Trace.");
            tabbedTracePane.add(errorMsg, "Error");
        } else {
            NodeList list = root.getElementsByTagName("note");
            Element first = (Element) list.item(0);
            Element last = (Element) list.item((list.getLength()) - 1);
            String startTime = first.getAttribute("modeltime");
            String stopTime = last.getAttribute("modeltime");
            startTimeField.setValue(startTime);
            stopTimeField.setValue(stopTime);
            tabbedTracePane.add(startTimeField, "min. Time");
            tabbedTracePane.add(stopTimeField, "max. Time");
            Vector EventList = new Vector();
            list = root.getElementsByTagName("event");
            for (int i = 0; i < list.getLength(); i++) {
                Element current = (Element) list.item(i);
                Node text = current.getFirstChild();
                String currentValue = text.getNodeValue();
                if (!EventList.contains(currentValue)) {
                    EventList.addElement(currentValue);
                    eventBox.addItem(currentValue);
                }
            }
            EventList.removeAllElements();
            tabbedTracePane.add(eventBox, "events");
            Vector entityList = new Vector();
            list = root.getElementsByTagName("entity");
            for (int i = 0; i < list.getLength(); i++) {
                Element current = (Element) list.item(i);
                Node text = current.getFirstChild();
                String currentValue = text.getNodeValue();
                if (!entityList.contains(currentValue)) {
                    entityList.addElement(currentValue);
                    entityBox.addItem(currentValue);
                }
            }
            entityList.removeAllElements();
            tabbedTracePane.add(entityBox, "Entities");
            applyTraceSettings.setVisible(true);
            loadTraceSettings.setVisible(true);
            saveTraceSettings.setVisible(true);
            tabbedTracePane.setVisible(true);
        }
    }

    /**
     * updates reporterPanel showing all reporters of the current report and there parameters
     */
    private void initializeReportPanel(String xmlReportFilename) {
        tabbedReportPane.removeAll();
        Document doc;
        try {
            doc = DocumentReader.getInstance().readDoc(
                xmlReportFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }
        Element root = doc.getDocumentElement();
        if (root.getNodeName() != "report") {
            JLabel errorMsg = new JLabel(xmlReportFilename
                + " is not a valid xml-Report.");
            tabbedReportPane.add(errorMsg, "Error");
        } else {
            NodeList list = root.getElementsByTagName("reporter");
            for (int i = 0; i < list.getLength(); i++) {
                Element current = (Element) list.item(i);
                String currentValue = current.getAttribute("type");
                // JLabel reporterLabel = new JLabel(currentValue);  // Inserted comment since unused (JG, 11.03.09)
                Element firstItem = (Element) current.getElementsByTagName(
                    "item").item(0);
                NodeList params = firstItem.getElementsByTagName("param");
                JPanel paramsPanel = new JPanel();
                paramsPanel.setLayout(new GridLayout(7, 3));
                for (int j = 0; j < params.getLength(); j++) {
                    Element currentParam = (Element) params.item(j);
                    if (currentParam.getParentNode().getParentNode()
                        .getNodeName() == "reporter") {
                        paramsPanel.add(new JCheckBox(currentParam
                            .getAttribute("name")));
                    }
                }
                JPanel reporterPanel = new JPanel();
                JCheckBox reporterType = new JCheckBox("show "
                    + currentValue + " in report");
                reporterType.setBackground(Color.white);
                reporterPanel.setLayout(new BorderLayout());
                reporterPanel.add(reporterType, BorderLayout.NORTH);
                reporterPanel.add(paramsPanel, BorderLayout.CENTER);
                reporterPanel.setBorder(new LineBorder(Color.black));
                tabbedReportPane.add(reporterPanel, currentValue);
                applyReportSettings.setVisible(true);
                loadReportSettings.setVisible(true);
                saveReportSettings.setVisible(true);
                tabbedReportPane.setVisible(true);
                setReportXSLFile.setVisible(true);
            }
        }
    }

    private void hideTracePanel() {
        applyTraceSettings.setVisible(false);
        loadTraceSettings.setVisible(false);
        saveTraceSettings.setVisible(false);
        tabbedTracePane.setVisible(false);
    }

    private void hideReportPanel() {
        applyReportSettings.setVisible(false);
        loadReportSettings.setVisible(false);
        saveReportSettings.setVisible(false);
        tabbedReportPane.setVisible(false);
        setReportXSLFile.setVisible(false);
    }

    /**
     * writeTraceParameters gets the data from the TracePanel. A DOM-tree with the data is constructed and then
     * serialized and written into traceParamXSLFilename
     */
    private void writeTraceParameters() {
        writeTraceParameters(traceParamXSLFilename);
    }

    private void writeTraceParameters(String filename) {
        JFormattedTextField minimum = (JFormattedTextField) tabbedTracePane
            .getComponent(0);
        String minTime = (String) minimum.getValue();
        JFormattedTextField maximum = (JFormattedTextField) tabbedTracePane
            .getComponent(1);
        String maxTime = (String) maximum.getValue();
        JComboBox eventBox = (JComboBox) tabbedTracePane.getComponent(2);
        String event = (String) eventBox.getSelectedItem();
        if (event == "all events") {
            event = "";
        }
        JComboBox entityBox = (JComboBox) tabbedTracePane.getComponent(3);
        String entity = (String) entityBox.getSelectedItem();
        if (entity == "all entities") {
            entity = "";
        }
        Document document = XMLHelper.createDocument();
        Element root = document.createElement("xsl:stylesheet");
        root.setAttribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform");
        root.setAttribute("version", "1.0");
        document.appendChild(root);
        Element min = document.createElement("xsl:param");
        min.setAttribute("name", "minimum");
        min.setAttribute("select", minTime);
        root.appendChild(min);
        Element max = document.createElement("xsl:param");
        max.setAttribute("name", "maximum");
        max.setAttribute("select", maxTime);
        root.appendChild(max);
        Element ent = document.createElement("xsl:param");
        ent.setAttribute("name", "entityname");
        ent.setAttribute("select", "'" + entity + "'");
        root.appendChild(ent);
        Element eve = document.createElement("xsl:param");
        eve.setAttribute("name", "eventname");
        eve.setAttribute("select", "'" + event + "'");
        root.appendChild(eve);
        try {
            XMLHelper.serializeDocument(document, new FileWriter(filename));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * writeReportParameters gets the data from the ReportPanel. A DOM-tree with the data is constructed and then
     * serialized and written into reportParamXSLFilename
     */
    private void writeReportParameters() {
        writeReportParameters(reportParamXSLFilename);
    }

    private void writeReportParameters(String filename) {
        Document document = XMLHelper.createDocument();
        Element root = document.createElement("xsl:stylesheet");
        root.setAttribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform");
        root.setAttribute("version", "1.0");
        int reporterCount = 0;
        for (int i = 1; i <= tabbedReportPane.getComponentCount(); i++) {
            JPanel reporterPanel = (JPanel) tabbedReportPane
                .getComponent(i - 1);
            JCheckBox reporterType = (JCheckBox) reporterPanel.getComponent(0);
            if (reporterType.isSelected()) {
                reporterCount++;
                Element reporter = document.createElement("xsl:param");
                reporter.setAttribute("name", "reporter" + reporterCount);
                String repType = "'" + reporterType.getText() + "'";
                repType = repType.replaceFirst("show ", "");
                repType = repType.replaceFirst(" in report", "");
                reporter.setAttribute("select", repType);
                root.appendChild(reporter);
                JPanel paramPanel = (JPanel) reporterPanel.getComponent(1);
                int paramCount = 0;
                for (int j = 1; j <= paramPanel.getComponentCount(); j++) {
                    JCheckBox parameter = (JCheckBox) paramPanel
                        .getComponent(j - 1);
                    if (parameter.isSelected()) {
                        paramCount++;
                        Element param = document.createElement("xsl:param");
                        param.setAttribute("name", "reporter" + reporterCount
                            + "Param" + paramCount);
                        param.setAttribute("select", "'" + parameter.getText()
                            + "'");
                        root.appendChild(param);
                    }
                }
                Element allParam = document.createElement("xsl:param");
                allParam.setAttribute("name", "reporter" + reporterCount
                    + "allParam");
                if (paramCount == 0) {
                    allParam.setAttribute("select", "1");
                } else {
                    allParam.setAttribute("select", "0");
                }
                root.appendChild(allParam);
                if (paramCount < maxParameters) {
                    for (int j = 0; j < (maxParameters - paramCount); j++) {
                        Element param = document.createElement("xsl:param");
                        param.setAttribute("name", "reporter" + reporterCount
                            + "Param" + (paramCount + j + 1));
                        param.setAttribute("select", "''");
                        root.appendChild(param);
                    }
                }
            }
        }
        Element allReporter = document.createElement("xsl:param");
        allReporter.setAttribute("name", "allReporters");
        if (reporterCount == 0) {
            allReporter.setAttribute("select", "1");
        } else {
            allReporter.setAttribute("select", "0");
        }
        if (reporterCount < maxReporters) {
            int repC = reporterCount;
            for (int i = 0; i < (maxReporters - reporterCount); i++) {
                repC++;
                Element reporter = document.createElement("xsl:param");
                reporter.setAttribute("name", "reporter" + repC);
                reporter.setAttribute("select", "''");
                root.appendChild(reporter);
                int paramCount = 0;
                for (int j = 0; j < maxParameters; j++) {
                    paramCount++;
                    Element param = document.createElement("xsl:param");
                    param.setAttribute("name", "reporter" + repC + "Param"
                        + (paramCount));
                    param.setAttribute("select", "''");
                    root.appendChild(param);
                }
                Element param = document.createElement("xsl:param");
                param.setAttribute("name", "reporter" + repC + "allParam");
                param.setAttribute("select", "1");
                root.appendChild(param);
            }
        }
        root.appendChild(allReporter);
        document.appendChild(root);
        try {
            XMLHelper.serializeDocument(document, new FileWriter(filename));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * load the Trace Parameters from traceParamXSLFilename to be selected in the TracePanel
     */
    private void loadTraceParameters() {
        loadTraceParameters(traceParamXSLFilename);
    }

    private void loadTraceParameters(String xslFilename) {
        Document doc = null;
        try {
            doc = DocumentReader.getInstance().readDoc(xslFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }
        Element root = doc.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName("xsl:param");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element current = (Element) nodeList.item(i);
            if (current.getAttribute("name").equals("minimum")) {
                startTimeField.setText(current.getAttribute("select"));
            }
            if (current.getAttribute("name").equals("maximum")) {
                stopTimeField.setText(current.getAttribute("select"));
            }
            if (current.getAttribute("name").equals("entityname")) {
                if (current.getAttribute("select").equals("''")) {
                    entityBox.setSelectedItem("all entities");
                } else {
                    for (int j = 0; j < entityBox.getItemCount(); j++) {
                        if (current.getAttribute("select").equals(
                            "'" + entityBox.getItemAt(j) + "'")) {
                            entityBox.setSelectedIndex(j);
                        }
                    }
                }
            }
            if (current.getAttribute("name").equals("eventname")) {
                if (current.getAttribute("select").equals("''")) {
                    eventBox.setSelectedItem("all events");
                } else {
                    for (int j = 0; j < eventBox.getItemCount(); j++) {
                        if (current.getAttribute("select").equals(
                            "'" + eventBox.getItemAt(j) + "'")) {
                            eventBox.setSelectedIndex(j);
                        }
                    }
                }
            }
        }
    }

    /**
     * load the Report Parameters from reportParamXSLFilename to be selected in the ReportPanel
     */
    private void loadReportParameters() {
        loadReportParameters(reportParamXSLFilename);
    }

    private void loadReportParameters(String xslFilename) {
        Document doc = null;
        try {
            doc = DocumentReader.getInstance().readDoc(xslFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }
        Element root = doc.getDocumentElement();
        NodeList nodeList = root.getElementsByTagName("xsl:param");
        Vector parameterValues = new Vector();
        Vector parameterVector = new Vector();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element xslParam = (Element) nodeList.item(i);
            parameterValues.addElement(xslParam.getAttribute("select"));
            parameterVector.addElement(xslParam.getAttribute("name") + ":"
                + xslParam.getAttribute("select"));
        }
        String allParameters = parameterVector.toString();
        for (int i = 0; i < tabbedReportPane.getComponentCount(); i++) {
            JPanel reporterPanel = (JPanel) tabbedReportPane
                .getComponent(i);
            JCheckBox reporterType = (JCheckBox) reporterPanel
                .getComponent(0);
            /** check the boxes of selected reporters * */
            String repType = "'" + reporterType.getText() + "'";
            repType = repType.replaceFirst("show ", "");
            repType = repType.replaceFirst(" in report", "");
            if (parameterValues.contains(repType)) {
                reporterType.setSelected(true);
                String reporterNo = (String) parameterVector
                    .elementAt(parameterValues.indexOf(repType));
                reporterNo = reporterNo.split(":")[0];
                JPanel paramPanel = (JPanel) reporterPanel.getComponent(1);
                for (int j = 0; j < paramPanel.getComponentCount(); j++) {
                    JCheckBox param = (JCheckBox) paramPanel
                        .getComponent(j);
                    /** check all selected parameters * */
                    /** uncheck none selected parameters* */
                    if (Pattern.matches(".*" + reporterNo + "Param[\\d]+"
                            + ":'\\Q" + param.getText() + "\\E'.*",
                        allParameters)) {
                        param.setSelected(true);
                        /** if all parameters are selected check all * */
                    } else param.setSelected(parameterVector.contains(reporterNo
                        + "allParam:1"));
                }
                /**
                 * if all Reporters are selected check all reporter and all
                 * their parameters *
                 */
            } else if (parameterVector.contains("allReporters:1")) {
                reporterType.setSelected(true);
                JPanel paramPanel = (JPanel) reporterPanel.getComponent(1);
                for (int j = 0; j < paramPanel.getComponentCount(); j++) {
                    JCheckBox param = (JCheckBox) paramPanel
                        .getComponent(j);
                    param.setSelected(true);
                }
                /**
                 * if a reporter is not selected uncheck its box and all its
                 * parameters *
                 */
            } else {
                reporterType.setSelected(false);
                JPanel paramPanel = (JPanel) reporterPanel.getComponent(1);
                for (int j = 0; j < paramPanel.getComponentCount(); j++) {
                    JCheckBox param = (JCheckBox) paramPanel
                        .getComponent(j);
                    param.setSelected(false);
                }
            }
        }
    }

    /***************************************************************************
     * opens a swing dialog to load an XSL file
     *
     * @param currentFilename
     *            String: the filename of the XSL file in use
     **************************************************************************/
    public String loadDialog(String currentFilename) {
        String filename = currentFilename;
        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new XSLFilter());
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

    /***************************************************************************
     * opens a swing dialog to save an XSL file
     *
     * @param currentFilename
     *            String: the filename of the XSL file in use
     **************************************************************************/
    public String saveDialog(String currentFilename) {
        String filename = null;
        JFileChooser jfc = new JFileChooser();
        jfc.addChoosableFileFilter(new XSLFilter());
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

}