package desmoj.extensions.experimentation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import desmoj.core.statistic.TimeSeries;
import desmoj.core.util.ExperimentListener;
import desmoj.core.util.SimRunEvent;


/**
 * A simple time series plotter that can be displayed in the experiment launcher.
 *
 * @author Nicolas Knaak
 * @author Philip Joschko
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class TimeSeriesPlotter extends GraphicalObserver implements
    ExperimentListener, ChartOwner, Observer {

    /** The chart panel (window frame) and observers user interface */
    private final ChartPanel myGUI;

    /** The data set to be plotted */
    private TimeSeries[] myData;

    private int[] elements;

    private final String firstlegend;

    /**
     * Creates a new TimeSeries plotter from the given array of TimeSeries objects.
     *
     * @param title     window title
     * @param context   the context to show this observer in
     * @param data      array of TimeSeries data to display
     * @param winWidth  width of window
     * @param winHeight height of window
     */
    public TimeSeriesPlotter(String title, GraphicalObserverContext context,
                             TimeSeries[] data, int winWidth, int winHeight) {
        this(title, context, data[0], winWidth, winHeight);
        for (int i = 1; i < data.length; i++) {
            this.addTimeSeries(data[i]);
        }

    }

    /**
     * Creates a new TimeSeries plotter from the given TimeSeries object. This is the main constructor. The other
     * constructors finally call this one.
     *
     * @param title     window title
     * @param context   the context to show this observer in
     * @param data      TimeSeries to display
     * @param winWidth  width of window
     * @param winHeight height of window
     */
    public TimeSeriesPlotter(String title, GraphicalObserverContext context,
                             TimeSeries data, int winWidth, int winHeight) {
        this(title, context, data, winWidth, winHeight, null);
    }

    /**
     * Creates a new TimeSeries plotter from the given TimeSeries object. This is the main constructor. The other
     * constructors finally call this one.
     *
     * @param title     window title
     * @param context   the context to show this observer in
     * @param data      TimeSeries to display
     * @param winWidth  width of window
     * @param winHeight height of window
     * @param String    legend (only used if further plots are added to this plotter)
     */
    public TimeSeriesPlotter(String title, GraphicalObserverContext context,
                             TimeSeries data, int winWidth, int winHeight, String legend) {
        super(title, context);

        myData = new TimeSeries[1];
        myData[0] = data;
        myData[0].connectToPlotter(this);
        elements = new int[1];
        elements[0] = 0;
        this.firstlegend = legend; // for later usage

        myGUI = new ChartPanel(this, title, "Time", myData[0].getName());
        register();
        setVisible(true);
        setSize(winWidth, winHeight);
    }

    /**
     * Creates a new TimeSeries plotter from the given TimeSeries object.
     *
     * @param title     window title
     * @param context   the context to show this observer in
     * @param data      data of time series to display
     * @param winWidth  width of window
     * @param winHeight height of window
     * @param xLocation horizontal position of window
     * @param yLocation vertical position of window
     */
    public TimeSeriesPlotter(String title, GraphicalObserverContext context,
                             TimeSeries data, int winWidth, int winHeight, int xLocation,
                             int yLocation) {
        this(title, context, data, winWidth, winHeight);
        setLocation(xLocation, yLocation);
    }

    /**
     * Creates a new TimeSeries plotter from the given array of TimeSeries objects.
     *
     * @param title     window title
     * @param context   the context to show this observer in
     * @param data      data of time series to display
     * @param winWidth  width of window
     * @param winHeight height of window
     * @param xLocation horizontal position of window
     * @param yLocation vertical position of window
     */
    public TimeSeriesPlotter(String title, GraphicalObserverContext context,
                             TimeSeries[] data, int winWidth, int winHeight, int xLocation,
                             int yLocation) {
        this(title, context, data, winWidth, winHeight);
        setLocation(xLocation, yLocation);
    }

    /**
     * Adds another TimeSeries statistic object which will be displayed in the chart, using a default legend entry.
     *
     * @param newData Another TimeSeries statistic object which will be displayed in the chart.
     */
    public void addTimeSeries(TimeSeries newData) {
        addTimeSeries(newData, newData.getName());
    }

    /**
     * Adds another TimeSeries statistic object which will be displayed in the chart.
     *
     * @param newData Another TimeSeries statistic object which will be displayed in the chart.
     * @param legend  A short description.     *
     */
    public void addTimeSeries(TimeSeries newData, String legend) {
        int[] old_elements = elements;
        TimeSeries[] old_dataArray = myData;
        int n = old_elements.length;

        if (n == 1) {
            myGUI.addLegend(firstlegend != null ? firstlegend : "default");
            myGUI.set_ylabel("Value");
        }

        elements = new int[n + 1];
        myData = new TimeSeries[n + 1];

        for (int i = 0; i < n; i++) {
            elements[i] = old_elements[i];
            myData[i] = old_dataArray[i];
        }
        elements[n] = 0;
        myData[n] = newData;
        myData[n].connectToPlotter(this);
        myGUI.addLegend(legend);

    }

    /**
     * Returns the plotter's GUI (a JFreeCHartPanel)
     *
     * @return a JFreeChart panel to plot the time series
     */
    public Component getGUI() {
        return myGUI;
    }

    /**
     * Called when the experiment is (re)started. Nothing happens
     *
     * @param e a SimRunEvent
     */
    public void experimentRunning(SimRunEvent e) {
    }

    /**
     * Called when the experiment is stopped. Update of display
     *
     * @param e a SimRunEvent
     */
    public void experimentStopped(SimRunEvent e) {
        myGUI.redrawChart();
    }

    /**
     * Called when the experiment is paused. Update of display
     *
     * @param e a SimRunEvent
     */
    public void experimentPaused(SimRunEvent e) {
        experimentStopped(e);
    }

    /**
     * Draws the values given by the TimeSeries statistic object into the chart. This method is automatically invoked by
     * the chart object, if it has to repaint.
     */
    public void drawChart(Graphics g) {
        double newTime;
        double newData;

        for (int series = 0; series < myData.length; series++) {
            g.setColor(ChartPanel.color(series));
            List<Double> timeValues = myData[series].getTimeValues();
            List<Double> dataValues = myData[series].getDataValues();
            if (timeValues != null && dataValues != null) {
                if (timeValues.size() >= 2) {
                    double lastData = dataValues.get(0);
                    double lastTime = timeValues.get(0);
                    for (int pair = 1; pair < timeValues.size(); pair++) {
                        newData = dataValues.get(pair);
                        newTime = timeValues.get(pair);
                        myGUI.drawLine(g, lastTime, lastData, newTime, lastData);
                        myGUI.drawLine(g, newTime, lastData, newTime, newData);
                        lastData = newData;
                        lastTime = newTime;
                    }
                }
            }
        }
    }

    /**
     * The update method (required by interface 'Observer') will be called, if the TimeSeries produces new values.
     */
    public void update(Observable x, Object y) {
        updatePlotter();
    }

    /**
     * This method updates the chart. The chart will only be repainted if a new scaling is required. If no rescaling is
     * required, then only the new values are painted. Of course this is much faster.
     * <p>
     * Because the TimeSeriesPlotter has no bench marks for scaling at the beginning, you have to call this method at
     * least one time, when the upper and lower limits are known.
     */
    public void updatePlotter() {
        List<Double> dataValues;
        List<Double> timeValues;
        boolean redraw = false;
        for (int series = 0; series < myData.length; series++) {
            dataValues = myData[series].getDataValues();
            timeValues = myData[series].getTimeValues();
            if (dataValues == null || timeValues == null) {
                continue;
            }

            if (elements[series] == 0 && dataValues.size() > 0) {
                myGUI.setMax_x(timeValues.get(0));
                myGUI.setMin_x(timeValues.get(0));
                myGUI.setMax_y(dataValues.get(0));
                myGUI.setMin_y(dataValues.get(0));
                elements[series] = 1;
            }

            for (int i = elements[series]; i < dataValues.size(); i++) {
                redraw = redraw || myGUI.testValue(timeValues.get(i), dataValues.get(i).doubleValue());
            }
            if (myGUI.isShowing()) {
                if (dataValues.size() >= 2) {
                    if (redraw) {
                        myGUI.redrawChart();
                    } else {
                        try {
                            double newData;
                            double newTime;
                            Color color = ChartPanel.color(series);
                            double lastData = dataValues.get(elements[series] - 1).doubleValue();
                            double lastTime = timeValues.get(elements[series] - 1).doubleValue();
                            for (int pair = elements[series]; pair < timeValues.size(); pair++) {
                                newData = dataValues.get(pair).doubleValue();
                                newTime = timeValues.get(pair).doubleValue();
                                myGUI.drawLine(color, lastTime, lastData, newTime, lastData);
                                myGUI.drawLine(color, newTime, lastData, newTime, newData);
                                lastData = newData;
                                lastTime = newTime;
                            }
                        } catch (Exception e) {
                        } // the window was closed while drawing the chart

                    }
                }
            }
            elements[series] = dataValues.size();
        }
    }

    /**
     * A simple means of drawing a TimeSeries without requiring the experimentation GUI by adding a TimeSeriesPlotter to
     * an otherwise empty Frame.
     *
     * @author Nicolas Knaak
     * @author Philip Joschko
     *     <p>
     *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
     *     compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
     *     <p>
     *     Unless required by applicable law or agreed to in writing, software distributed under the License is
     *     distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
     *     the License for the specific language governing permissions and limitations under the License.
     * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
     */
    public static class SimpleTimeSeriesViewer extends javax.swing.JFrame {

        private static final long serialVersionUID = 1L;

        /**
         * Opens a frame containing the plot of a TimeSeries.
         *
         * @param ts The time Series to plot
         */
        public SimpleTimeSeriesViewer(TimeSeries ts) {

            super("Viewer of " + ts.getQuotedName());
            this.setLocation(0, 0);
            this.setSize(600, 400);
            this.setVisible(true);
            ObserverDesktop o = new ObserverDesktop();
            TimeSeriesPlotter tsp = new TimeSeriesPlotter(ts.getName(), o, ts, 300, 300);
            tsp.experimentStopped(null);
            tsp.update();
            tsp.drawChart(getGraphics());
            this.getContentPane().add(o);
            this.setVisible(true);
        }
    }
}