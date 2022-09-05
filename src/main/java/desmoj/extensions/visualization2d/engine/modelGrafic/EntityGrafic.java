package desmoj.extensions.visualization2d.engine.modelGrafic;


import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Entity;
import desmoj.extensions.visualization2d.engine.model.EntityType;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.viewer.InfoPane;
import desmoj.extensions.visualization2d.engine.viewer.ViewerPanel;


/**
 * Grafic of an entity
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
public class EntityGrafic extends JComponent implements Grafic, MouseListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Entity entity;
    private final EntityType entityType;
    private Image image;
    private String code;
    private EntityPosition positionIntern;
    private final EntityPosition positionExtern;
    private boolean printable;
    private final String viewId;


    public EntityGrafic(Entity entity, String viewId, EntityPosition positionExtern) throws ModelException {
        // TODO Auto-generated constructor stub

        this.entity = entity;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.positionExtern = positionExtern;
        this.transform();
        this.entityType = entity.getModel().getEntityTyps().get(this.entity.getEntityTypeId());
        this.setCode();
        this.setImage();
        this.setPreferredSize();
        this.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.black));
        //System.out.println("EntityGrafic code: "+code+" name: "+this.entity.getName());

        // Listener Hinzufuegen
		/*
		if(this.entity.getModel().getCoordinatenListener() != null){
			this.addMouseMotionListener(this.entity.getModel().getCoordinatenListener());
			this.addMouseListener(this.entity.getModel().getCoordinatenListener());
			System.out.println("setze Entity CoordinatenListener: "+this.entity.getId());
		}
		*/
        this.addMouseListener(this);

    }

    public EntityGrafic(Entity entity) throws ModelException {
        this(entity, null, null);
    }

    /**
     * get all views (viewId's) with Entities
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getEntities().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Entity entity = model.getEntities().get(ids[i]);
            EntityGrafic entityGrafic = (EntityGrafic) entity.getGrafic();
            if (entityGrafic != null && entity.isStatic()) {
                String viewId = entityGrafic.getViewId();
                views.add(viewId);
            }
        }
        String[] out = new String[views.size()];
        int i = 0;
        for (Iterator<String> it = views.iterator(); it.hasNext(); ) {
            out[i] = it.next();
            i++;
        }
        return out;

    }

    /**
     * Construct the smallest rectangle, which include the positions of all entities in view
     *
     * @param viewId id of view
     * @return smallest Rectangle, when an static Entity exist. null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        double minX = (double) Integer.MAX_VALUE / 2;
        double minY = (double) Integer.MAX_VALUE / 2;
        double maxX = (double) Integer.MIN_VALUE / 2;
        double maxY = (double) Integer.MIN_VALUE / 2;
        String[] id = model.getEntities().getAllIds();
        //System.out.println("Anz. Entities: "+id.length);
        for (int i = 0; i < id.length; i++) {
            Entity entity = model.getEntities().get(id[i]);
            if (entity.isStatic()) {
                EntityGrafic entityGrafic = (EntityGrafic) entity.getGrafic();
                if (entityGrafic != null &&
                    entityGrafic.getViewId().equals(viewId)) {
                    found = true;
                    minX = Math.floor(Math.min(minX, entityGrafic.getPositionExtern().getX()));
                    minY = Math.floor(Math.min(minY, entityGrafic.getPositionExtern().getY()));
                    maxX = Math.ceil(Math.max(maxX, entityGrafic.getPositionExtern().getX()));
                    maxY = Math.ceil(Math.max(maxY, entityGrafic.getPositionExtern().getY()));
                    //System.out.println(entity.getId()+"  "+entityGrafic.getPositionExtern().getX()+" "+entityGrafic.getPositionExtern().getY());
                }
            }
        }
        Rectangle r = null;
        if (found) {
            r = new Rectangle((int) Math.round(minX), (int) Math.round(minY), (int) Math.round(maxX - minX),
                (int) Math.round(maxY - minY));
        }
        //System.out.println("EntityGrafic: BoundsExtern: "+r);
        return r;
    }

    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getEntities().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Entity entity = model.getEntities().get(id[i]);
            EntityGrafic entityGrafic = (EntityGrafic) entity.getGrafic();
            if (entityGrafic != null) {
                if (entity.isStatic() &&
                    entityGrafic.getViewId().equals(viewId)) {
                    entityGrafic.transform();
                    panel.add(entityGrafic);
                }
                //System.out.println("EntityGrafic.updateInit   "+entity.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    public void transform() {
        if (this.positionExtern != null) {
            Point ext = new Point((int) positionExtern.getX(), (int) positionExtern.getY());
            Point p = entity.getModel().getModelGrafic().
                transformToIntern(this.viewId, new Point(ext));
            if (p == null) {
                this.positionIntern = this.positionExtern;
                this.printable = false;
            } else {
                EntityPosition p1 =
                    new EntityPosition(p.x, p.y, positionExtern.getAngle(), positionExtern.getDirection());
                this.positionIntern = p1;
                this.printable = true;
            }
            this.setLocation(this.positionIntern);
        }
        //System.out.println("EntityGrafic.transform id: "+entity.getId()+"  "+this.positionIntern.getX()+"  "+this.positionIntern.getY());
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setImage() throws ModelException {
        this.image = this.entityType.getImage(this.entity.getState());
    }

    public void setCode() {
        if (this.entity.getName() != null) {
            this.code = this.entity.getName();
        } else {
            this.code = "id: " + this.entity.getId();
        }
        this.setPreferredSize();
    }

    public void setLocation(EntityPosition position) {
        this.positionIntern = position;
        this.setLocation((int) Math.round(this.positionIntern.getX() - this.getWidth() / 2),
            (int) Math.round(this.positionIntern.getY() - this.getHeight() / 2));
        //System.out.println("EntityGrafic.setLocation id: "+entity+" posIntern: "+this.positionIntern);
    }

    public EntityPosition getPositionExtern() {
        return this.positionExtern;
    }

    public EntityPosition getPositionIntern() {
        return this.positionIntern;
    }

    protected void paintComponent(Graphics g) {
        //System.out.println("EntityGrafic  posIntern: "+this.positionIntern+"  "+this.getLocation());
        this.setCode();
        if (this.positionIntern == null) {
            Point p = this.getLocation();
            this.positionIntern = new EntityPosition(p.getX(), p.getY(), 0.0, true);
        }
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform t = g2.getTransform();
        int width = this.getWidth();
        int height = this.getHeight();
        int width1 = this.entityType.getWidth();
        int height1 = this.entityType.getHeight();
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        g2.setFont(Grafic.FONT_DEFAULT);
        g2.setColor(Grafic.COLOR_BACKGROUND);
        g2.fillRect(0, 0, width, height);
        if ((this.entityType.getShow() & EntityType.SHOW_ICON) != 0) {
            //g2.rotate(angle, width/2, height/2);
            if (!this.positionIntern.getDirection()) {
                g2.scale(-1.0, 1.0);
                g2.translate(-width, 0);
            }
            g2.drawImage(this.image, (width - width1) / 2, 0, width1, height1, this);
            //g2.drawImage(this.image, (width-width1)/2, 0, width1, height-fm.getHeight(), this);
            g2.setTransform(t);
        }
        if ((this.entityType.getShow() & EntityType.SHOW_NAME) != 0) {
            g2.setColor(java.awt.Color.black);
            g2.drawString(this.code, (width - fm.stringWidth(code)) / 2, height - fm.getDescent());
        }
    }

    private void setPreferredSize() {
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        // width changed at 30.10.2011 by Chr.Mueller
        int width = 0;
        if (this.entityType.getShow() == EntityType.SHOW_NAME) {
            width = fm.stringWidth(this.code);
        } else if (this.entityType.getShow() == EntityType.SHOW_ICON) {
            width = this.entityType.getWidth();
        } else {
            width = Math.max(this.entityType.getWidth(), fm.stringWidth(this.code));
        }

        // height changed at 30.10.2011 by Chr.Mueller
        int height = 0;
        if ((this.entityType.getShow() & EntityType.SHOW_NAME) != 0) {
            height += fm.getHeight();
        }
        if ((this.entityType.getShow() & EntityType.SHOW_ICON) != 0) {
            height += this.entityType.getHeight();
        }
        Dimension d = new Dimension(width, height);
        this.setPreferredSize(d);
        this.setSize(d);
    }

    /**
     * Handler zum Anzeigen des InfoPane Event wird nur bearbeitet, wenn die Simulation angehalten ist Im anderen Fall
     * kann der Viewer (inbes. Applet) ueberlastet sein
     */
    public void mouseClicked(MouseEvent e) {
        //System.out.println("EntityGrafic.mouseClicked; "+this.entity.getId());
        ViewerPanel viewer = this.getEntity().getModel().getViewer();
        if (viewer != null && viewer.getSimulationThread() != null
            && !viewer.getSimulationThread().isWorking()) {
            // Event wird nur bearbeitet, wenn die Simulation angehalten ist
            // Im anderen Fall kann der Viewer (inbes. Applet) ueberlastet sein
            InfoPane infoPane = ViewerPanel.getInfoPane();
            if (infoPane != null) {
                infoPane.setVisible(true);
                infoPane.addEntity(this.entity.getId(), code);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
