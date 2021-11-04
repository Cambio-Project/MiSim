package desmoj.extensions.grafic.util;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class to paint a JPanel into a JFrame (onscreen) or as png into a file (offscreen). When paint offscreen, no
 * application frame is genereted.
 *
 * @author christian.mueller@th-wildau.de and goebel@informatik.uni-hamburg.de
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class PaintPanel {

    private final String path;        /* output path */
    private final Dimension size;        /* image size */
    private final Point location;    /* location of output frame */

    /**
     * Constructor to set the path of output directory and the size of created image.
     *
     * @param path
     * @param size
     */
    public PaintPanel(String path, Dimension size) {
        this.path = path;
        this.size = size;
        this.location = new Point(50, 50);
    }

    protected String getPath() {
        return this.path;
    }

    protected Dimension getSize() {
        return this.size;
    }

    /**
     * save panel as png file in path/fileName.png
     *
     * @param panel
     * @param fileName
     */
    protected void save(JPanel panel, String fileName) {
        Rectangle rect = new Rectangle(0, 0, size.width, size.height);
        BufferedImage img = this.paintNotVisibleComponent(panel, new Container(), rect);
        this.savePicture(img, "png", path, fileName);
    }

    /**
     * show panel in a JFrame
     *
     * @param panel
     * @param name  name of JFrame
     */
    protected void show(JPanel panel, String name) {
        ShowPanel showPanel = new ShowPanel(panel, this.size, name);
        showPanel.setLocation(this.location);
        this.location.translate(30, 30);
        showPanel.pack();
    }

    /**
     * save BufferedImage as file.
     *
     * @param img    image to save
     * @param format image format. For possible values look at api of ImageIO
     * @param path   output directory
     * @param name   filename is name.format
     */
    private void savePicture(BufferedImage img, String format, String path, String name) {
        try {
            ImageIO.write(img, format, new File(path + name + "." + format));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create a BufferedImage and paint component in it.
     *
     * @param component component to paint
     * @param container a possible empty container
     * @param rectangle size of image
     * @return
     */
    private BufferedImage paintNotVisibleComponent(Component component,
                                                   Container container, Rectangle rectangle) {
        BufferedImage img = new BufferedImage(rectangle.width,
            rectangle.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        SwingUtilities.paintComponent(g, component, container, rectangle);
        g.dispose();
        return img;
    }

    /**
     * JFrame to show panel onscreen
     *
     * @author Christian
     */
    class ShowPanel extends JFrame {

        public ShowPanel(JPanel panel, Dimension size, String name) {
            super(name);
            this.setPreferredSize(size);
            this.add(panel);
            this.setVisible(true);
        }

    }
}
