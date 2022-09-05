package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.EntityType;
import desmoj.extensions.visualization2d.engine.model.List;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ProcessNew;
import desmoj.extensions.visualization2d.engine.util.MyTableUtilities;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * Grafic of (new) process object
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
public class ProcessNewGrafic extends JPanel implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    private final JScrollPane p;
    private final JPanel a;
    private final JPanel b;
    private final JLabel t;
    private final JLabel usedRes;
    private final boolean horizontal;
    private final Dimension entityDimension;
    private final int anzVisible;
    private final String defaultEntityTypeId;
    private final String code;
    private final ProcessNew process;
    private int dh;  // zusaetzliche Hoehe bei anonymen Resourcen
    private Dimension deltaSize;  // Abweichung von default Groesse der Grafic
    private final boolean showResources; // Ressourcen sollen animiert werden
    private final String viewId;


    /**
     * Constructor
     *
     * @param process             process-instance
     * @param viewId              Id of view
     * @param pointExtern         middle point of grafic in process panel
     * @param defaultEntityTypeId default entity types used as process or resource entity
     * @param anzVisible          No. of visible entities. When more, a scrollbar is used.
     * @param horizontal          true, when entities are horizontal arranged. false. when vertical
     * @param showResources       true, to display required resources, false otherwise
     * @param deltaSize           value to increment default size of grafic
     */
    public ProcessNewGrafic(ProcessNew process, String viewId, Point pointExtern,
                            String defaultEntityTypeId, int anzVisible, boolean horizontal,
                            boolean showResources, Dimension deltaSize) {

        this.process = process;
        String comment = this.process.getCommentText();
        JLabel commentLabel = null;
        if (comment != null) {
            if (horizontal) {
                comment = "<html><body>" + comment + "</body></html>";
            }
            commentLabel = new JLabel(comment);
            commentLabel.setFont(process.getCommentFont());
            commentLabel.setForeground(process.getCommentColor());
        }
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.pointExtern = pointExtern;
        this.transform();
        this.defaultEntityTypeId = defaultEntityTypeId;
        this.anzVisible = anzVisible;
        this.horizontal = horizontal;
        if (process.getName() != null) {
            this.code = this.process.getName();
        } else {
            this.code = "id: " + this.process.getId();
        }
        this.entityDimension = this.maxEntityDimension();
        this.setBorder(BorderFactory.createTitledBorder(Grafic.Border_Default,
            this.code, TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION, Grafic.FONT_DEFAULT,
            Grafic.COLOR_BORDER));
        this.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        this.add(contentPanel, BorderLayout.CENTER);
        if (commentLabel != null) {
            if (horizontal) {
                this.add(commentLabel, BorderLayout.NORTH);
            } else {
                commentLabel.setUI(new VerticalLabelUI(false));
                this.add(commentLabel, BorderLayout.WEST);
            }
        }

        this.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.deltaSize = new Dimension(0, 0);
        if (deltaSize != null) {
            this.deltaSize = deltaSize;
        }
        this.showResources = showResources;

        a = new JPanel();
        a.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        a.setBackground(ModelGrafic.COLOR_BACKGROUND);
        b = new JPanel();
        a.add(b);
        b.setOpaque(false);
        b.setLayout(new GridLayout(1, 1));
        t = new JLabel("0");
        t.setFont(Grafic.FONT_DEFAULT);
        t.setHorizontalAlignment(JLabel.CENTER);
        t.setForeground(Color.blue);

        if (this.process.getModel().getCoordinatenListener() != null) {
            a.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
            a.addMouseListener(this.process.getModel().getCoordinatenListener());
            b.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
            b.addMouseListener(this.process.getModel().getCoordinatenListener());
        }

        JPanel corner = new JPanel();
        corner.setLayout(new GridLayout(1, 1));
        corner.setBackground(Grafic.COLOR_BACKGROUND);
        this.dh = 0;
        this.usedRes = new JLabel("", JLabel.RIGHT);
        this.usedRes.setFont(Grafic.FONT_DEFAULT);
        this.usedRes.setForeground(Color.blue);
        if (this.showResources && process.isAbstractResource()) {
            contentPanel.add(this.usedRes, BorderLayout.NORTH);
            this.dh = 15;
        }
        if (this.horizontal) {
            JLabel res = new JLabel("Res.", JLabel.RIGHT);
            res.setFont(Grafic.FONT_DEFAULT);
            res.setUI(new VerticalLabelUI(false));
            if (process.isAbstractResource()) {
                Dimension resD = res.getPreferredSize();
                res.setPreferredSize(new Dimension(resD.width, resD.height));
            } else {
                Dimension resD = res.getPreferredSize();
                res.setPreferredSize(new Dimension(resD.width, this.entityDimension.height));
            }

            JLabel proc = new JLabel("Proc.", JLabel.RIGHT);
            proc.setFont(Grafic.FONT_DEFAULT);
            proc.setUI(new VerticalLabelUI(false));
            Dimension procD = proc.getPreferredSize();
            proc.setPreferredSize(new Dimension(procD.width, this.entityDimension.height));

            Box headerH = Box.createVerticalBox();
            headerH.add(res);
            if (!process.isAbstractResource()) {
                headerH.add(Box.createVerticalGlue());
            }
            headerH.add(proc);
            headerH.setBackground(Grafic.COLOR_BACKGROUND);
            headerH.setOpaque(true);

            if (this.process.getModel().getCoordinatenListener() != null) {
                headerH.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
                headerH.addMouseListener(this.process.getModel().getCoordinatenListener());
            }


            p = new JScrollPane(a, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            if (this.showResources) {
                p.setRowHeaderView(headerH);
            }
            p.setCorner(JScrollPane.LOWER_LEFT_CORNER, corner);
            javax.swing.JScrollBar sb = p.getHorizontalScrollBar();
            sb.setPreferredSize(new Dimension(sb.getPreferredSize().width, 5));
            contentPanel.add(t, BorderLayout.EAST);
            Dimension d;
            if (this.showResources) {
                d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 60,
                    2 * this.entityDimension.height + this.deltaSize.height + this.dh + 45);
                //System.out.println(this.process.getId()+" entity:"+this.entityDimension.width+"  anz: "+this.anzVisible+"   delta: "+this.deltaSize.width);
            } else {
                d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 20,
                    this.entityDimension.height + this.deltaSize.height + this.dh + 45);
            }
            Dimension d1 = d;
            if (commentLabel != null && this.process.isCommentSizeExt()) {
                int r = this.getInsets().left + this.getInsets().right + commentLabel.getInsets().left +
                    commentLabel.getInsets().right;
                d1 = new Dimension(Math.max(d.width, commentLabel.getPreferredSize().width) + r,
                    d.height + commentLabel.getPreferredSize().height);
            }
            this.setBounds(this.pointIntern.x - d1.width / 2, this.pointIntern.y - d1.height / 2, d1.width, d1.height);

        } else {
            JLabel res = new JLabel("Res.", JLabel.LEFT);
            res.setFont(Grafic.FONT_DEFAULT);
            Dimension resD = res.getPreferredSize();
            res.setPreferredSize(new Dimension(this.entityDimension.width, resD.height));
            JLabel proc = new JLabel("Proc.", JLabel.LEFT);
            proc.setFont(Grafic.FONT_DEFAULT);
            Dimension procD = proc.getPreferredSize();
            proc.setPreferredSize(new Dimension(this.entityDimension.width, procD.height));
            JPanel headerV = new JPanel();
            headerV.setLayout(new GridLayout(1, 1));
            headerV.setBackground(Grafic.COLOR_BACKGROUND);
            headerV.setLayout(new FlowLayout(FlowLayout.CENTER));
            headerV.add(res);
            headerV.add(proc);

            if (this.process.getModel().getCoordinatenListener() != null) {
                headerV.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
                headerV.addMouseListener(this.process.getModel().getCoordinatenListener());
            }

            p = new JScrollPane(a, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            if (this.showResources) {
                p.setColumnHeaderView(headerV);
            }
            p.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
            javax.swing.JScrollBar sb = p.getVerticalScrollBar();
            sb.setPreferredSize(new Dimension(5, sb.getPreferredSize().height));
            contentPanel.add(t, BorderLayout.SOUTH);
            Dimension d;
            if (this.showResources) {
                d = new Dimension(2 * this.entityDimension.width + this.deltaSize.width + 75,
                    (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 70 + this.dh);
            } else {
                d = new Dimension(this.entityDimension.width + this.deltaSize.width + 20,
                    (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 45 + this.dh);
            }
            Dimension d1 = d;
            if (commentLabel != null && this.process.isCommentSizeExt()) {
                int r = this.getInsets().top + this.getInsets().bottom + commentLabel.getInsets().top +
                    commentLabel.getInsets().bottom;
                d1 = new Dimension(d.width + commentLabel.getPreferredSize().width,
                    Math.max(d.height, commentLabel.getPreferredSize().height + r));
            }
            this.setBounds(this.pointIntern.x - d1.width / 2, this.pointIntern.y - d1.height / 2, d1.width, d1.height);
        }
        contentPanel.add(p, BorderLayout.CENTER);
        this.update();
    }

    /**
     * get all views (viewId's) with Process
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getProcessNewes().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            ProcessNew process = model.getProcessNewes().get(ids[i]);
            ProcessNewGrafic processGrafic = (ProcessNewGrafic) process.getGrafic();
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
     * Construct the smallest rectangle, which include the positions of all listElements  in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an ProcessNewGrafic exist, null otherwise.
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getProcessNewes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            ProcessNew process = model.getProcessNewes().get(id[i]);
            ProcessNewGrafic processGrafic = (ProcessNewGrafic) process.getGrafic();
            if (processGrafic != null &&
                processGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = processGrafic.getBoundsExtern();
                minX = Math.min(minX, r.x);
                minY = Math.min(minY, r.y);
                maxX = Math.max(maxX, r.x + r.width);
                maxY = Math.max(maxY, r.y + r.height);
                //System.out.println(list.getId()+"  "+r.toString());
            }
        }
        //System.out.println("ListGrafic: min point: "+minX+", "+minY);
        //System.out.println("ListGrafic: max point: "+maxX+", "+maxY);
        Rectangle r = null;
        if (found) {
            r = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        //System.out.println("ProcessNewGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * initialize process- and line-panel
     *
     * @param panel
     * @param linePanel
     */
    public static void updateInit(Model model, String viewId, JComponent processPanel, JComponent linePanel) {
        ProcessLineList processLineList = null;
        linePanel.removeAll();
        String[] id = model.getProcessNewes().getAllIds();
        for (int i = 0; i < id.length; i++) {
            ProcessNew process = model.getProcessNewes().get(id[i]);
            ProcessNewGrafic processGrafic = (ProcessNewGrafic) process.getGrafic();
            if (processGrafic != null &&
                processGrafic.getViewId().equals(viewId)) {
                processGrafic.transform();
                processPanel.add(processGrafic);
                processLineList = processGrafic.getProcessLineList();
                if (processLineList != null) {
                    linePanel.add(processLineList);
                }
                //System.out.println("ProcessNewGrafic.updateInit   "+list.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    /**
     * transforms middle point to internal coordinates
     */
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
        //System.out.println("ListGrafic id: "+list.getId()+"  "+this.pointIntern.toString());
    }

    /**
     * get bounds of grafic in external coordinates
     *
     * @return
     */
    public Rectangle getBoundsExtern() {
        Dimension d = new Dimension(this.getBounds().width, this.getBounds().height);
        return new Rectangle(this.pointExtern.x - d.width / 2,
            this.pointExtern.y - d.height / 2, d.width, d.height);
    }


    /**
     * update of ProcessNewGrafic content. Will be called from ProcessNew.addEntry and ProcessNew.removeEntry.
     */
    public void update() {
        //this.entityDimension = maxEntityDimension();
        b.removeAll();
        //b.setLayout(new SpringLayout());
        int anzEntry = this.process.getEntriesAnz();
        JComponent[][] bx = new JComponent[2][anzEntry]; // ohne Res
        JComponent[][] bx1 = new JComponent[3][anzEntry]; // mit Res

        if (this.showResources) {
            // 1.line RessourceAnzahl bzw. ResourceEntities
            for (int i = 0; i < anzEntry; i++) {
                if (this.process.isAbstractResource()) {
                    int r = this.process.getResourceEntriesAnz(i);
                    JLabel l = new JLabel(r + " " + this.process.getResourceType());
                    l.setFont(Grafic.FONT_DEFAULT);
                    //if(!this.horizontal) l.setUI(new VerticalLabelUI(false));
                    bx1[0][i] = l;
                } else {
                    String[] res = this.process.getResourceEntries(i);
                    if (res.length > 0) {
                        // Es gibt Resourcen
                        JComponent[][] bxRes = new JComponent[1][res.length];
                        for (int j = 0; j < res.length; j++) {
                            EntityGrafic en = (EntityGrafic) process.getModel().getEntities().get(res[j]).getGrafic();
                            bxRes[0][j] = en;
                        }
                        bx1[0][i] = new MyTableUtilities().makeCompactGrid(bxRes, horizontal);
                    } else {
                        // Es gibt keine Resourcen. Es wird nur ein Dummy eingefuegt.
                        JComponent[][] bxRes = new JComponent[1][1];
                        bxRes[0][0] = new Dummy();
                        bx1[0][i] = new MyTableUtilities().makeCompactGrid(bxRes, horizontal);
                    }
                }
            }
        }
        // 2.line ProcessEntities
        for (int i = 0; i < anzEntry; i++) {
            String[] procs = this.process.getProcessEntries(i);
            if (procs.length > 0) {
                JComponent[][] bxProcs = new JComponent[1][procs.length];
                for (int j = 0; j < procs.length; j++) {
                    EntityGrafic en = (EntityGrafic) process.getModel().getEntities().get(procs[j]).getGrafic();
                    bxProcs[0][j] = en;
                }
                JComponent tab = new MyTableUtilities().makeCompactGrid(bxProcs, horizontal);
                bx[0][i] = tab;
                bx1[1][i] = tab;
            } else {
                JComponent[][] bxProcs = new JComponent[1][1];
                bxProcs[0][0] = new Dummy();
                JComponent tab = new MyTableUtilities().makeCompactGrid(bxProcs, horizontal);
                bx[0][i] = tab;
                bx1[1][i] = tab;
            }
        }
        // 3.line Index
        for (int i = 0; i < anzEntry; i++) {
            JLabel l = new JLabel(Integer.toString(i));
            l.setFont(Grafic.FONT_DEFAULT);
            bx[1][i] = l;
            bx1[2][i] = l;

        }

        JComponent tab;
        if (this.showResources) {
            tab = new MyTableUtilities().makeCompactGrid(bx1, this.horizontal);
        } else {
            tab = new MyTableUtilities().makeCompactGrid(bx, this.horizontal);
        }
        if (this.process.getModel().getCoordinatenListener() != null) {
            tab.addMouseListener(this.process.getModel().getCoordinatenListener());
            tab.addMouseMotionListener(this.process.getModel().getCoordinatenListener());
        }
        b.add(tab);

        this.setPreferredSize(getSize());
        t.setText(anzEntry + "");
        if (this.process.isAbstractResource()) {
            String used = "Res. used: " + process.getResourceUsed() + " of " + process.getResourceTotal();
            this.usedRes.setText(used);
        }
    }

    /**
     * compute max dimension of all entityGrafics in a ProcessNewGrafic
     */
    private Dimension maxEntityDimension() {
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
        for (int i = 0; i < this.process.getEntriesAnz(); i++) {
            String[] procs = this.process.getProcessEntries(i);
            for (int j = 0; j < procs.length; j++) {
                desmoj.extensions.visualization2d.engine.model.Entity en =
                    process.getModel().getEntities().get(procs[j]);
                width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
                height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
            }
            if (!this.process.isAbstractResource()) {
                String[] res = this.process.getResourceEntries(i);
                for (int j = 0; j < res.length; j++) {
                    desmoj.extensions.visualization2d.engine.model.Entity en =
                        process.getModel().getEntities().get(res[j]);
                    width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
                    height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
                }
            }
        }
        return new Dimension(width, height);
    }

    /**
     * get an instance of ProcessLineList to paint a line between this processGrafic the list with id
     * this.process.getListId()
     *
     * @return
     */
    public ProcessLineList getProcessLineList() {
        ProcessLineList out = null;
        List li = null;
        Point processP = new Point(this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2);
        if (this.process.getListId() != null) {
            li = process.getModel().getLists().get(this.process.getListId());
        }
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


    /**
     * Dispatches mouse event to EntityGrafic. Called from all mouse event handler.
     *
     * @param e
     */
    private void mouseEventHandler(MouseEvent e) {
        System.out.println("ProcessNewGrafic.mouseEventHandler x:" + e.getX() + " y:" + e.getY());
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


    /**
     * Inner class to paint a line the middle point of process and line
     *
     * @author tian
     */
    class ProcessLineList extends JComponent {

        private final Point process;
        private final Point list;
        private final Point min;
        private final Point max;

        public ProcessLineList(Point process, Point list) {
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

    /**
     * Dummy, used in update()
     *
     * @author Christian
     */
    class Dummy extends JComponent {

        protected Dummy() {
            this.setLayout(null);
            this.setOpaque(false);
            this.setSize(maxEntityDimension());
        }
    }

}
	
