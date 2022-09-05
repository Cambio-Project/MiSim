package desmoj.extensions.visualization2d.engine.modelGrafic;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.model.Statistic;
import desmoj.extensions.visualization2d.engine.viewer.InfoPane;
import desmoj.extensions.visualization2d.engine.viewer.ViewerPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XIntervalSeriesCollection;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * Class to animate a instance of class Statistic. For animation you can choose a default animation type:
 * ANIMATION_LastValue, ANIMATION_TimeValueDiagram, ANIMATION_Histogram. Later, you can change interactively, with a
 * popup menu, between the animation types.
 * <p>
 * For  ANIMATION_TimeValueDiagram and ANIMATION_Histogram the jFreeChart Library is used.
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
public class StatisticGrafic extends JComponent
    implements Grafic, MouseListener, ActionListener {

    public static final int ANIMATION_LastValue = 0;
    public static final int ANIMATION_TimeValueDiagram = 1;
    public static final int ANIMATION_Histogram = 2;
    private static final Color DIAGRAM_BACKGROUND = new Color(240, 240, 240);
    private static final Color DIAGRAM_FORGROUND = Color.blue;
    private static final Color DIAGRAM_BORDER = Color.black;
    private static final Color DIAGRAM_GRID = Color.black;
    private static final Color DIAGRAM_MARKER_MIN_MAX = Color.green;
    private static final Color DIAGRAM_MARKER_MEAN = Color.yellow;
    private static final Color DIAGRAM_MARKER_STD_DEV = Color.green;
    private static final String[] TEXT_POPUP_MENU =
        {"Time Value Diagram", "Histogram", "Last Value"};

    private static final long serialVersionUID = 1L;
    private static JComponent statisticPanel;
    private final Statistic statistic;
    private String code;
    private Dimension deltaSize;
    private Point pointIntern;
    private final Point pointExtern;
    private int typeAnimation;
    private final boolean isIntValue;
    private JLabel valueLabel, minLabel, maxLabel,
        meanLabel, stdDevLabel;
    private JFreeChart chart;
    private final String viewId;


    /**
     * Build a StatisticGrafic Instance
     *
     * @param statistic     The associated statistic instance
     * @param viewId        Id of view
     * @param pointExtern   Middle point
     * @param typeAnimation Default animation type. This can changed by popup menu. For animation types look at
     *                      StatisticGrafic.ANIMATION_...
     * @param isIntValue    In typeAnimation == StatisticGrafic.ANIMATION_LastValue value is shown as integer.
     * @param deltaSize     The default size can be incremented/decremented by deltaSize. Null means no change.
     * @param infopane      Grafic is used for infopane
     * @throws ModelException
     */
    public StatisticGrafic(Statistic statistic, String viewId, Point pointExtern,
                           int typeAnimation, boolean isIntValue, Dimension deltaSize, boolean infopane)
        throws ModelException {

        this.statistic = statistic;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        //System.out.println("StatisticGrafic-Konstructor   id: "+this.statistic.getId());
        this.pointExtern = pointExtern;
        this.transform();
        this.setCode();
        this.setBorder(BorderFactory.createTitledBorder(Grafic.Border_Default,
            this.code, TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION, Grafic.FONT_DEFAULT,
            Grafic.COLOR_BORDER));
        this.deltaSize = deltaSize;
        if (this.deltaSize == null) {
            this.deltaSize = new Dimension(0, 0);
        }
        this.typeAnimation = typeAnimation;
        this.isIntValue = isIntValue;
        JPanel content = new JPanel();
        switch (this.typeAnimation) {
            case StatisticGrafic.ANIMATION_LastValue:
                content = this.buildLastValuePanel();
                break;
            case StatisticGrafic.ANIMATION_TimeValueDiagram:
                content = this.buildTimeValueDiagramPanel();
                break;
            case StatisticGrafic.ANIMATION_Histogram:
                if (statistic.hasHistogramSupport()) {
                    content = this.buildHistogramPanel();
                } else {
                    this.typeAnimation = StatisticGrafic.ANIMATION_TimeValueDiagram;
                    content = this.buildTimeValueDiagramPanel();
                }
                break;
        }
        this.setLayout(new GridLayout(1, 1));
        this.add(content);
        this.setOpaque(true);
        Dimension d = new Dimension(
            content.getPreferredSize().width + this.deltaSize.width,
            content.getPreferredSize().height + this.deltaSize.height);
        this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        this.update();
        // Listener hinzufuegen
        if (this.statistic.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.statistic.getModel().getCoordinatenListener());
            this.addMouseListener(this.statistic.getModel().getCoordinatenListener());
            if (!infopane && this.typeAnimation != StatisticGrafic.ANIMATION_LastValue) {
                content.addMouseMotionListener(this.statistic.getModel().getCoordinatenListener());
                content.addMouseListener(this.statistic.getModel().getCoordinatenListener());
            }
        }
        this.addMouseListener(this);
    }

    /**
     * get all views (viewId's) with Statistic
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getStatistics().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Statistic statistic = model.getStatistics().get(ids[i]);
            StatisticGrafic statisticGrafic = (StatisticGrafic) statistic.getGrafic();
            if (statisticGrafic != null) {
                String viewId = statisticGrafic.getViewId();
                views.add(viewId);
            }
        }
        String[] out = new String[views.size()];
        int i = 0;
        for (Iterator<String> it = views.iterator(); it.hasNext(); ) {
            out[i] = it.next();
            i++;
        }
        return out;

    }

    /**
     * Construct the smallest rectangle, which include the positions of all statistics in view
     *
     * @param viewId id of view
     * @return smallest Rectangle when an StatisticGrafic exist, null otherwise.
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        double minX = (double) Integer.MAX_VALUE / 2;
        double minY = (double) Integer.MAX_VALUE / 2;
        double maxX = (double) Integer.MIN_VALUE / 2;
        double maxY = (double) Integer.MIN_VALUE / 2;
        String[] id = model.getStatistics().getAllIds();
        //System.out.println("Anz. Entities: "+id.length);
        for (int i = 0; i < id.length; i++) {
            Statistic statistic = model.getStatistics().get(id[i]);
            StatisticGrafic statistikGrafic = (StatisticGrafic) statistic.getGrafic();
            if (statistikGrafic != null &&
                statistikGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = statistikGrafic.getBoundsExtern();
                minX = Math.floor(Math.min(minX, r.getX()));
                minY = Math.floor(Math.min(minY, r.getY()));
                maxX = Math.ceil(Math.max(maxX, r.getX() + r.width));
                maxY = Math.ceil(Math.max(maxY, r.getY() + r.height));
                //System.out.println(statistic.getId()+"  "+statistikGrafic.pointExtern.getX()+" "+statistikGrafic.pointExtern.getY());
            }
        }
        Rectangle r = null;
        if (found) {
            r = new Rectangle((int) Math.round(minX), (int) Math.round(minY), (int) Math.round(maxX - minX),
                (int) Math.round(maxY - minY));
        }
        //System.out.println("StatisticGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * Put all StatisticGrafic instances into panel. Store panel as StatisticGrafic.statisticPanel.
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        //System.out.println("StatisticGrafic.updateInit   ");
        StatisticGrafic.statisticPanel = panel;
        panel.removeAll();
        String[] id = model.getStatistics().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Statistic statistik = model.getStatistics().get(id[i]);
            StatisticGrafic statistikGrafic = (StatisticGrafic) statistik.getGrafic();
            if (statistikGrafic != null &&
                statistikGrafic.getViewId().equals(viewId)) {
                statistikGrafic.transform();
                panel.add(statistikGrafic);
                //System.out.println("StatisticGrafic.updateInit   "+statistik.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    /**
     * Build content for animationType StatisticGrafic.ANIMATION_LastValue
     *
     * @return
     */
    private JPanel buildLastValuePanel() {
        JPanel out = new JPanel();
        out.setBackground(Grafic.COLOR_BACKGROUND);
        out.setOpaque(true);
        out.setLayout(new BorderLayout());
        switch (this.statistic.getTypeIndex()) {
            case Statistic.INDEX_None:
                this.valueLabel = new JLabel();
                this.valueLabel.setFont(Grafic.FONT_BIG);
                this.valueLabel.setForeground(Grafic.COLOR_FOREGROUND);
                out.add(this.valueLabel, BorderLayout.CENTER);
                if (this.isIntValue) {
                    out.setPreferredSize(new Dimension(80, 50));
                } else {
                    out.setPreferredSize(new Dimension(100, 50));
                }
                break;
            case Statistic.INDEX_Min_Max:
                this.valueLabel = new JLabel();
                this.valueLabel.setFont(Grafic.FONT_BIG);
                this.valueLabel.setForeground(Grafic.COLOR_FOREGROUND);
                out.add(this.valueLabel, BorderLayout.CENTER);
                Box top = Box.createVerticalBox();
                this.minLabel = new JLabel();
                this.minLabel.setFont(Grafic.FONT_DEFAULT);
                this.minLabel.setForeground(Grafic.COLOR_FOREGROUND);
                top.add(this.minLabel);
                this.maxLabel = new JLabel();
                this.maxLabel.setFont(Grafic.FONT_DEFAULT);
                this.maxLabel.setForeground(Grafic.COLOR_FOREGROUND);
                top.add(this.maxLabel);
                out.add(top, BorderLayout.SOUTH);
                if (this.isIntValue) {
                    out.setPreferredSize(new Dimension(80, 70));
                } else {
                    out.setPreferredSize(new Dimension(100, 70));
                }
                break;
            case Statistic.INDEX_Mean_StdDev:
                this.valueLabel = new JLabel();
                this.valueLabel.setFont(Grafic.FONT_BIG);
                this.valueLabel.setForeground(Grafic.COLOR_FOREGROUND);
                out.add(this.valueLabel, BorderLayout.CENTER);
                Box bottom = Box.createVerticalBox();
                this.meanLabel = new JLabel();
                this.meanLabel.setFont(Grafic.FONT_DEFAULT);
                this.meanLabel.setForeground(Grafic.COLOR_FOREGROUND);
                bottom.add(this.meanLabel);
                this.stdDevLabel = new JLabel();
                this.stdDevLabel.setFont(Grafic.FONT_DEFAULT);
                this.stdDevLabel.setForeground(Grafic.COLOR_FOREGROUND);
                bottom.add(this.stdDevLabel);
                out.add(bottom, BorderLayout.SOUTH);
                if (this.isIntValue) {
                    out.setPreferredSize(new Dimension(80, 70));
                } else {
                    out.setPreferredSize(new Dimension(100, 70));
                }
                break;
        }
        return out;
    }

    /**
     * Build content for animationType StatisticGrafic.ANIMATION_TimeValueDiagram
     *
     * @return
     * @throws ModelException
     */
    private JPanel buildTimeValueDiagramPanel() throws ModelException {
        JPanel out = null;
        XYPlot plot = null;
        switch (this.statistic.getTypeData()) {
            case Statistic.DATA_Observations:
                XYSeriesCollection dataset1 = new XYSeriesCollection();
                dataset1.addSeries(this.statistic.getObservationSerie());
                this.chart = ChartFactory.createScatterPlot(null, "Time", null,
                    dataset1, PlotOrientation.VERTICAL, false, true, false);
                this.chart.setBackgroundPaint(Grafic.COLOR_BACKGROUND);
                plot = this.chart.getXYPlot();
                break;
            case Statistic.DATA_TimeSeries:
                TimeSeriesCollection dataset2 = new TimeSeriesCollection();
                dataset2.addSeries(this.statistic.getTimeSerie());
                this.chart = ChartFactory.createXYStepChart(null, "Time", null,
                    dataset2, PlotOrientation.VERTICAL, false, true, false);
                this.chart.setBackgroundPaint(Grafic.COLOR_BACKGROUND);
                plot = this.chart.getXYPlot();
                break;
        }
        if (plot != null) {
            plot.setBackgroundPaint(StatisticGrafic.DIAGRAM_BACKGROUND);
            plot.setDomainGridlinePaint(StatisticGrafic.DIAGRAM_GRID);
            plot.setRangeGridlinePaint(StatisticGrafic.DIAGRAM_GRID);
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);
            String rangeAxisLabel = "";
            switch (statistic.getTypeIndex()) {
                case Statistic.INDEX_Min_Max:
                    rangeAxisLabel = "min - max";
                    break;
                case Statistic.INDEX_Mean_StdDev:
                    rangeAxisLabel = "\u03BC-\u03C3 - mean - \u03BC+\u03C3";
                    break;
            }
            //this.buildTimeValueDiagramAxisFormat(plot, rangeAxisLabel);
            plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
            plot.getRenderer().setSeriesPaint(0, StatisticGrafic.DIAGRAM_FORGROUND);

        }
        out = new ChartPanel(chart);
        out.setPreferredSize(new Dimension(350, 200));
        return out;
    }

    /**
     * Build content for animationType StatisticGrafic.ANIMATION_Histogram
     *
     * @return
     * @throws ModelException
     */
    private JPanel buildHistogramPanel() throws ModelException {
        XIntervalSeriesCollection dataset = new XIntervalSeriesCollection();
        dataset.addSeries(this.statistic.getHistogram());
        this.chart = ChartFactory.createXYBarChart(null, "Observation", false,
            "Count", dataset, PlotOrientation.VERTICAL, false, true, false);
        this.chart.setBackgroundPaint(Grafic.COLOR_BACKGROUND);
        XYPlot plot = this.chart.getXYPlot();
        plot.setBackgroundPaint(StatisticGrafic.DIAGRAM_BACKGROUND);
        plot.setDomainGridlinePaint(StatisticGrafic.DIAGRAM_GRID);
        plot.setRangeGridlinePaint(StatisticGrafic.DIAGRAM_GRID);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        ValueAxis rangeAxis = (ValueAxis) plot.getRangeAxis();
        rangeAxis.setLabelFont(Grafic.FONT_DEFAULT);
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(Grafic.FONT_DEFAULT);
        domainAxis.setAutoRange(true);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setDrawBarOutline(true);
        renderer.setSeriesPaint(0, StatisticGrafic.DIAGRAM_FORGROUND);
        renderer.setSeriesOutlinePaint(0, StatisticGrafic.DIAGRAM_BORDER);
        renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));

        this.buildHistogramAxisFormat(plot, "Observations");
        JPanel out = new ChartPanel(chart);
        out.setPreferredSize(new Dimension(350, 200));
        return out;
    }

    /**
     * updates the StatisticGrafic Instance. Will be called by Statistic.update(....).
     *
     * @throws ModelException
     */
    public void update() throws ModelException {
        switch (this.typeAnimation) {
            case StatisticGrafic.ANIMATION_LastValue:
                this.updateLastValue();
                //System.out.println("update LastValue "+statistic.getId());
                break;
            case StatisticGrafic.ANIMATION_TimeValueDiagram:
                this.updateTimeValueDiagram();
                //System.out.println("update TimeValue "+statistic.getId());
                break;
            case StatisticGrafic.ANIMATION_Histogram:
                this.updateHistogram();
                //System.out.println("update Histogram "+statistic.getId());
                break;
        }
    }

    /**
     * Update of animation type StatisticGrafic.ANIMATION_LastValue
     *
     * @throws ModelException
     */
    private void updateLastValue() throws ModelException {
        switch (this.statistic.getTypeIndex()) {
            case Statistic.INDEX_None:
                if (this.statistic.hasValue()) {
                    this.valueLabel.setText(this.formatieren(this.statistic.getLastValue(), isIntValue));
                } else {
                    this.valueLabel.setText("");
                }
                break;
            case Statistic.INDEX_Min_Max:
                if (this.statistic.hasValue()) {
                    this.valueLabel.setText(this.formatieren(this.statistic.getLastValue(), isIntValue));
                    this.minLabel.setText("min: " + this.formatieren(this.statistic.getMin(), false));
                    this.maxLabel.setText("max: " + this.formatieren(this.statistic.getMax(), false));
                } else {
                    this.valueLabel.setText("");
                    this.minLabel.setText("min: ");
                    this.maxLabel.setText("max: ");
                }
                break;
            case Statistic.INDEX_Mean_StdDev:
                if (this.statistic.hasValue()) {
                    this.valueLabel.setText(this.formatieren(this.statistic.getLastValue(), isIntValue));
                    this.meanLabel.setText("\u03BC: " + this.formatieren(this.statistic.getMean(), false));
                    this.stdDevLabel.setText("\u03C3: " + this.formatieren(this.statistic.getStdDev(), false));
                } else {
                    this.valueLabel.setText("");
                    this.meanLabel.setText("\u03BC: ");
                    this.stdDevLabel.setText("\u03C3: ");
                }
                break;
        }
    }


    /**
     * Update of animation type StatisticGrafic.ANIMATION_TimeValueDiagram
     *
     * @throws ModelException
     */
    private void updateTimeValueDiagram() throws ModelException {
        Marker meanMarker, minMarker, maxMarker;
        double mean, min0, max0;
        XYPlot plot = this.chart.getXYPlot();
        String rangeAxisLabel = "";
        switch (this.statistic.getTypeIndex()) {
            case Statistic.INDEX_None:
                break;
            case Statistic.INDEX_Min_Max:
                plot.clearRangeMarkers();
                min0 = statistic.getMin();
                max0 = statistic.getMax();
                if (!Double.isNaN(min0)) {
                    minMarker = new ValueMarker(min0);
                    minMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MIN_MAX);
                    minMarker.setStroke(new BasicStroke(2.0f));
                    plot.addRangeMarker(minMarker, Layer.BACKGROUND);
                }
                if (!Double.isNaN(max0)) {
                    maxMarker = new ValueMarker(max0);
                    maxMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MIN_MAX);
                    maxMarker.setStroke(new BasicStroke(2.0f));
                    plot.addRangeMarker(maxMarker, Layer.BACKGROUND);
                }
                rangeAxisLabel = "min - max";
                break;
            case Statistic.INDEX_Mean_StdDev:
                plot.clearRangeMarkers();
                if (statistic.hasValue()) {
                    min0 = statistic.getMean() - statistic.getStdDev();
                    max0 = statistic.getMean() + statistic.getStdDev();
                    mean = statistic.getMean();
                    if (!Double.isNaN(mean)) {
                        meanMarker = new ValueMarker(mean);
                        meanMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MEAN);
                        meanMarker.setStroke(new BasicStroke(2.0f));
                        plot.addRangeMarker(meanMarker, Layer.BACKGROUND);
                    }
                    if (!Double.isNaN(min0)) {
                        minMarker = new ValueMarker(min0);
                        minMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_STD_DEV);
                        minMarker.setStroke(new BasicStroke(2.0f));
                        plot.addRangeMarker(minMarker, Layer.BACKGROUND);
                    }
                    if (!Double.isNaN(max0)) {
                        maxMarker = new ValueMarker(max0);
                        maxMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_STD_DEV);
                        maxMarker.setStroke(new BasicStroke(2.0f));
                        plot.addRangeMarker(maxMarker, Layer.BACKGROUND);
                    }
                }
                rangeAxisLabel = "\u03BC-\u03C3 - mean - \u03BC+\u03C3";
                break;
        }
        //this.buildTimeValueDiagramAxisFormat(plot, rangeAxisLabel);
    }

    /**
     * Update of animation type StatisticGrafic.ANIMATION_Histogram
     *
     * @throws ModelException
     */
    private void updateHistogram() throws ModelException {
        Marker meanMarker, minMarker, maxMarker;
        double mean, min0, max0;
        XYPlot plot = this.chart.getXYPlot();
        String domainAxisLabel = "Observations  ";
        switch (this.statistic.getTypeIndex()) {
            case Statistic.INDEX_None:
                break;
            case Statistic.INDEX_Min_Max:
                plot.clearDomainMarkers();
                min0 = statistic.getMin();
                max0 = statistic.getMax();
                if (!Double.isNaN(min0)) {
                    minMarker = new ValueMarker(min0);
                    minMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MIN_MAX);
                    minMarker.setStroke(new BasicStroke(2.0f));
                    plot.addDomainMarker(minMarker, Layer.FOREGROUND);
                }
                if (!Double.isNaN(max0)) {
                    maxMarker = new ValueMarker(max0);
                    maxMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MIN_MAX);
                    maxMarker.setStroke(new BasicStroke(2.0f));
                    plot.addDomainMarker(maxMarker, Layer.FOREGROUND);
                }
                domainAxisLabel += "(min - max)";
                break;
            case Statistic.INDEX_Mean_StdDev:
                plot.clearDomainMarkers();
                min0 = statistic.getMean() - statistic.getStdDev();
                max0 = statistic.getMean() + statistic.getStdDev();
                mean = statistic.getMean();
                if (!Double.isNaN(mean)) {
                    meanMarker = new ValueMarker(mean);
                    meanMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_MEAN);
                    meanMarker.setStroke(new BasicStroke(2.0f));
                    plot.addDomainMarker(meanMarker, Layer.FOREGROUND);
                }
                if (!Double.isNaN(min0)) {
                    minMarker = new ValueMarker(min0);
                    minMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_STD_DEV);
                    minMarker.setStroke(new BasicStroke(2.0f));
                    plot.addDomainMarker(minMarker, Layer.FOREGROUND);
                }
                if (!Double.isNaN(max0)) {
                    maxMarker = new ValueMarker(max0);
                    maxMarker.setPaint(StatisticGrafic.DIAGRAM_MARKER_STD_DEV);
                    maxMarker.setStroke(new BasicStroke(2.0f));
                    plot.addDomainMarker(maxMarker, Layer.FOREGROUND);
                }
                domainAxisLabel += "(\u03BC-\u03C3 - mean - \u03BC+\u03C3)";
                break;
        }
        this.buildHistogramAxisFormat(plot, domainAxisLabel);
    }

    /**
     * Transforms pointExtern to point Intern
     */
    public void transform() {
        Point p = statistic.getModel().getModelGrafic().
            transformToIntern(this.viewId, this.pointExtern);
        if (p == null) {
            this.pointIntern = this.pointExtern;
        } else {
            this.pointIntern = p;
        }
        this.setLocation(this.pointIntern.x - this.getWidth() / 2, this.pointIntern.y - this.getHeight() / 2);
        //System.out.println("ListGrafic id: "+list.getId()+"  "+this.pointIntern.toString());
    }

    /**
     * build code to sign the output by name or id.
     */
    private void setCode() {
        if (this.statistic.getName() != null) {
            this.code = this.statistic.getName();
        } else {
            this.code = "id: " + this.statistic.getId();
        }
    }

    /**
     * some jFreeChart Axis settings for TimeValueDiagram
     *
     * @param plot           jFreeChart plot instance
     * @param rangeAxisLabel Label of rangeAxis
     */
    private void buildTimeValueDiagramAxisFormat(XYPlot plot, String rangeAxisLabel) {
        System.out.println("StatisticGrafic.buildTimeValueDiagramAxisFormat");
        ValueAxis rangeAxis = (ValueAxis) plot.getRangeAxis();
        double a = 0.1 * Math.max(Math.abs(statistic.getValueLow()), Math.abs(statistic.getValueHigh()));
        rangeAxis.setLowerBound(statistic.getValueLow() - a);
        rangeAxis.setUpperBound(statistic.getValueHigh() + a);
        rangeAxis.setLabel(rangeAxisLabel);
        rangeAxis.setLabelFont(FONT_DEFAULT);


        DateAxis dateAxis = new DateAxis();
        DateAxis[] domainAxisArray = new DateAxis[1];
        domainAxisArray[0] = dateAxis;
        plot.setDomainAxes(domainAxisArray);

        dateAxis.setLowerBound(statistic.getTimeLow());
        dateAxis.setUpperBound(statistic.getTimeHigh());

        long diff = statistic.getTimeHigh() - statistic.getTimeLow();
        String format, unit;
        if (diff > 24 * 60 * 60 * 1000) {
            format = "d.MM.yyyy";
            unit = "[day]";
        } else if (diff > 60 * 60 * 1000) {
            format = "H:mm";
            unit = "[h]";
        } else if (diff > 60 * 1000) {
            format = "m:ss";
            unit = "[min]";
        } else if (diff > 1000) {
            format = "s.S";
            unit = "[sec]";
        } else {
            format = "S";
            unit = "[millisec]";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        dateAxis.setDateFormatOverride(sdf);
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        String von = sdf1.format(dateAxis.getMinimumDate());
        String bis = sdf1.format(dateAxis.getMaximumDate());
        dateAxis.setLabel(von + "    Time " + unit + "   " + bis);
        dateAxis.setLabelFont(FONT_DEFAULT);

    }

    /**
     * some jFreeChart Axis settings for Histogram
     *
     * @param plot            jFreeChart plot instance
     * @param domainAxisLabel Label of domain Axis
     */
    private void buildHistogramAxisFormat(XYPlot plot, String domainAxisLabel) {
        switch (this.statistic.getTypeData()) {
            case Statistic.DATA_Observations:
                ValueAxis rangeAxis = (ValueAxis) plot.getRangeAxis();
                rangeAxis.setLabel("Count");
                break;
            case Statistic.DATA_TimeSeries:
                DateAxis dateAxis = new DateAxis();
                DateAxis[] rangeAxisArray = new DateAxis[1];
                rangeAxisArray[0] = dateAxis;
                plot.setRangeAxes(rangeAxisArray);
                dateAxis = (DateAxis) plot.getRangeAxis();
                dateAxis.setMinimumDate(new Date(0));
                long diff = dateAxis.getMaximumDate().getTime();
                String format, unit;
                if (diff > 24 * 60 * 60 * 1000) {
                    format = "d.MM.yyyy";
                    unit = "[day]";
                } else if (diff > 60 * 60 * 1000) {
                    format = "H:mm";
                    unit = "[h]";
                } else if (diff > 60 * 1000) {
                    format = "m:ss";
                    unit = "[min]";
                } else if (diff > 1000) {
                    format = "s.S";
                    unit = "[sec]";
                } else {
                    format = "S";
                    unit = "[millisec]";
                }
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                dateAxis.setDateFormatOverride(sdf);
                dateAxis.setLabel("Time " + unit);
                dateAxis.setLabelFont(Grafic.FONT_DEFAULT);
                break;
        }

        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabel(domainAxisLabel);
		/*
		domainAxis.setLowerBound(statistik.getHistogramLow());
		domainAxis.setUpperBound(statistik.getHistogramHigh());
		TickUnits tu = new TickUnits(); tu.add(new NumberTickUnit(statistik.getHistogramIntervalLength()));
		domainAxis.setStandardTickUnits(tu);
		domainAxis.setAutoTickUnitSelection(true);
		domainAxis.setVerticalTickLabels(false);
		*/
    }

    public Rectangle getBoundsExtern() {
        Dimension d = new Dimension(this.getBounds().width, this.getBounds().height);
        return new Rectangle(this.pointExtern.x - d.width / 2,
            this.pointExtern.y - d.height / 2, d.width, d.height);
    }

    /**
     * convert double to String. Used by animation type StatisticGrafic.ANIMATION_LastValue.
     *
     * @param a
     * @return
     */
    private String formatieren(double a, boolean isIntValue) {
        String format = "";
        if (isIntValue) {
            a = Math.rint(a);
            if (Math.abs(a) < 10000.0) {
                format = "#0";
            } else {
                format = "0.E0";
            }
        } else {
            if (Math.abs(a) < 0.001) {
                format = "0.00E0";
            } else if (Math.abs(a) < 10000.0) {
                format = "#0.000";
            } else {
                format = "0.00E0";
            }
        }
        DecimalFormat df = new DecimalFormat(format);
        return df.format(a);
    }

    /**
     * Build the popup menu to switch between typeAnimation. works only when statistic has observations Called by
     * MouseListener Event wird nur bearbeitet, wenn die Simulation angehalten ist Im anderen Fall kann der Viewer
     * (inbes. Applet) ueberlastet sein
     *
     * @param event MouseEvent
     */
    private void checkPopupMenu(MouseEvent event) {
        //System.out.println("StatisticGrafic.checkPopupMenu");
        ViewerPanel viewer = this.statistic.getModel().getViewer();
        if (viewer != null && viewer.getSimulationThread() != null
            && !viewer.getSimulationThread().isWorking()) {
            // Event wird nur bearbeitet, wenn die Simulation angehalten ist
            // Im anderen Fall kann der Viewer (inbes. Applet) ueberlastet sein
            if (event.isPopupTrigger() && this.statistic.hasValue()) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem mi = new JMenuItem(StatisticGrafic.TEXT_POPUP_MENU[0]);
                mi.addActionListener(this);
                popup.add(mi);
                mi = new JMenuItem(StatisticGrafic.TEXT_POPUP_MENU[1]);
                mi.addActionListener(this);
                popup.add(mi);
                popup.show(event.getComponent(), event.getX(), event.getY());
            }
        }
    }

    /**
     * Called by popup menu items to show statistics in InfoPane.
     */
    public void actionPerformed(ActionEvent event) {
        InfoPane infoPane = ViewerPanel.getInfoPane();
        if (infoPane != null) {
            infoPane.setVisible(true);
            if (StatisticGrafic.TEXT_POPUP_MENU[0].equals(event.getActionCommand())) {
                infoPane.addStatistic(this.statistic.getId(), code, StatisticGrafic.ANIMATION_TimeValueDiagram,
                    isIntValue);
            } else if (StatisticGrafic.TEXT_POPUP_MENU[1].equals(event.getActionCommand())) {
                infoPane.addStatistic(this.statistic.getId(), code, StatisticGrafic.ANIMATION_Histogram, isIntValue);
            }
        }
    }

    public void mouseClicked(MouseEvent event) {
        this.checkPopupMenu(event);
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    public void mousePressed(MouseEvent event) {
        this.checkPopupMenu(event);
    }

    public void mouseReleased(MouseEvent event) {
        this.checkPopupMenu(event);
    }

}
