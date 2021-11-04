package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.EntityType;
import desmoj.extensions.visualization2d.engine.model.List;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.Process;


/**
 * Grafic for a (old) process object
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
public class ProcessGrafic extends JPanel implements Grafic, MouseListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final Process process;
    private final String code;
    private final JPanel resContPanel;
    private final JPanel procContPanel;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    //private int 					x, y;
    private final String defaultEntityTypeId;
    private final int zeilen;
    private final String viewId;


    public ProcessGrafic(Process process, String viewId, Point pointExtern, String defaultEntityTypeId) {

        this.process = process;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.pointExtern = pointExtern;
        this.transform();
        if (process.getName() != null) {
            this.code = this.process.getName();
        } else {
            this.code = "id: " + this.process.getId();
        }
        this.defaultEntityTypeId = defaultEntityTypeId;
        Dimension d = this.maxEntityDimension();
        String[] resEn = this.process.getResourceEntity();
        String[] procEn = this.process.getProcessEntity();
        this.zeilen = (int) Math.rint(Math.signum((double) resEn.length) + Math.signum((double) procEn.length));
        this.setBounds(this.pointIntern.x, this.pointIntern.y,
            Math.max(100, (d.width * Math.max(resEn.length, procEn.length)) + 30), zeilen * d.height + 26);
        this.setBorder(BorderFactory.createTitledBorder(this.code));
        this.setLayout(new GridLayout(zeilen, 1));
        this.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.resContPanel = new JPanel();
        this.procContPanel = new JPanel();

        if (this.process.getResourceEntity().length > 0) {
            JPanel resPanel = new JPanel();
            resPanel.setLayout(new BorderLayout());
            resPanel.setOpaque(false);
            this.add(resPanel);
            JPanel resContPanel0 = new JPanel();
            resContPanel0.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            resContPanel0.setOpaque(false);
            resContPanel.setLayout(new GridLayout(1, resEn.length));
            resContPanel.setOpaque(false);
            resContPanel0.add(resContPanel);
            JLabel r = new JLabel("R:");
            r.setFont(Grafic.FONT_DEFAULT);
            resPanel.add(BorderLayout.WEST, r);
            resPanel.add(BorderLayout.CENTER, resContPanel0);
        }
        if (this.process.getProcessEntity().length > 0) {
            JPanel procPanel = new JPanel();
            procPanel.setLayout(new BorderLayout());
            procPanel.setOpaque(false);
            this.add(procPanel);
            JPanel procContPanel0 = new JPanel();
            procContPanel0.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            procContPanel0.setOpaque(false);
            procContPanel.setLayout(new GridLayout(1, procEn.length));
            procContPanel.setOpaque(false);
            procContPanel0.add(procContPanel);
            JLabel e = new JLabel("E:");
            e.setFont(Grafic.FONT_DEFAULT);
            procPanel.add(BorderLayout.WEST, e);
            procPanel.add(BorderLayout.CENTER, procContPanel0);
        }
        this.update();
        if (this.process.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
        }

    }

    /**
     * get all views (viewId's) with Process
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getProcesses().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Process process = model.getProcesses().get(ids[i]);
            ProcessGrafic processGrafic = (ProcessGrafic) process.getGrafic();
            if (processGrafic != null) {
                String viewId = processGrafic.getViewId();
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
     * Construct the smallest rectangle, which include the positions of all processElements  in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an ProcessGrafic exist, null otherwise
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getProcesses().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Process process = model.getProcesses().get(id[i]);
            ProcessGrafic processGrafic = (ProcessGrafic) process.getGrafic();
            if (processGrafic != null &&
                processGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = processGrafic.getBoundsExtern();
                minX = Math.min(minX, r.x);
                minY = Math.min(minY, r.y);
                maxX = Math.max(maxX, r.x + r.width);
                maxY = Math.max(maxY, r.y + r.height);
                //System.out.println(process.getId()+"  "+r.toString());
            }
        }
        //System.out.println("ProcessGrafic: min point: "+minX+", "+minY);
        //System.out.println("ProcessGrafic: max point: "+maxX+", "+maxY);
        Rectangle r = null;
        if (found) {
            r = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        //System.out.println("ProcessGrafic: BoundsExtern: "+r);
        return r;
    }

    public static void updateInit(Model model, String viewId, JComponent processPanel, JComponent linePanel) {
        ProcessLineList processLineList = null;
        linePanel.removeAll();
        String[] id = model.getProcesses().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Process process = model.getProcesses().get(id[i]);
            ProcessGrafic processGrafic = (ProcessGrafic) process.getGrafic();
            if (processGrafic != null &&
                processGrafic.getViewId().equals(viewId)) {
                processGrafic.transform();
                processPanel.add(processGrafic);
                processLineList = processGrafic.getProcessLineList();
                if (processLineList != null) {
                    linePanel.add(processLineList);
                }
                //System.out.println("ProcessGrafic.updateInit   "+list.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    public void transform() {
        Point p = process.getModel().getModelGrafic().
            transformToIntern(this.viewId, this.pointExtern);
        if (p == null) {
            this.pointIntern = this.pointExtern;
            this.printable = false;
        } else {
            this.pointIntern = p;
            this.printable = true;
        }
        this.setLocation(this.pointIntern.x - this.getWidth() / 2, this.pointIntern.y - this.getHeight() / 2);
    }

    public Rectangle getBoundsExtern() {
        return new Rectangle(this.pointExtern.x, this.pointExtern.y, this.getWidth(), this.getHeight());
    }


    /**
     * update of ProcessGrafic content. Will be called from Process.set and Process.unset
     */
    public void update() {
        String[] resEn = this.process.getResourceEntity();
        String[] procEn = this.process.getProcessEntity();
        this.resContPanel.removeAll();
        this.procContPanel.removeAll();
        for (int i = 0; i < resEn.length; i++) {
            if (!resEn[i].trim().equals("")) {
                resContPanel.add((EntityGrafic) process.getModel().getEntities().get(resEn[i]).getGrafic());
                //System.out.println(i+"  resContPanel  "+resEn[i]);
            } else {
                resContPanel.add(new JLabel(" "));
            }
        }
        for (int i = 0; i < procEn.length; i++) {
            if (!procEn[i].trim().equals("")) {
                procContPanel.add((EntityGrafic) process.getModel().getEntities().get(procEn[i]).getGrafic());
                //System.out.println(i+"  procContPanel  "+procEn[i]);
            } else {
                procContPanel.add(new JLabel(" "));
            }
        }
        Dimension d = this.maxEntityDimension();
        int w1 = Math.max(100, (d.width * Math.max(resEn.length, procEn.length)) + 30);
        int h1 = this.zeilen * d.height + 26;
        this.setBounds(this.pointIntern.x - w1 / 2, this.pointIntern.y - h1 / 2, w1, h1);
        this.setPreferredSize(getSize());
        this.validate();
        this.repaint();
    }

    /**
     * compute max dimension of all entityGrafics in a listGrafic
     */
    private Dimension maxEntityDimension() {
        String[] resEn = this.process.getResourceEntity();
        String[] procEn = this.process.getProcessEntity();
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);

        EntityType defaultType = process.getModel().getEntityTyps().get(defaultEntityTypeId);
        // width changed at 01.11.2011 by Chr.Mueller
        int width = 0;
        if (defaultType.getShow() == EntityType.SHOW_NAME) {
            width = fm.stringWidth(defaultType.getId());
        } else if (defaultType.getShow() == EntityType.SHOW_ICON) {
            width = defaultType.getWidth();
        } else {
            width = Math.max(defaultType.getWidth(), fm.stringWidth(defaultType.getId()));
        }
        // height changed at 31.10.2011 by Chr.Mueller
        int height = 0;
        if ((defaultType.getShow() & EntityType.SHOW_NAME) != 0) {
            height += fm.getHeight();
        }
        if ((defaultType.getShow() & EntityType.SHOW_ICON) != 0) {
            height += defaultType.getHeight();
        }
        for (int i = 0; i < resEn.length; i++) {
            if (!resEn[i].trim().equals("")) {
                desmoj.extensions.visualization2d.engine.model.Entity en =
                    process.getModel().getEntities().get(resEn[i]);
                width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
                height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
            }
        }
        for (int i = 0; i < procEn.length; i++) {
            if (!procEn[i].trim().equals("")) {
                desmoj.extensions.visualization2d.engine.model.Entity en =
                    process.getModel().getEntities().get(procEn[i]);
                width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
                height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
            }
        }
        //System.out.println("ProcessGrafic: "+this.process.getId()+" width: "+ width);
        return new Dimension(width, height);
    }

    public ProcessLineList getProcessLineList() {
        ProcessLineList out = null;
        Point processP = new Point(this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2);
        List li = process.getModel().getLists().get(this.process.getListId());
        //System.out.println(this.process.getId()+"   "+this.process.getListId());
        if (li != null) {
            ListGrafic listGrafic =
                (ListGrafic) process.getModel().getLists().get(this.process.getListId()).getGrafic();
            if (listGrafic != null) {
                Point list = new Point(listGrafic.getX() + listGrafic.getWidth() / 2,
                    listGrafic.getY() + listGrafic.getHeight() / 2);
                out = new ProcessLineList(processP, list);
            }
        }
        return out;
    }

    private void mouseEventHandler(MouseEvent e) {
        System.out.println("ListGrafic.mouseEventHandler x:" + e.getX() + " y:" + e.getY());
        java.awt.Component[] comps = this.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof EntityGrafic) {
                EntityGrafic en = (EntityGrafic) comps[i];
                if (en.getBounds().contains(e.getPoint())) {
                    en.dispatchEvent(e);
                    break;
                }
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


    class ProcessLineList extends JComponent {

        private final Point process;
        private final Point list;
        private final Point min;
        private final Point max;

        public ProcessLineList(Point process, Point list) {
            //System.out.println(process+"   "+list);
            this.process = process;
            this.list = list;
            this.min = new Point(Math.min(this.process.x, this.list.x), Math.min(this.process.y, this.list.y));
            this.max = new Point(Math.max(this.process.x, this.list.x), Math.max(this.process.y, this.list.y));
            Rectangle r = new Rectangle(min.x - 2, min.y - 2, max.x - min.x + 4, max.y - min.y + 4);
            this.setLayout(null);
            this.setBounds(r);
            this.setOpaque(false);

        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(COLOR_FOREGROUND);
            g2.drawLine(this.process.x - this.min.x + 2, this.process.y - this.min.y + 2, this.list.x - this.min.x + 2,
                this.list.y - this.min.y + 2);
        }
    }
}
