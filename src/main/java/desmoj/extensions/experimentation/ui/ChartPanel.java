package desmoj.extensions.experimentation.ui;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A GUI container (inherited from javax.swing.JPanel) which includes a header, legend items (if needed) and a special
 * Chart object. All functionality of the Chart object which is needed by the user, is encapsulated in this class, so
 * the user doesn`t need to have direct access to the Chart object, he doesn`t need to care about the Chart object at
 * all.
 * <p>
 * This class additionally includes a method saveAs(filename) for saving the chart to an image file.
 * <p>
 * If you are a user of DESMO-J you don`t need to create a object of this kind: Create a HistogramPlotter or a
 * TimeSeriesPlotter instead!
 * <p>
 * This class is needed by the graphical observers HistogramPlotter and TimeSeriesPlotter. If you want to extend DESMO-J
 * you can create new plotter classes inherited from GraphicalObserver which contains this object as a private field
 * "myGUI" and a method drawChart(Graphics) for implementing the drawing of the data.
 *
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
public class ChartPanel extends JPanel implements ActionListener {

    /**
     * The panel which contains the title label.
     */
    private final JPanel titlePanel;

    /**
     * A label used as title for this panel.
     */
    private final JLabel title;

    /**
     * A panel which contains the legend items.
     */
    private final JPanel legendPanel;

    /**
     * The chart class which is inherited from canvas. All chart functionality is implemented there.
     */
    private final Chart chart;

    /**
     * The number of elements which are represented in the legend.
     */
    private int legendElements = 0;

    /**
     * The context menu for this chart.
     */
    private final PopupMenu popupMenu;


    /**
     * Constructor of the chart panel. If you are a user of Desmo-J you don`t need to create a object of this kind:
     * Create a HistogramPlotter or a TimeSeriesPlotter instead!
     * <p>
     * If you want to extend Desmo-J you can create new plotter classses inherited from GraphicalObserver which contains
     * this object as a private field "myGUI" and a method drawChart(Graphics) for implementing the drawing of the
     * data.
     *
     * @param chartOwner The object which implements the drawChart() method.
     * @param name       Title of the chart.
     * @param x_label    The labeling for the x axis.
     * @param y_label    The labeling for the y axis.
     */
    public ChartPanel(ChartOwner chartOwner, String name, String x_label, String y_label) {
        super();
        title = new JLabel(name);
        titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout());
        titlePanel.add(title);

        legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout());
        legendPanel.setAutoscrolls(true);

        chart = new Chart(chartOwner, x_label, y_label);

        popupMenu = new PopupMenu("Chart");
        MenuItem mi = new MenuItem("Save image");
        mi.addActionListener(this);
        popupMenu.add(mi);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        setLayout(new BorderLayout());
        add("North", titlePanel);
        add("South", legendPanel);
        add("Center", chart);
        add(popupMenu);

    }

    /**
     * Returns the colors used to draw charts. You can draw more than one diagram in one chart. If you do so, you have
     * to use colors in the same order, as the legend showcases them. This static method returns different colors,
     * starting with 0=RED and ending with 9=LIGHTGREY. All values higher or smaller than this will return BLACK. This
     * is not implemented as static final array, because of the possibility to draw more charts then colors exists (they
     * will be black).
     *
     * @param i The index number of the color you want.
     * @return An AWT Color object.
     */
    public static Color color(int i) {
        switch (i) {
            case 0:
                return Color.red;
            case 1:
                return Color.blue;
            case 2:
                return Color.green;
            case 3:
                return Color.pink;
            case 4:
                return Color.darkGray;
            case 5:
                return Color.orange;
            case 6:
                return Color.magenta;
            case 7:
                return Color.yellow;
            case 8:
                return Color.cyan;
            case 9:
                return Color.lightGray;
            default:
                return Color.black;
        }
    }

    /**
     * Paints the panel and all of its components.
     */
    public void paint(Graphics g) {
        scale();
        super.paint(g);
    }

    /**
     * Rescales the size of the header font.
     */
    public void scale() {
        int headerSize = Math.min(this.getWidth(), this.getHeight()) / 14;
        if (headerSize < 14) {
            headerSize = 14;
        }
        if (headerSize > 30) {
            headerSize = 30;
        }
        if (headerSize != title.getFont().getSize()) {
            title.setFont(new Font("Sans Serif", Font.CENTER_BASELINE, headerSize));
        }
    }

    /**
     * Forces the chart to repaint.
     */
    public void redrawChart() {
        chart.repaint();
    }

    /**
     * Adds a new legend item at the bottom of the panel in a new color.
     *
     * @param text The labeling of the legend item.
     */
    public void addLegend(String text) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(text);
        label.setFont(new Font("Sans Serif", Font.BOLD, 10));
        panel.setBackground(color(legendElements++));
        label.setForeground(Color.white);
        panel.add(label);
        legendPanel.add(panel);

    }

    /**
     * Tests a new pair of values which appears in the data. The pair will NOT be stored, it only will be tested if the
     * values are lower or higher than the existing minimums and maximums. This is needed for scaling the chart
     * automatically.
     *
     * @param x A new x value which shall be tested, if it is the new minmum or maximum.
     * @param y A new y value which shall be tested, if it is the new minmum or maximum.
     * @return Returns true if the values are higher or smaller than the current axis borders.
     */
    public boolean testValue(double x, double y) {
        return chart.testValue(x, y);
    }

    /**
     * Sets the maximum x value which appears in the data.
     *
     * @param max_x The maximum x value which appears in the data.
     */
    public void setMax_x(double max_x) {
        chart.setMax_x(max_x);
    }

    /**
     * Sets the maximum y value which appears in the data.
     *
     * @param max_y The maximum y value which appears in the data.
     */
    public void setMax_y(double max_y) {
        chart.setMax_y(max_y);
    }

    /**
     * Sets the minimum x value which appears in the data.
     *
     * @param min_x The minimum x value which appears in the data.
     */
    public void setMin_x(double min_x) {
        chart.setMin_x(min_x);
    }

    /**
     * Sets the minimum y value which appears in the data.
     *
     * @param min_y The minimum y value which appears in the data.
     */
    public void setMin_y(double min_y) {
        chart.setMin_y(min_y);
    }

    /**
     * Resets the legend (labeling) for the x axis.
     *
     * @param x_label A new title for the x axis.
     */
    public void set_xlabel(String x_label) {
        chart.setX_label(x_label);
    }

    /**
     * Resets the legend (labeling) for the y axis.
     *
     * @param y_label A new title for the y axis.
     */
    public void set_ylabel(String y_label) {
        chart.setY_label(y_label);
    }

    /**
     * Draws a line into the chart. This method automatically calls the methods calculateXPosition() and
     * calculateYPosition() so you don`t have to take care about any scaling or transforming.
     *
     * @param g  The specified Graphics context.
     * @param x1 The x value of the starting coordinates.
     * @param y1 The y value of the starting coordinates.
     * @param x2 The x value of the ending coordinates.
     * @param y2 The x value of the ending coordinates.
     */
    public void drawLine(Graphics g, double x1, double y1, double x2, double y2) {
        chart.drawLine(g, x1, y1, x2, y2);
    }

    /**
     * Draws an unfilled rectangle into the chart. This method automatically calls the methods calculateXPosition() and
     * calculateYPosition() so you don`t have to take care about any scaling or transforming.
     *
     * @param g  The specified Graphics context.
     * @param x1 The x value of the starting coordinates.
     * @param y1 The y value of the starting coordinates.
     * @param x2 The x value of the ending coordinates.
     * @param y2 The x value of the ending coordinates.
     */
    public void drawRect(Graphics g, double x1, double y1, double x2, double y2) {
        chart.drawRect(g, x1, y1, x2, y2);
    }

    /**
     * Draws a filled rectangle into the chart. This method automatically calls the methods calculateXPosition() and
     * calculateYPosition() so you don`t have to take care about any scaling or transforming.
     *
     * @param g  The specified Graphics context.
     * @param x1 The x value of the starting coordinates.
     * @param y1 The y value of the starting coordinates.
     * @param x2 The x value of the ending coordinates.
     * @param y2 The x value of the ending coordinates.
     */
    public void fillRect(Graphics g, double x1, double y1, double x2, double y2) {
        chart.fillRect(g, x1, y1, x2, y2);
    }

    /**
     * Draws a line into the chart. This method automatically calls the methods calculateXPosition() and
     * calculateYPosition() so you don`t have to take care about any scaling or transforming.
     * <p>
     * If you have a graphic context (e.g. method shall be invoked via a paint(Graphics g) method you should NOT use
     * this method, but rather drawLine(Graphics, double, double, double, double).
     *
     * @param color The color of the line.
     * @param x1    The x value of the starting coordinates.
     * @param y1    The y value of the starting coordinates.
     * @param x2    The x value of the ending coordinates.
     * @param y2    The x value of the ending coordinates.
     */
    public void drawLine(Color color, double x1, double y1, double x2, double y2) {
        chart.drawLine(color, x1, y1, x2, y2);
    }

    /**
     * Event handler for right-mouse-clicks onto the chart. The context menu will be shown.
     */
    public void processMouseEvent(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
        super.processMouseEvent(e);
    }

    /**
     * Event handler for the items in the context menu.
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Save image")) {
            saveAs(title.getText() + ".jpeg");
        }
    }

    /**
     * Saves the chart (including title and legend) as an image file. If saving fails, the exception will be catched,
     * but not handled.
     *
     * @param title
     */
    public void saveAs(String title) {
        try {
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            this.paintAll(g);
            g.dispose();
            ImageIO.write(image, "jpeg", new File(title));

        } catch (Exception e) {
        }
    }
}

