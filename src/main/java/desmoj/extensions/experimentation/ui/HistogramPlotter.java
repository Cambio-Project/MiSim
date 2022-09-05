package desmoj.extensions.experimentation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import desmoj.core.statistic.Histogram;
import desmoj.core.util.ExperimentListener;
import desmoj.core.util.SimRunEvent;

/**
 * A simple histogram plotter based on the statistic class Histogram and the UI class ChartPanel.
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
public class HistogramPlotter extends GraphicalObserver implements
    ExperimentListener, ChartOwner {

    /** The chart panel (window frame) and observers user interface */
    private final ChartPanel myGUI;

    /** Histogram statistic data object from desmoj */
    private final Histogram data;

    /** Boolean value whether to draw the chart or not. */
    private boolean hasToDrawChart;

    /**
     * Creates a new HistogramPlotter
     *
     * @param name      name displayed in window title
     * @param context   graphical observer context the plotter is displayed in
     * @param data      HistogramData consisting of double values to be displayed in the histogram
     * @param xUnit     unit label for x axis
     * @param winWidth  width of window
     * @param winHeight height of window
     */
    public HistogramPlotter(String title, GraphicalObserverContext context,
                            Histogram data, String xUnit, int winWidth, int winHeight) {
        super(title, context);
        this.data = data;

        myGUI = new ChartPanel(this, title, xUnit, "Percentage");
        myGUI.setSize(winWidth, winHeight);
        myGUI.setMax_x(data.getUpperLimit());
        myGUI.setMin_x(data.getLowerLimit(1));
        myGUI.setMax_y(100);
        myGUI.setMin_y(0);
        register();
        setVisible(true);
        setSize(winWidth, winHeight);

        hasToDrawChart = false;
    }

    /**
     * Creates a new HistogramPlotter
     *
     * @param name      name displayed in window title
     * @param context   graphical observer context the plotter is displayed in
     * @param data      HistogramData consisting of double values to be displayed in the histogram
     * @param xUnit     unit label for x axis
     * @param winWidth  width of window
     * @param winHeight height of window
     * @param x         horizontal window location
     * @param y         vertical window location
     */
    public HistogramPlotter(String title, GraphicalObserverContext context,
                            Histogram data, String xUnit, int winWidth, int winHeight,
                            int x, int y) {
        this(title, context, data, xUnit, winWidth, winHeight);
        setLocation(x, y);
    }

    /**
     * @return The GUI component of this observer (a
     *     <code>jfree.org.chart.ChartPanel</code>).
     * @see GraphicalObserver#getGUI()
     */
    public Component getGUI() {
        return myGUI;
    }

    /**
     * Called when experiment is (re)started. Nothing happens here.
     *
     * @param e a SimRunEvent
     * @see desmoj.extensions.experimentation.access.ExperimentListener#experimentRunning(SimRunEvent)
     */
    public void experimentRunning(SimRunEvent e) {
        hasToDrawChart = false;
    }

    /**
     * Called when experiment is stopped. HistogramData is displayed in chart.
     *
     * @param e a SimRunEvent
     * @see desmoj.extensions.experimentation.access.ExperimentListener#experimentStopped(SimRunEvent)
     */
    public void experimentStopped(SimRunEvent e) {
        calculateBars();
    }

    /**
     * Called when experiment is paused. Current HistogramData is displayed in chart until restarting the simulation.
     *
     * @param e a SimRunEvent
     * @see desmoj.extensions.experimentation.access.ExperimentListener#experimentPaused(SimRunEvent)
     */
    public void experimentPaused(SimRunEvent e) {
        calculateBars();
    }

    /**
     * Draws the histogram bars given by the Histogram statistic object into the chart. This method is automatically
     * invoked by the chart object, if it has to repaint.
     */
    public void drawChart(Graphics g) {
        if (hasToDrawChart) {
            double breite = data.getCellWidth();
            double factor = 100.0 / data.getObservations();
            for (int i = 1; i <= data.getCells(); i++) {
                g.setColor(Color.red);
                myGUI.fillRect(g, 0 + (i - 1) * breite, 0, breite, ((double) data.getObservationsInCell(i)) * factor);
                g.setColor(Color.lightGray);
                myGUI.drawRect(g, 0 + (i - 1) * breite, 0, breite, ((double) data.getObservationsInCell(i)) * factor);

            }
        }
    }

    /**
     * Calculates the upper border which should be displayed in the chart, and communicates all borders to the chart.
     */
    private void calculateBars() {
        hasToDrawChart = true;
        myGUI.setMax_y(
            (double) data.getObservationsInCell(data.getMostFrequentedCell()) * 100 / data.getObservations());
        myGUI.setMax_x(data.getUpperLimit());
        myGUI.setMin_x(data.getLowerLimit(1));
        myGUI.redrawChart();
    }

}