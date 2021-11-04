package desmoj.extensions.visualization2d.engine.viewer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

/**
 * Der Listener wertet MouseMotionEvents aus und schreibt die Mouseposition in die Statuszeile von viewerPanel. Der
 * Listener wertet MouseEvents aus und beim Click auf Button2 wird in viewerPanel der ZoomCenterPoint gesetzt.
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
public class CoordinatenListener implements MouseListener, MouseMotionListener {

    private HashSet<Component> roots = null;
    private ViewerPanel viewerPanel = null;
    private boolean centerMarked;

    /**
     * @param viewerPanel
     */
    public CoordinatenListener(ViewerPanel viewerPanel) {
        this.roots = new HashSet<Component>();
        this.addRoot(null);
        this.viewerPanel = viewerPanel;
        this.centerMarked = false;
    }

    /**
     * Hinzufuegen einer Komponente zur Liste der Parrent Komponenten
     *
     * @param root
     */
    public void addRoot(Component root) {
        this.roots.add(root);
    }

    /**
     * Bestimmt Koordinaten von e relativ zur ersten Parent Komponente in roots
     *
     * @param e
     * @return
     */
    private Point rootPos(MouseEvent e) {
        Component comp = e.getComponent();
        Point point = new Point(0, 0);
        while (!this.roots.contains(comp)) {
            Point p = comp.getLocation();
            point.translate(p.x, p.y);
            //System.out.print("y");
            comp = comp.getParent();
        }
        point.translate(e.getX(), e.getY());
        return point;
    }

    /**
     * wertet MouseEvent e aus und schreibt die Cursor Position in die Statuszeile des ViewerPanel.
     *
     * @param e
     */
    private void writeMousePosition(Point intern) {
        if (this.viewerPanel != null) {
            if (this.viewerPanel.getModel().getModelGrafic() != null &&
                this.viewerPanel.getModel().getModelGrafic().getSelectedView() != null) {
                //Point intern = this.viewerPanel.getModel().getModelGrafic().getMousePosition();
                if (intern != null) {
                    Point extern = this.viewerPanel.getModel().getModelGrafic().
                        transformToExtern(intern);
                    this.viewerPanel.setCoordinatePoint(extern);
                }
            }
        }
    }

    /**
     * setzt und zeichnet den ZoomCenterPoint
     *
     * @param intern
     */
    private void setZoomCenter(Point intern) {
        if (this.centerMarked && intern != null) {
            Point extern = this.viewerPanel.getModel().getModelGrafic().transformToExtern(intern);
            //System.out.println("setZoomCenter x: "+extern);
            this.viewerPanel.setSimulationZoomCenter(extern);
            this.viewerPanel.getModel().getModelGrafic().
                paintZoomMarker(this.viewerPanel.getSimulationZoomCenter(), false);
        }
        this.centerMarked = false;
    }

    private void markZoomCenterPoint(Point intern) {
        if (intern != null) {
            Point extern = this.viewerPanel.getModel().getModelGrafic().transformToExtern(intern);
            //System.out.println("markZoomCenter  "+extern+"  ZoomCenter: "+this.viewerPanel.getSimulationZoomCenter());
            if (this.viewerPanel.getModel().getModelGrafic().isInZoomCenter(extern)) {
                //System.out.println("markZoomCenter  is in");
                this.centerMarked = true;
                this.viewerPanel.getModel().getModelGrafic().
                    paintZoomMarker(this.viewerPanel.getSimulationZoomCenter(), true);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        Point intern = this.rootPos(e);
        //System.out.println(" x: "+intern.getX()+"  y: "+intern.getY());
        this.writeMousePosition(intern);
    }

    public void mouseDragged(MouseEvent e) {
        Point intern = this.rootPos(e);
        //System.out.println(" x: "+intern.getX()+"  y: "+intern.getY());
        this.writeMousePosition(intern);
    }


    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mousePressed(MouseEvent e) {
        Point intern = this.rootPos(e);
        //System.out.println("CoordinatenListener.pressed x: "+intern);
        this.markZoomCenterPoint(intern);
    }

    public void mouseReleased(MouseEvent e) {
        Point intern = this.rootPos(e);
        //System.out.println("CoordinatenListener.released x: "+intern.getX()+"  y: "+intern.getY());
        this.setZoomCenter(intern);
    }
}
