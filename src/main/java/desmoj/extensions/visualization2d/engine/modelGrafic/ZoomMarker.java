package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import desmoj.extensions.visualization2d.engine.model.Model;

/**
 * Class to paint a marker at zoom center.
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
public class ZoomMarker extends JComponent {

    private Point point = null;
    private final String viewId;
    private final Model model;
    private boolean shift_mode;

    public ZoomMarker(Model model, String viewId, Point point) {
        this.viewId = viewId;
        this.point = point;
        this.model = model;
        this.shift_mode = false;
    }

    public void setPoint(Point point, boolean mode) {
        //System.out.println("ZoomMarker.setPoint "+point+"  "+mode);
        this.point = point;
        this.shift_mode = mode;
        this.repaint();
    }

    public boolean isInZoomCenter(Point p) {
        Point p_intern = model.getModelGrafic().transformToIntern(this.viewId, p);
        Point center_intern = model.getModelGrafic().transformToIntern(this.viewId, point);
        //System.out.println("Marker: p     : "+p_intern);
        //System.out.println("Marker: center: "+center_intern);
        int dx = p_intern.x - center_intern.x;
        int dy = p_intern.y - center_intern.y;
        return Math.sqrt(dx * dx + dy * dy) <= 10;
    }

    public void paintComponent(Graphics g) {
        //System.out.println("ZoomMarker.paint");
        Graphics2D g2 = (Graphics2D) g;
        if (this.point != null) {
            Point intern = model.getModelGrafic().
                transformToIntern(this.viewId, point);
            g2.setColor(Grafic.COLOR_ZOOM_MARKER);
            g2.setStroke(new BasicStroke(1));
            if (this.shift_mode) {
                g2.fillOval(intern.x - 10, intern.y - 10, 20, 20);
            } else {
                g2.drawOval(intern.x - 10, intern.y - 10, 20, 20);
            }
            g2.drawLine(intern.x - 20, intern.y, intern.x + 20, intern.y);
            g2.drawLine(intern.x, intern.y - 20, intern.x, intern.y + 20);
        }
    }

}
