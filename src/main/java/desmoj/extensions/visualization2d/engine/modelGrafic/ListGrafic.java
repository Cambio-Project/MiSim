package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.EntityType;
import desmoj.extensions.visualization2d.engine.model.List;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.util.MyTableUtilities;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * Grafic of a list
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
public class ListGrafic extends JPanel implements Grafic {

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
    private final boolean horizontal;
    private final Dimension entityDimension;
    private final int anzVisible;
    private final String defaultEntityTypeId;
    private final String code;
    private Dimension deltaSize;  // Abweichung von default Groesse der Grafic
    private final List list;
    private final String viewId;


    /**
     * @param list
     */
    public ListGrafic(List list, String viewId, Point pointExtern,
                      String defaultEntityTypeId, int anzVisible,
                      boolean horizontal, Dimension deltaSize) {

        this.list = list;
        String comment = list.getCommentText();
        JLabel commentLabel = null;
        if (comment != null) {
            if (horizontal) {
                comment = "<html><body>" + comment + "</body></html>";
            }
            commentLabel = new JLabel(comment);
            commentLabel.setFont(list.getCommentFont());
            commentLabel.setForeground(list.getCommentColor());
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
        this.deltaSize = new Dimension(0, 0);
        if (deltaSize != null) {
            this.deltaSize = deltaSize;
        }
        if (list.getName() != null) {
            this.code = this.list.getName();
        } else {
            this.code = "id: " + this.list.getId();
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

        if (this.list.getModel().getCoordinatenListener() != null) {
            this.addMouseListener(this.list.getModel().getCoordinatenListener());
            this.addMouseMotionListener(this.list.getModel().getCoordinatenListener());
            a.addMouseListener(this.list.getModel().getCoordinatenListener());
            a.addMouseMotionListener(this.list.getModel().getCoordinatenListener());
            b.addMouseListener(this.list.getModel().getCoordinatenListener());
            b.addMouseMotionListener(this.list.getModel().getCoordinatenListener());
        }


        if (this.horizontal) {
            p = new JScrollPane(a, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            javax.swing.JScrollBar sb = p.getHorizontalScrollBar();
            sb.setPreferredSize(new Dimension(sb.getPreferredSize().width, 5));
            contentPanel.add(t, BorderLayout.EAST);
            Dimension d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 25,
                this.entityDimension.height + this.deltaSize.height + 45);
            Dimension d1 = d;
            if (commentLabel != null && list.isCommentSizeExt()) {
                int r = this.getInsets().left + this.getInsets().right + commentLabel.getInsets().left +
                    commentLabel.getInsets().right;
                d1 = new Dimension(Math.max(d.width, commentLabel.getPreferredSize().width + r),
                    d.height + commentLabel.getPreferredSize().height);
            }
            this.setBounds(this.pointIntern.x - d1.width / 2, this.pointIntern.y - d1.height / 2, d1.width, d1.height);
        } else {
            p = new JScrollPane(a, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            javax.swing.JScrollBar sb = p.getVerticalScrollBar();
            sb.setPreferredSize(new Dimension(5, sb.getPreferredSize().height));
            contentPanel.add(t, BorderLayout.SOUTH);
            Dimension d = new Dimension(this.entityDimension.width + this.deltaSize.width + 65,
                (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 55);
            Dimension d1 = d;
            if (commentLabel != null && list.isCommentSizeExt()) {
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
     * get all views (viewId's) with Lists
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getLists().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            List list = model.getLists().get(ids[i]);
            ListGrafic listGrafic = (ListGrafic) list.getGrafic();
            if (listGrafic != null) {
                String viewId = listGrafic.getViewId();
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
     * @return smallest rectangle when an ListGrafic exist, null otherwise;
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getLists().getAllIds();
        for (int i = 0; i < id.length; i++) {
            List list = model.getLists().get(id[i]);
            ListGrafic listGrafic = (ListGrafic) list.getGrafic();
            if (listGrafic != null &&
                listGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = listGrafic.getBoundsExtern();
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
        //System.out.println("ListGrafic: BoundsExtern: "+r);
        return r;
    }

    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getLists().getAllIds();
        for (int i = 0; i < id.length; i++) {
            List list = model.getLists().get(id[i]);
            ListGrafic listGrafic = (ListGrafic) list.getGrafic();
            if (listGrafic != null &&
                listGrafic.getViewId().equals(viewId)) {
                listGrafic.transform();
                panel.add(listGrafic);
                //System.out.println("EntityGrafic.updateInit   "+list.getId());
            }
        }
    }

    public String getViewId() {
        return this.viewId;
    }

    public void transform() {
        Point p = list.getModel().getModelGrafic().
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

    public Rectangle getBoundsExtern() {
        Dimension d = new Dimension(this.getBounds().width, this.getBounds().height);
        return new Rectangle(this.pointExtern.x - d.width / 2,
            this.pointExtern.y - d.height / 2, d.width, d.height);
    }


    /**
     * update of ListGrafic content. Will be called from List.add and List.remove.
     */
    public void update() {
        //this.entityDimension = maxEntityDimension();
        this.setPreferredSize(getSize());
        b.removeAll();
        String[][] content = this.list.getContent();
        JComponent[][] bx = new JComponent[2][content.length];
        for (int i = 0; i < content.length; i++) {
            EntityGrafic en = (EntityGrafic) list.getModel().getEntities().get(content[i][0]).getGrafic();
            bx[0][i] = en;
            JLabel l = new JLabel(content[i][1]);
            l.setFont(Grafic.FONT_DEFAULT);
            bx[1][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, this.horizontal);
        if (this.list.getModel().getCoordinatenListener() != null) {
            tab.addMouseListener(this.list.getModel().getCoordinatenListener());
            tab.addMouseMotionListener(this.list.getModel().getCoordinatenListener());
        }
        b.add(tab);
        this.setPreferredSize(getSize());
        t.setText(content.length + "");
    }

    /**
     * compute max dimension of all entityGrafics in a listGrafic
     */
    private Dimension maxEntityDimension() {
        String[][] content = this.list.getContent();
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        EntityType defaultType = list.getModel().getEntityTyps().get(defaultEntityTypeId);
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
        for (int i = 0; i < content.length; i++) {
            desmoj.extensions.visualization2d.engine.model.Entity en = list.getModel().getEntities().get(content[i][0]);
            width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
            height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
        }
        return new Dimension(width, height);
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

}
