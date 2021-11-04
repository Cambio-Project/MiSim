package desmoj.extensions.experimentation.ui;

import javax.swing.JComponent;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 * A GUI component (inherited from java.awt.Canvas) which displays a chart. It includes two labeled and auto-scaled
 * axes. There a methods (drawLine, drawRect, fillRect) to paint on the chart without taking care about transforming or
 * scaling the dimensions. You have to add this class to a panel or a frame. You also have to implement the method which
 * should draw the data (e.g. a histogram or a mathematical function) in a class which is inherited from the interface
 * ChartOwner.
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
public class Chart extends JComponent implements ActionListener {

    /** The minimum spacing between two labels on the axes. */
    private static final int FONT_SPACING = 6;
    /** The border spacing of the chart. */
    private static final int ARROW_SPACING = 6;
    /**
     * The left border of the white space for drawing the chart. This is not 0, because in the left of the chart there
     * are for example the labels of the y axis.
     */
    int borderLeft;
    /**
     * The right border of the white space for drawing the chart. This is not equal to the width of the canvas, because
     * in the right of the chart there is a small spacing.
     */
    int borderRight;
    /**
     * The upper border of the white space for drawing the chart. This is not 0, because above the chart there is a
     * small spacing.
     */
    int borderTop;
    /**
     * The right border of the white space for drawing the chart. This is not equal to the width of the canvas, because
     * under the chart there are for example the labels of the x axis.
     */
    int borderBottom;
    /**
     * The object who implements the method for drawing the data. In Desmo-J this is the HistogramPlotter respectively
     * the TimeSeriesPlotter.
     */
    private final ChartOwner chartOwner;
    /** The legend (labeling) of the y axis. */
    private String y_label;
    /** The legend (labeling) of the x axis. */
    private String x_label;
    /** The minimum x value of the data set (needed for scaling). */
    private double min_x;
    /** The maximum x value of the data set (needed for scaling). */
    private double max_x;
    /** The minimum y value of the data set (needed for scaling). */
    private double min_y;
    /** The maximum y value of the data set (needed for scaling). */
    private double max_y;
    /** Largest value which is labeled on the y axis. */
    private double upper_y;
    /** Smallest value which is labeled on the y axis. */
    private double lower_y;
    /** The interval between two labels on the y axis. */
    private double interval_y;
    /** The number of pixels which represents interval_y. */
    private int interval_in_pixels_y;
    /** Largest value which is labeled on the x axis. */
    private double upper_x;
    /** Smallest value which is labeled on the x axis. */
    private double lower_x;
    /** The interval between two labels on the x axis. */
    private double interval_x;
    /** The number of pixels which represents interval_x. */
    private int interval_in_pixels_x;
    /** The size of the font used for labeling. */
    private int fontHeight;
    /** The font used for labeling. */
    private Font font;
    /** The thickness of arrows on axes. */
    private int arrowWidth;

    /**
     * Constructor of the chart. Saves the given labels for x- and y-axis and calls the constructor of java.awt.Canvas
     *
     * @param chartOwner The object which implements the drawChart() method.
     * @param x_label    The legend for the x axis.
     * @param y_label    The legend for the y axis.
     */
    public Chart(ChartOwner chartOwner, String x_label, String y_label) {
        super();
        this.y_label = y_label;
        this.x_label = x_label;
        this.chartOwner = chartOwner;
        // Set current minimum=maximum, so the chart will not be displayed
        // until max_x!=min_x AND max_y!=min_y
        max_x = min_x = 0;
        max_y = min_y = 0;

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * This method is called automatically if the canvas has to be repaint. You don`t have to do this! It overrides the
     * paint(Graphics) method from java.awt.Canvas. If size of the chart is big enough, it calls paintBackground() (for
     * painting the chart background).
     *
     * @param g The specified Graphics context.
     */
    public void paint(Graphics g) {
        if (this.getWidth() > 40 && this.getHeight() > 40) {
            paintBackground(g);
            chartOwner.drawChart(g);
        }
    }

    /**
     * Draws the chart basics. Calculates the best fitting intervalls for the axes. Paints a white backgorund, the axes,
     * the labels and a grey grid for easier metering.
     * <p>
     * At last it tansforms (not scales!) the coordinate system. Thus the methods drawLine, drawRect and fillRect must
     * not take care about transforming: The root of the coordinate system will be (0,0), and the value y will be
     * autoscaled to -y, so a higher data value will be painted above a lower y value, unlike the coordinate system of a
     * canvas. ATTENTION: The value "1" still represents a pixel, because if we would scale "1" to interval_x (or rather
     * interval_y) a new line would be very fat (that is not want we want). The "scaling" will be done by the methods
     * calculateXPosition() and calculateYPosition() of this class, which will be automatically invoked if you use
     * drawLine(), drawRect() or fillRect().
     *
     * @param g The specified Graphics context.
     */
    public void paintBackground(Graphics g) {
        double f = 1.6;           // factor for next increasing of interval
        int elements;           // number of elements that have to be written with chosen interval
        int pos;               // helping variable for drawing the labels (saves the position)
        int[] label_width;       // helping variable for drawing the labels (saves the width of every label)
        String[]
            label;           // helping variable for drawing the labels (saves the string representation of every label)
        FontRenderContext fdr = ((Graphics2D) g).getFontRenderContext();

        // Do not continue if there are no values to be painted
        if (max_x == min_x || max_y == min_y) {
            return;
        }

        // sets the borders and the font size depending on the canvas size (yet not too small)
        borderRight = Math.max(this.getWidth() - ARROW_SPACING - arrowWidth, 100);
        borderTop = ARROW_SPACING + arrowWidth;
        fontHeight = Math.min(this.getHeight(), this.getWidth()) / 25;
        if (fontHeight > 16) {
            fontHeight = 16;
        }
        if (fontHeight < 9) {
            fontHeight = 9;
        }
        setFont(g, fontHeight);
        borderBottom = Math.max(this.getHeight() - ARROW_SPACING - arrowWidth - (int) (2.5 * fontHeight), 60);

        // this loop finds the most useful interval for the y axis
        interval_y = 1;
        do {
            if (max_y % interval_y == 0) {
                upper_y = max_y;
            } else {
                upper_y = max_y + (interval_y - (max_y % interval_y));
            }

            if (min_y % interval_y == 0) {
                lower_y = min_y;
            } else {
                lower_y = min_y - (min_y % interval_y);
            }

            elements = (int) ((upper_y - lower_y) / interval_y) + 1;

            if ((fontHeight + FONT_SPACING) * (elements - 1) < borderBottom - borderTop) {
                break;  // this interval fits
            }

            if (Math.log10(interval_y) % 1 == 0) {
                f = 1.25;
                interval_y *= f;   // increasing the interval
                continue;
            }
            if (Math.abs(f - 1.6) < 0.0001) {
                f = 1.25;
            } else {
                f = 1.6;
            }
            interval_y *= f;   // increasing the interval
        } while (true);

        // now the y interval in pixels is known:
        interval_in_pixels_y = elements <= 1 ? 1 : (borderBottom - borderTop) / (elements - 1);

        // search the left border of the chart, depending on the width of the y labels
        // and saving the width of every label
        borderLeft = 0;
        label_width = new int[elements];
        label = new String[elements];
        for (int n = 0; n < elements; n++) {
            if ((lower_y + (interval_y * n) % 1 == 0)) {
                label[n] = "" + ((int) (lower_y + (interval_y * n)));
            } else {
                label[n] = "" + ((lower_y + (interval_y * n)));
            }
            label_width[n] = (int) font.getStringBounds(label[n], fdr).getWidth();
            if (label_width[n] > borderLeft) {
                borderLeft = label_width[n];
            }
        }
        borderLeft = borderLeft + arrowWidth + ARROW_SPACING + (int) (fontHeight * 1.5);

        // first paint the background
        g.setColor(Color.white);  // white Background
        //g.fillRect(borderLeft-arrowWidth,borderTop-arrowWidth, borderRight-borderLeft+2*arrowWidth, borderBottom-borderTop+2*arrowWidth);
        g.fillRect(borderLeft, borderTop, borderRight - borderLeft, borderBottom - borderTop);

        // then paint the units on the y-axis
        for (int n = 0; n < elements; n++) {
            g.setColor(Color.black);  // black labels
            pos = borderBottom - interval_in_pixels_y * n;
            g.drawString(label[n], borderLeft - label_width[n] - arrowWidth, pos + (fontHeight / 2));
            g.drawLine(borderLeft - arrowWidth + 1, pos, borderLeft, pos);
            g.setColor(Color.lightGray);  // lightgray lines
            g.drawLine(borderLeft + 1, pos, borderRight, pos);
        }

        // this loop finds the most useful interval for the x axis
        interval_x = 1;
        do {
            if (max_x % interval_x == 0) {
                upper_x = max_x;
            } else {
                upper_x = max_x + (interval_x - (max_x % interval_x));
            }

            if (min_x % interval_x == 0) {
                lower_x = min_x;
            } else {
                lower_x = min_x - (min_x % interval_x);
            }

            elements = (int) ((upper_x - lower_x) / interval_x) + 1;
            interval_in_pixels_x = (borderRight - borderLeft) / (elements - 1);

            // testing every unit labels width and saving the width
            label_width = new int[elements];
            label = new String[elements];
            int n;
            for (n = 0; n < elements; n++) {
                if ((lower_x + (interval_x * n) % 1 == 0)) {
                    label[n] = "" + ((int) (lower_x + (interval_x * n)));
                } else {
                    label[n] = "" + ((lower_x + (interval_x * n)));
                }
                label_width[n] = (int) font.getStringBounds(label[n], fdr).getWidth();
                if (label_width[n] + FONT_SPACING > interval_in_pixels_x) {
                    break;
                }
            }
            if (n == elements) {
                break;  // loop was succesfull: this interval fits
            }

            if (Math.log10(interval_x) % 1 == 0) {
                f = 1.25;
                interval_x *= f;   // increasing the interval
                continue;
            }
            if (f == 1.6) {
                f = 1.25;
            } else {
                f = 1.6;
            }
            interval_x *= f;   // increasing the interval
        } while (true);

        // paint the units on the x-axis
        for (int n = 0; n < elements; n++) {
            g.setColor(Color.black);  // black labels
            pos = borderLeft + interval_in_pixels_x * n;
            g.drawString(label[n], pos - (label_width[n] / 2), borderBottom + arrowWidth + ARROW_SPACING + fontHeight);
            g.drawLine(pos, borderBottom + arrowWidth - 1, pos, borderBottom);
            g.setColor(Color.lightGray);  // lightgray lines
            g.drawLine(pos, borderTop, pos, borderBottom);
        }


        // paints the axes and the arrows
        g.setColor(Color.black);  // black x- and y-axis
        g.drawLine(borderLeft, borderTop, borderLeft, borderBottom);
        g.drawLine(borderLeft, borderBottom, borderRight, borderBottom);
        int[] x = new int[3];     // two small arrows
        int[] y = new int[3];
        x[0] = borderLeft;
        y[0] = borderTop - arrowWidth;
        x[1] = borderLeft - arrowWidth;
        y[1] = borderTop;
        x[2] = borderLeft + arrowWidth;
        y[2] = borderTop;
        g.fillPolygon(x, y, 3);  // arrow on y-axis
        x[0] = borderRight + arrowWidth;
        y[0] = borderBottom;
        x[1] = borderRight;
        y[1] = borderBottom - arrowWidth;
        x[2] = borderRight;
        y[2] = borderBottom + arrowWidth;
        g.fillPolygon(x, y, 3);  // arrow on x-axis

        //		 paints the label (header) for the y axis
        g.setColor(Color.black);
        drawVerticalString((Graphics2D) g, fdr, y_label, (float) (fontHeight * 1.25),
            (float) ((borderBottom + font.getStringBounds(y_label, fdr).getWidth()) / 2));
        g.drawString(x_label,
            borderLeft + (int) ((borderRight - borderLeft - font.getStringBounds(x_label, fdr).getWidth()) / 2),
            this.getHeight() - 1);

        // Rescales the canvas, thus drawing of user data will be easier.
        ((Graphics2D) g).translate((long) borderLeft, (long) borderBottom);
        ((Graphics2D) g).scale(1, -1);

    }


    /**
     * Calculates the x coordinate in pixels for the given x data value.
     *
     * @param x The x data value.
     * @return The x position in pixels, where the data value should be painted.
     */
    public int calculateXPosition(double x) {
        return (int) ((x - this.lower_x) / interval_x * interval_in_pixels_x);
    }

    /**
     * Calculates the y coordinate in pixels for the given y data value.
     *
     * @param y The y data value.
     * @return The y position in pixels, where the data value should be painted.
     */
    public int calculateYPosition(double y) {
        return (int) ((y - this.lower_y) / interval_y * interval_in_pixels_y);
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
        g.drawLine(calculateXPosition(x1), calculateYPosition(y1), calculateXPosition(x2), calculateYPosition(y2));

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
        g.drawRect(calculateXPosition(x1), calculateYPosition(y1), calculateXPosition(x2), calculateYPosition(y2));

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
        g.fillRect(calculateXPosition(x1), calculateYPosition(y1), calculateXPosition(x2), calculateYPosition(y2));

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
        Graphics g = this.getGraphics();
        g.setColor(color);
        ((Graphics2D) g).translate((long) borderLeft, (long) borderBottom);
        ((Graphics2D) g).scale(1, -1);
        g.drawLine(calculateXPosition(x1), calculateYPosition(y1), calculateXPosition(x2), calculateYPosition(y2));
    }

    /**
     * Draws a text which is rotated by 90 degrees.
     *
     * @param g2d  The specified Graphics context.
     * @param fdr  The specified FontRenderContext.
     * @param text The text which shall be displayed.
     * @param x    The x coordinate of the right(!) border.
     * @param y    The y coordinate of the bottom border.
     */
    private void drawVerticalString(Graphics2D g2d, FontRenderContext fdr, String text, float x, float y) {
        g2d.rotate(-Math.PI / 2.0);
        new TextLayout(text, font, fdr).draw(g2d, -y, x);
        g2d.rotate(Math.PI / 2.0);
    }


    /**
     * Sets a new font size for the font used inside the chart (e.g. for labels).
     *
     * @param g    The specified Graphics context.
     * @param size The new Size for the the font.
     */
    private void setFont(Graphics g, int size) {
        font = new Font("sans serif", Font.PLAIN, size);
        g.setFont(font);
        fontHeight = size;
        arrowWidth = fontHeight / 2;
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
        if (x > max_x) {
            max_x = x;
        }
        if (x < min_x) {
            min_x = x;
        }
        if (y > max_y) {
            max_y = y;
        }
        if (y < min_y) {
            min_y = y;
        }

        return x < lower_x || x > upper_x || y < lower_y || y > upper_y;
    }

    /**
     * Sets the maximum y value which appears in the data.
     *
     * @param max_y The maximum y value which appears in the data.
     */
    public void setMax_y(double max_y) {
        this.max_y = max_y;
    }

    /**
     * Sets the maximum x value which appears in the data.
     *
     * @param max_x The maximum x value which appears in the data.
     */
    public void setMax_x(double max_x) {
        this.max_x = max_x;
    }

    /**
     * Sets the minimum x value which appears in the data.
     *
     * @param min_x The minimum x value which appears in the data.
     */
    public void setMin_x(double min_x) {
        this.min_x = min_x;
    }

    /**
     * Sets the minimum y value which appears in the data.
     *
     * @param min_y The minimum y value which appears in the data.
     */
    public void setMin_y(double min_y) {
        this.min_y = min_y;
    }

    /**
     * Resets the legend (labeling) for the x axis.
     *
     * @param x_label A new title for the x axis.
     */
    public void setX_label(String x_label) {
        this.x_label = x_label;
    }

    /**
     * Resets the legend (labeling) for the y axis.
     *
     * @param y_label A new title for the y axis.
     */
    public void setY_label(String y_label) {
        this.y_label = y_label;
    }


    /**
     * Event handler for right-mouse-clicks onto the chart. The context menu will be shown.
     */
    public void processMouseEvent(MouseEvent e) {
        ((ChartPanel) (this.getParent())).processMouseEvent(e);
    }

    /**
     * Event handler for this class. Does nothing.
     */
    public void actionPerformed(ActionEvent e) {
    }
}

