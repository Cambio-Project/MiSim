package desmoj.extensions.visualization2d.engine.modelGrafic;


import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.BackgroundLine;
import desmoj.extensions.visualization2d.engine.model.Model;


/**
 * The Constructor is called by BackgroundElement.createGrafic
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
public class BackgroundLineGrafic extends JComponent implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Dimension defaultSize = new Dimension(10, 10);

    private final BackgroundLine backgroundLine;
    private final Color color;
    private Dimension sizeExtern, sizeIntern;                    // Position (2. Alternative)
    private final String viewId;
    private final Point[] pointsIntern;
    private final Point[] pointsExtern;

    /**
     * Constructor with all features There a 2 possible cases: (topLeft and bottomRight are both null) xor (middle and
     * size are null) When in backgroundElement a name is set, then a border with the name is displayed.
     *
     * @param backgroundElement
     * @param viewId            Id of view
     * @param topLeft           topLeft corner
     * @param bottomRight       bottomRight corner
     * @param middle            middlePoint
     * @param size              fixed size of element
     * @param textColor         color of text
     * @param background        background color, when null then transparent
     */
    public BackgroundLineGrafic(BackgroundLine backgroundLine, String viewId,
                                Point[] points, Color color) {
        //System.out.println("start BackgroundLineGrafic");
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.backgroundLine = backgroundLine;
        this.color = color;
        this.setOpaque(true);
        this.pointsExtern = points;
        this.pointsIntern = new Point[this.pointsExtern.length];
        this.transform();
        this.repaint();
        //System.out.println("end BackgroundLineGrafic");
    }

    /**
     * get all views (viewId's) with BackgroundLines
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getBackgroundLines().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            BackgroundLine bg = model.getBackgroundLines().get(ids[i]);
            BackgroundLineGrafic bgg = (BackgroundLineGrafic) bg.getGrafic();
            String viewId = bgg.getViewId();
            views.add(viewId);
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
     * Construct the smallest rectangle, which include the positions of all entities  in view
     *
     * @param viewId id of view
     * @return smallest Rectangle, when an background element exist. null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        double minX = (double) Integer.MAX_VALUE / 2;
        double minY = (double) Integer.MAX_VALUE / 2;
        double maxX = (double) Integer.MIN_VALUE / 2;
        double maxY = (double) Integer.MIN_VALUE / 2;
        String[] id = model.getBackgroundLines().getAllIds();
        for (int i = 0; i < id.length; i++) {
            BackgroundLine backgroundLine = model.getBackgroundLines().get(id[i]);
            BackgroundLineGrafic backgroundLineGrafic = (BackgroundLineGrafic) backgroundLine.getGrafic();
            if (backgroundLineGrafic != null &&
                backgroundLineGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = backgroundLineGrafic.getBoundsExtern();
                minX = Math.floor(Math.min(minX, r.getX()));
                minY = Math.floor(Math.min(minY, r.getY()));
                maxX = Math.ceil(Math.max(maxX, r.getX() + r.width));
                maxY = Math.ceil(Math.max(maxY, r.getY() + r.height));
                //System.out.println(backgroundLine.getId()+"   "+backgroundLineGrafic.getBoundsExtern().toString());
            }
        }
        Rectangle r = null;
        if (found) {
            r = new Rectangle((int) Math.round(minX), (int) Math.round(minY), (int) Math.round(maxX - minX),
                (int) Math.round(maxY - minY));
        }
        //System.out.println("BackgroundLineGrafic  BoundsExtern: "+r);
        return r;
    }

    /**
     * called by ViewGrafic.updateInit
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        //System.out.println("in BackgroundLine.updateInit");

        // baue TreeSet tmp zur Sortierung der Abarbeitung auf
        TreeMap<Double, TreeSet<String>> tmp = new TreeMap<Double, TreeSet<String>>();
        String[] id = model.getBackgroundLines().getAllIds();
        for (int i = 0; i < id.length; i++) {
            BackgroundLine backgroundLine = model.getBackgroundLines().get(id[i]);
            Double level = new Double(backgroundLine.getLevel());
            TreeSet<String> tmpSet = tmp.get(level);
            if (tmpSet == null) {
                tmpSet = new TreeSet<String>();
            }
            tmpSet.add(id[i]);
            tmp.put(level, tmpSet);
        }

        // Abarbeitung des  TreeSet
        Iterator<Double> enLevel = tmp.keySet().iterator();
        while (enLevel.hasNext()) {
            TreeSet<String> tmpSet = tmp.get(enLevel.next());
            Iterator<String> enId = tmpSet.iterator();
            while (enId.hasNext()) {
                BackgroundLine backgroundLine = model.getBackgroundLines().get(enId.next());
                BackgroundLineGrafic backgroundLineGrafic = (BackgroundLineGrafic) backgroundLine.getGrafic();
                if (backgroundLineGrafic != null &&
                    backgroundLineGrafic.getViewId().equals(viewId)) {
                    backgroundLineGrafic.transform();
                    panel.add(backgroundLineGrafic);
                }
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    /**
     * transforms from external to internal coordinate system
     */
    public void transform() {
        for (int i = 0; i < this.pointsIntern.length; i++) {
            this.pointsIntern[i] = backgroundLine.getModel().getModelGrafic().
                transformToIntern(this.viewId, this.pointsExtern[i]);
            //System.out.println(i+"   "+this.pointsIntern[i]+"   "+this.viewId+"   "+this.pointsExtern[i]);
        }
        this.setBounds();
    }

    /**
     * sets bounds of background element
     */
    private void setBounds() {
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, width = 0, height = 0;
        boolean valid = true;
        for (int i = 0; i < this.pointsIntern.length; i++) {
            if (this.pointsIntern[i] != null) {
                x = Math.min(x, this.pointsIntern[i].x);
                y = Math.min(y, this.pointsIntern[i].y);
            } else {
                valid = false;
            }
        }
        if (valid) {
            for (int i = 0; i < this.pointsIntern.length; i++) {
                width = Math.max(width, this.pointsIntern[i].x - x);
                height = Math.max(height, this.pointsIntern[i].y - y);
            }
            width += backgroundLine.getLineSize();
            height += backgroundLine.getLineSize();
            this.setBounds(0, 0, x + width, y + height);
            this.sizeIntern = new Dimension(width, height);
        }
        //System.out.println("Size BackgroundLine: "+this.getBounds());
    }

    private void setPreferredSize() {
        this.setPreferredSize(this.sizeIntern);
        this.setSize(this.sizeIntern);
    }

    /**
     * get external size
     *
     * @return
     */
    public Rectangle getBoundsExtern() {
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, width = 0, height = 0;
        for (int i = 0; i < this.pointsExtern.length; i++) {
            x = Math.min(x, this.pointsExtern[i].x);
            y = Math.min(y, this.pointsExtern[i].y);
        }
        for (int i = 0; i < this.pointsExtern.length; i++) {
            width = Math.max(width, this.pointsExtern[i].x - x);
            height = Math.max(height, this.pointsExtern[i].y - y);
        }
        return new Rectangle(x, y, width, height);
    }

    public void paintComponent(Graphics g) {
        //System.out.println("start paintComponent");
        int[] xPoints = new int[this.pointsIntern.length];
        for (int i = 0; i < this.pointsIntern.length; i++) {
            xPoints[i] = this.pointsIntern[i].x;
        }
        int[] yPoints = new int[this.pointsIntern.length];
        for (int i = 0; i < this.pointsIntern.length; i++) {
            yPoints[i] = this.pointsIntern[i].y;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(
            new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        g2.setColor(this.color);
        g2.setStroke(new BasicStroke(backgroundLine.getLineSize()));
        g2.drawPolyline(xPoints, yPoints, pointsIntern.length);

        //System.out.println("end paintComponent");
    }

}
