package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

/**
 * paint a static entity
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
public class EntityStaticComponent extends JComponent implements MouseListener {

    private static final long serialVersionUID = 1L;

    public EntityStaticComponent(Rectangle bound) {
        this.setLayout(null);
        this.setBounds(bound);
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    public void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(
            new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        AffineTransform t = g2.getTransform();
        java.awt.Component[] comps = this.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof EntityGrafic) {
                EntityGrafic en = (EntityGrafic) comps[i];
                g2.translate(en.getPositionIntern().getX(), en.getPositionIntern().getY());
                g2.rotate(en.getPositionIntern().getAngle());
                g2.translate(-en.getSize().width / 2.0, -en.getSize().height / 2.0);
                //System.out.println("paint static entity:"+en.getEntity().getId());
            } else {
                g2.translate(comps[i].getX(), comps[i].getY());
            }
            comps[i].paint(g2);
            g2.setTransform(t);
        }
    }

    private void mouseEventHandler(MouseEvent e) {
        //System.out.println("EntityStaticGraficComponent.mouseEventHandler x:"+e.getX()+" y:"+e.getY());
        java.awt.Component[] comps = this.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof EntityGrafic) {
                EntityGrafic en = (EntityGrafic) comps[i];
                Point center = new Point(en.getX() + en.getWidth() / 2, en.getY() + en.getHeight() / 2);
                int dist = (en.getWidth() + en.getHeight()) / 4;
                if (Point.distance(e.getX(), e.getY(), center.x, center.y) < dist) {
                    System.out.println("gefunden");
                    en.dispatchEvent(e);
                    //en.mousePressed(e);
                    break;
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        mouseEventHandler(e);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

}
