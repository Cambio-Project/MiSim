package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.model.Resource;
import desmoj.extensions.visualization2d.engine.util.MyTableUtilities;
import desmoj.extensions.visualization2d.engine.util.SpringUtilities;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * Grafic of Resource
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
public class ResourceGrafic extends JPanel implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    private final JScrollPane queuePane;
    private final JScrollPane processPane;
    private final JPanel queueFlow;
    private final JPanel queuePanel;
    private final JPanel processFlow;
    private final JPanel processPanel;
    private final JLabel queueLength;
    private final JLabel processLength;
    private final JLabel usedRes;
    private final boolean horizontal;
    private final Dimension entityDimension;
    private final int anzVisible;
    private final String defaultEntityTypeId;
    private final String code;
    private final Resource resource;
    private Dimension deltaSize;  // Abweichung von default Groesse der Grafic
    private final String viewId;


    /**
     * Constructor
     *
     * @param resource            resource instance
     * @param viewId              Id of view
     * @param pointExtern         middle point in resource panel
     * @param defaultEntityTypeId default entity type of process entities
     * @param anzVisible          No. of visible entities. When more, a scrollbar is used.
     * @param horizontal          true, when entities are horizontal arranged. false. when vertical
     * @param deltaSize           value to increment default size of grafic
     * @throws ModelException, when ?????????????
     */
    public ResourceGrafic(Resource resource, String viewId, Point pointExtern,
                          String defaultEntityTypeId, int anzVisible, boolean horizontal,
                          Dimension deltaSize) throws ModelException {

        this.resource = resource;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.pointExtern = pointExtern;
        this.transform();
        this.defaultEntityTypeId = defaultEntityTypeId;
        this.anzVisible = anzVisible;
        this.horizontal = horizontal;
        if (resource.getName() != null) {
            this.code = this.resource.getName();
        } else {
            this.code = "id: " + this.resource.getId();
        }
        this.entityDimension = this.maxEntityDimension();
        this.setBorder(BorderFactory.createTitledBorder(Grafic.Border_Default,
            this.code, TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION, Grafic.FONT_DEFAULT,
            Grafic.COLOR_BORDER));
        this.setLayout(new BorderLayout());
        this.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.deltaSize = new Dimension(0, 0);
        if (deltaSize != null) {
            this.deltaSize = deltaSize;
        }
        JPanel p = new JPanel();
        this.add(p, BorderLayout.CENTER);

        // process content initialization
        this.processFlow = new JPanel();
        this.processFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.processFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.processPanel = new JPanel();
        this.processFlow.add(this.processPanel);
        this.processPanel.setOpaque(false);
        this.processLength = new JLabel("0");
        this.processLength.setFont(Grafic.FONT_DEFAULT);
        this.processLength.setHorizontalAlignment(JLabel.CENTER);
        this.processLength.setForeground(Color.blue);
        this.processLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.processLength.setOpaque(true);

        // queue content initialization
        this.queueFlow = new JPanel();
        this.queueFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.queueFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.queuePanel = new JPanel();
        this.queueFlow.add(this.queuePanel);
        this.queuePanel.setOpaque(false);
        this.queueLength = new JLabel("0");
        this.queueLength.setFont(Grafic.FONT_DEFAULT);
        this.queueLength.setHorizontalAlignment(JLabel.CENTER);
        this.queueLength.setForeground(Color.blue);
        this.queueLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.queueLength.setOpaque(true);

        JPanel cornerQ = new JPanel();
        cornerQ.setLayout(new GridLayout(1, 1));
        cornerQ.setBackground(Grafic.COLOR_BACKGROUND);
        JPanel cornerP = new JPanel();
        cornerP.setLayout(new GridLayout(1, 1));
        cornerP.setBackground(Grafic.COLOR_BACKGROUND);
        this.usedRes = new JLabel("", JLabel.LEFT);
        this.usedRes.setFont(Grafic.FONT_DEFAULT);
        this.usedRes.setForeground(Color.blue);
        JLabel typeRes = new JLabel("Res. Type: " + this.resource.getResourceType(), JLabel.LEFT);
        typeRes.setFont(Grafic.FONT_DEFAULT);
        typeRes.setForeground(Color.blue);
        JPanel dataRes = new JPanel();
        dataRes.setBackground(Grafic.COLOR_BACKGROUND);
        dataRes.setOpaque(true);

        if (this.horizontal) {
            dataRes.setLayout(new GridLayout(2, 1));
            dataRes.add(this.usedRes);
            dataRes.add(typeRes);
            this.add(dataRes, BorderLayout.SOUTH);

            Box legendeQ = Box.createHorizontalBox();
            legendeQ.setBackground(Grafic.COLOR_BACKGROUND);
            legendeQ.setOpaque(true);
            JLabel legendeQ1Label = new JLabel("wait on Res.", JLabel.CENTER);
            legendeQ1Label.setFont(Grafic.FONT_DEFAULT);
            legendeQ1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeQ2Label = new JLabel("Rank-Entity-#Res", JLabel.CENTER);
            legendeQ2Label.setFont(Grafic.FONT_SMALL);
            legendeQ2Label.setUI(new VerticalLabelUI(false));
            legendeQ.add(legendeQ1Label);
            legendeQ.add(legendeQ2Label);

            Box legendeP = Box.createHorizontalBox();
            legendeP.setBackground(Grafic.COLOR_BACKGROUND);
            legendeP.setOpaque(true);
            JLabel legendeP1Label = new JLabel("Process", JLabel.CENTER);
            legendeP1Label.setFont(Grafic.FONT_DEFAULT);
            legendeP1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeP2Label = new JLabel("Index-Entity-#Res", JLabel.CENTER);
            legendeP2Label.setFont(Grafic.FONT_SMALL);
            legendeP2Label.setUI(new VerticalLabelUI(false));
            legendeP.add(legendeP1Label);
            legendeP.add(legendeP2Label);

            this.queuePane = new JScrollPane(this.queueFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.queuePane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerQ);
            JScrollBar sbQueue = this.queuePane.getHorizontalScrollBar();
            sbQueue.setPreferredSize(new Dimension(sbQueue.getPreferredSize().width, 5));

            this.processPane = new JScrollPane(this.processFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.processPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerP);
            JScrollBar sbProcess = this.processPane.getHorizontalScrollBar();
            sbProcess.setPreferredSize(new Dimension(sbProcess.getPreferredSize().width, 5));

            p.setLayout(new SpringLayout());
            p.add(legendeP);
            p.add(this.processPane);
            p.add(this.processLength);
            p.add(legendeQ);
            p.add(this.queuePane);
            p.add(this.queueLength);
            SpringUtilities.makeCompactGrid(p, 2, 3, 0, 0, 0, 0);

            Dimension d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 50,
                4 * this.entityDimension.height + this.deltaSize.height + 70);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        } else {
            dataRes.setLayout(new GridLayout(1, 2));
            this.usedRes.setUI(new VerticalLabelUI(false));
            typeRes.setUI(new VerticalLabelUI(false));
            dataRes.add(this.usedRes);
            dataRes.add(typeRes);
            this.add(dataRes, BorderLayout.EAST);

            Box legendeQ = Box.createVerticalBox();
            legendeQ.setBackground(Grafic.COLOR_BACKGROUND);
            legendeQ.setOpaque(true);
            JLabel legendeQ1Label = new JLabel("wait on Res.", JLabel.CENTER);
            legendeQ1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeQ2Label = new JLabel("#Res-Entity-Rank", JLabel.CENTER);
            legendeQ2Label.setFont(Grafic.FONT_SMALL);
            legendeQ.add(legendeQ1Label);
            legendeQ.add(legendeQ2Label);

            Box legendeP = Box.createVerticalBox();
            legendeP.setBackground(Grafic.COLOR_BACKGROUND);
            legendeP.setOpaque(true);
            JLabel legendeP1Label = new JLabel("Process", JLabel.CENTER);
            legendeP1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeP2Label = new JLabel("#Res-Entity-Index", JLabel.CENTER);
            legendeP2Label.setFont(Grafic.FONT_SMALL);
            legendeP.add(legendeP1Label);
            legendeP.add(legendeP2Label);

            this.queuePane = new JScrollPane(this.queueFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.queuePane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerQ);
            JScrollBar sbQueue = this.queuePane.getVerticalScrollBar();
            sbQueue.setPreferredSize(new Dimension(5, sbQueue.getPreferredSize().height));

            this.processPane = new JScrollPane(this.processFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.processPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerP);
            JScrollBar sbProcess = this.processPane.getVerticalScrollBar();
            sbProcess.setPreferredSize(new Dimension(5, sbProcess.getPreferredSize().height));

            p.setLayout(new SpringLayout());
            p.add(legendeQ);
            p.add(legendeP);
            p.add(this.queuePane);
            p.add(this.processPane);
            p.add(this.queueLength);
            p.add(this.processLength);
            SpringUtilities.makeCompactGrid(p, 3, 2, 0, 0, 0, 0);

            Dimension d = new Dimension((4 * this.entityDimension.width) + this.deltaSize.width + 50,
                (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 70);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        }
        this.update();
        if (this.resource.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.resource.getModel().getCoordinatenListener());
            this.addMouseListener(this.resource.getModel().getCoordinatenListener());
            this.queueFlow.addMouseMotionListener(this.resource.getModel().getCoordinatenListener());
            this.queueFlow.addMouseListener(this.resource.getModel().getCoordinatenListener());
            this.processFlow.addMouseMotionListener(this.resource.getModel().getCoordinatenListener());
            this.processFlow.addMouseListener(this.resource.getModel().getCoordinatenListener());
        }

    }

    /**
     * get all views (viewId's) with Resources
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getResources().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Resource resource = model.getResources().get(ids[i]);
            ResourceGrafic resourceGrafic = (ResourceGrafic) resource.getGrafic();
            if (resourceGrafic != null) {
                String viewId = resourceGrafic.getViewId();
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
     * Construct the smallest rectangle, which include the positions of all resources in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an ResourceGrafic exist, null otherwise.
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getResources().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Resource resource = model.getResources().get(id[i]);
            ResourceGrafic resourceGrafic = (ResourceGrafic) resource.getGrafic();
            if (resourceGrafic != null &&
                resourceGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = resourceGrafic.getBoundsExtern();
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
        //System.out.println("ResourceGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * Initialize resource panel
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getResources().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Resource res = model.getResources().get(id[i]);
            ResourceGrafic resGrafic = (ResourceGrafic) res.getGrafic();
            if (resGrafic != null &&
                resGrafic.getViewId().equals(viewId)) {
                resGrafic.transform();
                panel.add(resGrafic);
                //System.out.println("ResourceGrafic.updateInit   "+res.getId());
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
        Point p = resource.getModel().getModelGrafic().
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
        return new Rectangle(this.pointExtern.x, this.pointExtern.y, this.getWidth(), this.getHeight());
    }

    /**
     * update of ResourceGrafic content. Will be called from Resource.provide, Resource.takeProcess and
     * Resource.takeBack.
     *
     * @throws ModelException
     */
    public void update() throws ModelException {
        this.updateProcessPanel();
        this.updateQueuePanel();
    }

    /**
     * submethod of update
     *
     * @throws ModelException
     */
    private void updateProcessPanel() throws ModelException {
        this.processPanel.removeAll();
        this.processPanel.setLayout(new GridLayout(1, 1));
        int anzProcesEntry = resource.getProcessEntriesAnz();
        JComponent[][] bx = new JComponent[3][anzProcesEntry];

        // 1.Zeile RessourceAnzahl
        for (int i = 0; i < anzProcesEntry; i++) {
            int r = this.resource.getResourceEntriesAnz(i);
            JLabel l = new JLabel(Integer.toString(r));
            l.setFont(Grafic.FONT_DEFAULT);
            bx[0][i] = l;
        }
        // 2.Zeile ProcessEntities
        for (int i = 0; i < anzProcesEntry; i++) {
            String proc = this.resource.getProcessEntry(i);
            EntityGrafic en = (EntityGrafic) resource.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = en;
        }
        // 3.Zeile Index
        for (int i = 0; i < anzProcesEntry; i++) {
            JLabel l = new JLabel(Integer.toString(i));
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[2][i] = l;
        }

        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.resource.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.resource.getModel().getCoordinatenListener());
            tab.addMouseListener(this.resource.getModel().getCoordinatenListener());
        }
        processPanel.add(tab);


        this.setPreferredSize(getSize());
        this.processLength.setText(anzProcesEntry + "");
        String used = "Res. used: " + resource.getResourceUsed() + " of " + resource.getResourceTotal();
        this.usedRes.setText(used);

    }

    /**
     * submethod of update
     */
    private void updateQueuePanel() {
        // queuePanel
        String[][] queueContent = this.resource.getWaitingQueueContent();
        this.queuePanel.removeAll();
        //this.queuePanel.setLayout(new SpringLayout());
        this.queuePanel.setLayout(new GridLayout(1, 1));
        int anzQueueEntry = queueContent.length;
        JComponent[][] bx = new JComponent[3][anzQueueEntry];

        // 1.Zeile RessourceAnzahl
        for (int i = 0; i < anzQueueEntry; i++) {
            String r = queueContent[i][2];
            JLabel l = new JLabel(r);
            l.setFont(Grafic.FONT_DEFAULT);
            bx[0][i] = l;
        }
        // 2.Zeile ProcessEntities
        for (int i = 0; i < anzQueueEntry; i++) {
            String proc = queueContent[i][0];
            EntityGrafic en = (EntityGrafic) resource.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = en;
        }
        // 3.Zeile Rank
        for (int i = 0; i < anzQueueEntry; i++) {
            String rank = queueContent[i][1];
            JLabel l = new JLabel(rank);
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[2][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.resource.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.resource.getModel().getCoordinatenListener());
            tab.addMouseListener(this.resource.getModel().getCoordinatenListener());
        }
        queuePanel.add(tab);


        this.setPreferredSize(getSize());
        this.queueLength.setText(anzQueueEntry + "");
        String used = "Res. used: " + resource.getResourceUsed() + " of " + resource.getResourceTotal();
        this.usedRes.setText(used);
    }

    /**
     * compute max dimension of all entityGrafics in a ResourceGrafic
     *
     * @throws ModelException
     */
    private Dimension maxEntityDimension() throws ModelException {
        int width = resource.getModel().getEntityTyps().get(defaultEntityTypeId).getWidth();
        int height = resource.getModel().getEntityTyps().get(defaultEntityTypeId).getHeight();
        for (int i = 0; i < this.resource.getProcessEntriesAnz(); i++) {
            String proc = this.resource.getProcessEntry(i);
            desmoj.extensions.visualization2d.engine.model.Entity en = resource.getModel().getEntities().get(proc);
            width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
            height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
        }
        String[][] queueContent = this.resource.getWaitingQueueContent();
        for (int i = 0; i < queueContent.length; i++) {
            String proc = queueContent[i][0];
            desmoj.extensions.visualization2d.engine.model.Entity en = resource.getModel().getEntities().get(proc);
            width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
            height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
        }
        return new Dimension(width, height);
    }

    /**
     * Dispatches mouse event to EntityGrafic. Called from all mouse event handler.
     *
     * @param e
     */
    private void mouseEventHandler(MouseEvent e) {
        System.out.println("ResourceGrafic.mouseEventHandler x:" + e.getX() + " y:" + e.getY());
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
