package desmoj.extensions.grafic.util;


import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Histogram;
import desmoj.core.statistic.HistogramAccumulate;
import desmoj.core.statistic.TimeSeries;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


/**
 * Class to plot DesmoJ histogram, histogramAccumulater and time-series data in jFreeChart Plotter. When in DesmoJ
 * dataset getShowTimeSpansInReport() is set, the data values are interpreted as a timespan in a appropriate time unit.
 * The DesmoJ datasets are converted in jFreeChart Format. Onscreen and offscreen plots are supported.
 * <p>
 * See also PaintPanel and TimeSeriesDataSetAdapter
 *
 * @author christian.mueller@th-wildau.de and goebel@informatik.uni-hamburg.de modified at 4.12.2012 by
 *     christian.mueller@th-wildau.de
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class Plotter {

    public static final int TimeSeries_ScatterPlot = 0;
    public static final int TimeSeries_StepChart = 1;
    public static final int TimeSeries_LineChart = 2;

    private final PaintPanel paintPanel;
    private boolean onScreen;
    private TimeZone timeZone;
    private Locale locale;
    private Date begin;    /* begin of timeseries */
    private Date end;    /* end of timeseries */
    private final FontRenderContext frc;

    /**
     * Constructor to set the path of output directory and the size of created image. Default are onScreen = false,
     * locale = Locale.getDefault, timeZone = TimeZone.getdefault
     *
     * @param path
     * @param size
     */
    public Plotter(String path, Dimension size) {
        this.paintPanel = new PaintPanel(path, size);
        this.onScreen = false;
        this.locale = Locale.getDefault();
        this.timeZone = TimeZone.getDefault();
        this.begin = null;
        this.end = null;
        this.frc = new FontRenderContext(new AffineTransform(), true, true);
    }

    /**
     * Grafic is shown on screen, stored on file otherwise
     *
     * @param onscreen
     */
    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
    }

    /**
     * set locale for both grafic axis
     *
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * set timeZone for timeseries dateaxis
     *
     * @param timeZone
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * set date range for timeseries dateaxis. When null its automaticly configured by observations.
     *
     * @param begin
     * @param end
     */
    public void setTimeRange(TimeInstant begin, TimeInstant end) {
        this.begin = null;
        if (begin != null) {
            this.begin = new Date(new Double(begin.getTimeAsDouble(TimeUnit.MILLISECONDS)).longValue());
        }

        this.end = null;
        if (end != null) {
            this.end = new Date(new Double(end.getTimeAsDouble(TimeUnit.MILLISECONDS)).longValue());
        }
    }

    /**
     * make a histogram plot with a desmoJ histogram dataset. Deprecated, use setOnScreen(onScreen);
     * makeHistogramPlot(histogram); In the case histogram.getShowTimeSpansInReport() the data values are interpreted as
     * a timespan in a appropriate time unit.
     *
     * @param histogram
     * @param onScreen
     */
    @Deprecated
    public void makeHistogramPlot(Histogram histogram, boolean onscreen) {
        boolean onScreen = this.onScreen;
        this.setOnScreen(onscreen);
        this.makeHistogramPlot(histogram);
        this.setOnScreen(onScreen);
    }

    /**
     * make a histogram plot with a desmoJ histogram dataset. In the case histogram.getShowTimeSpansInReport() the data
     * values are interpreted as a timespan in a appropriate time unit.
     *
     * @param histogram
     */
    public void makeHistogramPlot(Histogram histogram) {
        if (this.onScreen) {
            paintPanel.show(this.getHistogramPlot(histogram), histogram.getName());
        } else {
            paintPanel.save(this.getHistogramPlot(histogram), histogram.getName());
        }
    }

    /**
     * make a histogramAccumulate plot with a desmoJ histogram dataset. In the case histogram.getShowTimeSpansInReport()
     * the data values are interpreted as a timespan in a appropriate time unit.
     *
     * @param histogram
     */
    public void makeHistogramAccumulatePlot(HistogramAccumulate histogram) {
        if (this.onScreen) {
            paintPanel.show(this.getHistogramAccumulatePlot(histogram), histogram.getName());
        } else {
            paintPanel.save(this.getHistogramAccumulatePlot(histogram), histogram.getName());
        }
    }

    /**
     * make a time-series plot with a desmoJ time-series dataset. Deprecated, use setOnScreen(onScreen);
     * makeTimeSeriesPlot(ts, Plotter.TimeSeries_ScatterPlot, true); In the case histogram.getShowTimeSpansInReport()
     * the data values are interpreted as a timespan in a appropriate time unit.
     *
     * @param ts       DesmoJ time-series
     * @param onscreen Grafic is shown on screen, stored on file otherwise
     */
    @Deprecated
    public void makeTimeSeriesPlot(TimeSeries ts, boolean onscreen) {
        boolean onScreen = this.onScreen;
        this.setOnScreen(onscreen);
        this.makeTimeSeriesPlot(ts, Plotter.TimeSeries_ScatterPlot, true);
        this.setOnScreen(onScreen);
    }

    /**
     * make a time-series plot with a desmoJ time-series dataset. In the case ts.getShowTimeSpansInReport() the data
     * values are interpreted as a timespan in a appropriate time unit.
     *
     * @param ts             DesmoJ time-series
     * @param plotType       possible Values: Plotter.TimeSeries_ScatterPlot, Plotter.TimeSeries_StepChart
     *                       Plotter.TimeSeries_LinePlot
     * @param multipleValues When multipleValues is set, multiple range values of a time value are allowed. In the
     *                       opposite case only the last range value of a time value is accepted.
     */
    public void makeTimeSeriesPlot(TimeSeries ts, int plotType, boolean multipleValues) {
        if (this.onScreen) {
            paintPanel.show(this.getTimeSeriesPanel(ts, plotType, multipleValues), ts.getName());
        } else {
            paintPanel.save(this.getTimeSeriesPanel(ts, plotType, multipleValues), ts.getName());
        }
    }

    /**
     * Build a JPanel with a histogram plot of a desmoJ histogram dataset In the case
     * histogram.getShowTimeSpansInReport() the data values are interpreted as a timespan in a appropriate time unit.
     *
     * @param histogram desmoJ histogram dataset
     * @return
     */
    private JPanel getHistogramPlot(Histogram histogram) {
        JFreeChart chart;
        NumberFormat formatter = NumberFormat.getInstance(locale);
        HistogramDataSetAdapter dataSet = new HistogramDataSetAdapter(histogram, locale);
        String title = histogram.getName();
        if (histogram.getDescription() != null) {
            title = histogram.getDescription();
        }
        String xLabel = dataSet.getCategoryAxisLabel();
        String yLabel = dataSet.getObservationAxisLabel();
        chart =
            ChartFactory.createBarChart(title, xLabel, yLabel, dataSet, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.white);

        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
        categoryplot.setBackgroundPaint(Color.lightGray);
        categoryplot.setRangeGridlinePaint(Color.white);
        categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
        barrenderer.setDefaultItemLabelsVisible(true);
        StandardCategoryItemLabelGenerator generator =
            new StandardCategoryItemLabelGenerator("{2}", formatter);
        barrenderer.setDefaultItemLabelGenerator(generator);

        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        //categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        categoryaxis.setMaximumCategoryLabelLines(4);

        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        //numberaxis.setUpperMargin(0.1D);
        numberaxis.setNumberFormatOverride(formatter);

        return new ChartPanel(chart);
    }

    /**
     * Build a JPanel with a histogram plot of a desmoJ histogramAccumulate dataset In the case
     * histogram.getShowTimeSpansInReport() the data values are interpreted as a timespan in a appropriate time unit.
     *
     * @param histogram desmoJ histogramAccumulate dataset
     * @return
     */
    private JPanel getHistogramAccumulatePlot(HistogramAccumulate histogram) {
        JFreeChart chart;
        NumberFormat formatter = NumberFormat.getInstance(locale);
        HistogramDataSetAdapter dataSet = new HistogramDataSetAdapter(histogram, locale);

        String title = histogram.getName();
        if (histogram.getDescription() != null) {
            title = histogram.getDescription();
        }
        String xLabel = dataSet.getCategoryAxisLabel();
        String yLabel = dataSet.getObservationAxisLabel();
        chart =
            ChartFactory.createBarChart(title, xLabel, yLabel, dataSet, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.white);

        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
        categoryplot.setBackgroundPaint(Color.lightGray);
        categoryplot.setRangeGridlinePaint(Color.white);
        categoryplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BarRenderer barrenderer = (BarRenderer) categoryplot.getRenderer();
        barrenderer.setDefaultItemLabelsVisible(true);
        StandardCategoryItemLabelGenerator generator =
            new StandardCategoryItemLabelGenerator("{2}", formatter);
        barrenderer.setDefaultItemLabelGenerator(generator);

        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        categoryaxis.setMaximumCategoryLabelLines(4);

        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setNumberFormatOverride(formatter);

        return new ChartPanel(chart);
    }


    /**
     * Build a JPanel with plotType of a DesmoJ time-series dataset. When allowMultipleValues is set, multiple range
     * values of a time value are allowed. In the opposite Case only the last range value of a time value is accepted.
     * In the case ts.getShowTimeSpansInReport() the data values are interpreted as a timespan in a appropriate time
     * unit.
     *
     * @param ts                  DesmoJ time-series dataset
     * @param plotType            possible Values: Plotter.TimeSeries_ScatterPlot, Plotter.TimeSeries_StepChart
     *                            Plotter.TimeSeries_LinePlot
     * @param allowMultipleValues
     * @return
     */
    private JPanel getTimeSeriesPanel(TimeSeries ts, int plotType, boolean allowMultipleValues) {
        JFreeChart chart;
        TimeSeriesDataSetAdapter dataset = new TimeSeriesDataSetAdapter(ts, allowMultipleValues);
        switch (plotType) {
            case Plotter.TimeSeries_LineChart:
                chart = ChartFactory.createXYLineChart(ts.getName(), "Time", "Observation", dataset,
                    PlotOrientation.VERTICAL, false, false, false);
                break;
            case Plotter.TimeSeries_ScatterPlot:
                chart = ChartFactory.createScatterPlot(ts.getName(), "Time", "Observation", dataset,
                    PlotOrientation.VERTICAL, false, false, false);
                break;
            case Plotter.TimeSeries_StepChart:
                chart = ChartFactory.createXYStepChart(ts.getName(), "Time", "Observation", dataset,
                    PlotOrientation.VERTICAL, false, false, false);
                break;
            default:
                chart = ChartFactory.createScatterPlot(ts.getName(), "Time", "Observation", dataset,
                    PlotOrientation.VERTICAL, false, false, false);
                break;
        }
        if (ts.getDescription() != null) {
            chart.setTitle(ts.getDescription());
        }

        XYPlot xyplot = (XYPlot) chart.getPlot();
        xyplot.setNoDataMessage("NO DATA");
        if (ts.getShowTimeSpansInReport() && !dataset.isValid()) {
            xyplot.setNoDataMessage("NO VALID TIMESPANS");
        }
        xyplot.setDomainZeroBaselineVisible(false);
        xyplot.setRangeZeroBaselineVisible(false);

        DateAxis dateAxis = new DateAxis();
        xyplot.setDomainAxis(dateAxis);
        this.configureDomainAxis(dateAxis);

        String numberLabel;
        if (!dataset.isValid()) {
            numberLabel = "Unit: invalid";
        } else if (ts.getShowTimeSpansInReport()) {
            numberLabel = "Unit: timespan [" + dataset.getRangeTimeUnit().name() + "]";
        } else if (ts.getUnit() != null) {
            numberLabel = "Unit: [" + ts.getUnit() + "]";
        } else {
            numberLabel = "Unit: unknown";
        }
        NumberAxis numberAxis = new NumberAxis();
        xyplot.setRangeAxis(numberAxis);
        this.configureRangeAxis(numberAxis, numberLabel);

        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
        xylineandshaperenderer.setSeriesOutlinePaint(0, Color.black);
        xylineandshaperenderer.setUseOutlinePaint(true);

        ChartPanel panel = new ChartPanel(chart);
        panel.setVerticalAxisTrace(false);
        panel.setHorizontalAxisTrace(false);
        panel.setPopupMenu(null);
        panel.setDomainZoomable(false);
        panel.setRangeZoomable(false);

        return panel;
    }


    /**
     * configure domainAxis (label, timeZone, locale, tick labels) of time-series chart
     *
     * @param dateAxis
     */
    private void configureDomainAxis(DateAxis dateAxis) {
        if (this.begin != null) {
            dateAxis.setMinimumDate(this.begin);
        }
        if (this.end != null) {
            dateAxis.setMaximumDate(this.end);
        }
        dateAxis.setTimeZone(this.timeZone);

        Date dateMin = dateAxis.getMinimumDate();
        Date dateMax = dateAxis.getMaximumDate();
        //DateFormat formatter = new SimpleDateFormat("d.MM.yyyy HH:mm:ss.SSS");;
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM, locale);
        String label = "Time: " + formatter.format(dateMin) + " .. " + formatter.format(dateMax);
        formatter = new SimpleDateFormat("z");
        label += " " + formatter.format(dateMax);
        dateAxis.setLabel(label);

        TimeInstant max = new TimeInstant(dateMax);
        TimeInstant min = new TimeInstant(dateMin);
        TimeSpan diff = TimeOperations.diff(max, min);

        if (TimeSpan.isLongerOrEqual(diff, new TimeSpan(1, TimeUnit.DAYS))) {
            formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        } else if (TimeSpan.isLongerOrEqual(diff, new TimeSpan(1, TimeUnit.HOURS))) {
            formatter = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        } else if (TimeSpan.isLongerOrEqual(diff, new TimeSpan(1, TimeUnit.MINUTES))) {
            formatter = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
        } else {
            formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        }
        dateAxis.setDateFormatOverride(formatter);
        dateAxis.setVerticalTickLabels(true);
    }

    /**
     * configure range axis ( lowerBound, upperBound, label, format ticks) of time-series chart
     *
     * @param numberAxis
     * @param label
     */
    private void configureRangeAxis(NumberAxis numberAxis, String label) {
        double min = numberAxis.getLowerBound();
        double max = numberAxis.getUpperBound();
        Double delta = 0.01 * (max - min);
        numberAxis.setLowerBound(min - delta);
        numberAxis.setUpperBound(max + delta);

        numberAxis.setLabel(label);

        // format Ticks
        double fontHeight = numberAxis.getTickLabelFont().getLineMetrics("X", this.frc).getHeight();
        double maxTicks = this.paintPanel.getSize().height / fontHeight;
        int digits = Math.max(0, (int) -Math.floor(Math.log10((max - min) / maxTicks)));
        //System.out.println(fontHeight+"  "+digits+"  "+Math.log10((max - min)/ maxTicks));
        NumberFormat formatter = NumberFormat.getNumberInstance(this.locale);
        formatter.setMinimumFractionDigits(digits);
        formatter.setMaximumFractionDigits(digits);
        formatter.setGroupingUsed(true);
        numberAxis.setNumberFormatOverride(formatter);
    }

}
