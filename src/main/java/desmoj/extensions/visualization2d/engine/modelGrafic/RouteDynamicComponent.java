package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.Route;


/**
 * A route that moves all entities on it.
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
public class RouteDynamicComponent extends JComponent implements MouseListener {

    private static final long serialVersionUID = 1L;
    private final String viewId;
    private final Model model;

    public RouteDynamicComponent(Model model, String viewId, Rectangle bound) {
        this.viewId = viewId;
        this.model = model;
        this.setLayout(null);
        this.setBounds(bound);
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    public void updateEntityPositions(long time) {
        String[] id = model.getRoutes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Route route = model.getRoutes().get(id[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null &&
                routeGrafic.getViewId().equals(viewId)) {
                routeGrafic.getRouteDynamicGrafic().updateEntityPositions(time);
            }
        }
    }

    public void paintComponent(Graphics g) {
        String[] id = model.getRoutes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Route route = model.getRoutes().get(id[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null &&
                routeGrafic.getViewId().equals(viewId)) {
                routeGrafic.getRouteDynamicGrafic().paintComponent(g);
            }
        }
    }

    private void mouseEventHandler(MouseEvent e) {
        //System.out.println("RouteDynamicGraficComponent.mouseEventHandler");
        String[] id = model.getRoutes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Route route = model.getRoutes().get(id[i]);
            RouteGrafic routeGrafic = (RouteGrafic) route.getGrafic();
            if (routeGrafic != null) {
                routeGrafic.getRouteDynamicGrafic().mouseEventHandler(e);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseReleased(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseClicked(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseEntered(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseExited(MouseEvent e) {
        mouseEventHandler(e);
    }

}
