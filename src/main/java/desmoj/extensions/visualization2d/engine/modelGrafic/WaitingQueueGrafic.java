package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
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
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.ModelException;
import desmoj.extensions.visualization2d.engine.model.WaitingQueue;
import desmoj.extensions.visualization2d.engine.util.MyTableUtilities;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * Grafic of WaitingQueue
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
public class WaitingQueueGrafic extends JPanel implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    private final JScrollPane masterWaitPane;
    private final JScrollPane slaveWaitPane;
    private final JScrollPane cooperationPane;
    private final JPanel masterWaitFlow;
    private final JPanel masterWaitPanel;
    private final JPanel slaveWaitFlow;
    private final JPanel slaveWaitPanel;
    private final JPanel cooperationFlow;
    private final JPanel cooperationPanel;
    private final JLabel masterWaitLength;
    private final JLabel slaveWaitLength;
    private final JLabel noOfCooperations;
    private final boolean horizontal;
    private final Dimension entityDimension;
    private final int anzVisible;
    private final String defaultEntityTypeId;
    private final String code;
    private final WaitingQueue waitingQueue;
    private Dimension deltaSize;  // Abweichung von default Groesse der Grafic
    private final String viewId;


    public WaitingQueueGrafic(WaitingQueue waitingQueue, String viewId, Point pointExtern,
                              String defaultEntityTypeId, int anzVisible, boolean horizontal,
                              Dimension deltaSize) throws ModelException {

        this.waitingQueue = waitingQueue;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.pointExtern = pointExtern;
        this.transform();
        this.defaultEntityTypeId = defaultEntityTypeId;
        this.anzVisible = anzVisible;
        this.horizontal = horizontal;
        if (waitingQueue.getName() != null) {
            this.code = this.waitingQueue.getName();
        } else {
            this.code = "id: " + this.waitingQueue.getId();
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

        // masterWait content initialization
        this.masterWaitFlow = new JPanel();
        this.masterWaitFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.masterWaitFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.masterWaitPanel = new JPanel();
        this.masterWaitFlow.add(this.masterWaitPanel);
        this.masterWaitPanel.setOpaque(false);
        this.masterWaitLength = new JLabel("0");
        this.masterWaitLength.setFont(Grafic.FONT_DEFAULT);
        this.masterWaitLength.setHorizontalAlignment(JLabel.CENTER);
        this.masterWaitLength.setForeground(Color.blue);
        this.masterWaitLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.masterWaitLength.setOpaque(true);

        // slaveWait content initialization
        this.slaveWaitFlow = new JPanel();
        this.slaveWaitFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.slaveWaitFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.slaveWaitPanel = new JPanel();
        this.slaveWaitFlow.add(this.slaveWaitPanel);
        this.slaveWaitPanel.setOpaque(false);
        this.slaveWaitLength = new JLabel("0");
        this.slaveWaitLength.setFont(Grafic.FONT_DEFAULT);
        this.slaveWaitLength.setHorizontalAlignment(JLabel.CENTER);
        this.slaveWaitLength.setForeground(Color.blue);
        this.slaveWaitLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.slaveWaitLength.setOpaque(true);

        // cooperation content initialization
        this.cooperationFlow = new JPanel();
        this.cooperationFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.cooperationFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.cooperationPanel = new JPanel();
        this.cooperationFlow.add(this.cooperationPanel);
        this.cooperationPanel.setOpaque(false);

        JPanel cornerM = new JPanel();
        cornerM.setLayout(new GridLayout(1, 1));
        cornerM.setBackground(Grafic.COLOR_BACKGROUND);
        JPanel cornerS = new JPanel();
        cornerS.setLayout(new GridLayout(1, 1));
        cornerS.setBackground(Grafic.COLOR_BACKGROUND);
        JPanel cornerC = new JPanel();
        cornerS.setLayout(new GridLayout(1, 1));
        cornerC.setBackground(Grafic.COLOR_BACKGROUND);

        if (this.horizontal) {
            Box legendeSlaveWait = Box.createHorizontalBox();
            legendeSlaveWait.setBackground(Grafic.COLOR_BACKGROUND);
            legendeSlaveWait.setOpaque(true);
            JLabel legendeSlaveWait1Label = new JLabel("Slave", JLabel.CENTER);
            legendeSlaveWait1Label.setFont(Grafic.FONT_DEFAULT);
            legendeSlaveWait1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeSlaveWait2Label = new JLabel("Rank-#Prod", JLabel.CENTER);
            legendeSlaveWait2Label.setFont(Grafic.FONT_DEFAULT);
            legendeSlaveWait2Label.setUI(new VerticalLabelUI(false));
            legendeSlaveWait.add(legendeSlaveWait1Label);

            Box legendeMasterWait = Box.createHorizontalBox();
            legendeMasterWait.setBackground(Grafic.COLOR_BACKGROUND);
            legendeMasterWait.setOpaque(true);
            JLabel legendeMasterWait1Label = new JLabel("Master", JLabel.CENTER);
            legendeMasterWait1Label.setFont(Grafic.FONT_DEFAULT);
            legendeMasterWait1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeMasterWait2Label = new JLabel("Rank-#Prod", JLabel.CENTER);
            legendeMasterWait2Label.setFont(Grafic.FONT_DEFAULT);
            legendeMasterWait2Label.setUI(new VerticalLabelUI(false));
            legendeMasterWait.add(legendeMasterWait1Label);

            Box legendeCooperation = Box.createHorizontalBox();
            legendeCooperation.setBackground(Grafic.COLOR_BACKGROUND);
            legendeCooperation.setOpaque(true);
            this.noOfCooperations = new JLabel("Cooperations: 0", JLabel.CENTER);
            this.noOfCooperations.setFont(Grafic.FONT_DEFAULT);
            this.noOfCooperations.setUI(new VerticalLabelUI(false));
            legendeCooperation.add(this.noOfCooperations);

            this.slaveWaitPane = new JScrollPane(this.slaveWaitFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.slaveWaitPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerS);
            JScrollBar sbSlave = this.slaveWaitPane.getHorizontalScrollBar();
            sbSlave.setPreferredSize(new Dimension(sbSlave.getPreferredSize().width, 5));

            this.masterWaitPane = new JScrollPane(this.masterWaitFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.masterWaitPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerM);
            JScrollBar sbMaster = this.masterWaitPane.getHorizontalScrollBar();
            sbMaster.setPreferredSize(new Dimension(sbMaster.getPreferredSize().width, 5));

            this.cooperationPane = new JScrollPane(this.cooperationFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.cooperationPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerC);
            int h = this.cooperationPane.getPreferredSize().height;
            this.cooperationPane.setPreferredSize(new Dimension(this.anzVisible * 10, h));
            JScrollBar sbCooperation = this.cooperationPane.getHorizontalScrollBar();
            sbCooperation.setPreferredSize(new Dimension(sbCooperation.getPreferredSize().width, 5));

            JPanel p1 = new JPanel(new GridLayout(2, 1));
            JPanel p2 = new JPanel(new BorderLayout());
            p2.add(legendeMasterWait, BorderLayout.WEST);
            p2.add(this.masterWaitPane, BorderLayout.CENTER);
            p2.add(this.masterWaitLength, BorderLayout.EAST);
            p1.add(p2);
            JPanel p3 = new JPanel(new BorderLayout());
            p3.add(legendeSlaveWait, BorderLayout.WEST);
            p3.add(this.slaveWaitPane, BorderLayout.CENTER);
            p3.add(this.slaveWaitLength, BorderLayout.EAST);
            p1.add(p3);

            JPanel p4 = new JPanel();
            p4.setLayout(new BorderLayout());
            p4.add(legendeCooperation, BorderLayout.WEST);
            p4.add(this.cooperationPane, BorderLayout.CENTER);
            p.setLayout(new BorderLayout());
            p.add(p4, BorderLayout.WEST);
            p.add(p1, BorderLayout.CENTER);

            Dimension d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 70,
                2 * this.entityDimension.height + this.deltaSize.height + 70);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        } else {
            Box legendeSlaveWait = Box.createVerticalBox();
            legendeSlaveWait.setBackground(Grafic.COLOR_BACKGROUND);
            legendeSlaveWait.setOpaque(true);
            JLabel legendeSlaveWait1Label = new JLabel("Slave", JLabel.CENTER);
            legendeSlaveWait1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeSlaveWait2Label = new JLabel("#Prod-Rank", JLabel.CENTER);
            legendeSlaveWait2Label.setFont(Grafic.FONT_DEFAULT);
            legendeSlaveWait.add(legendeSlaveWait1Label);

            Box legendeMasterWait = Box.createVerticalBox();
            legendeMasterWait.setBackground(Grafic.COLOR_BACKGROUND);
            legendeMasterWait.setOpaque(true);
            JLabel legendeMasterWait1Label = new JLabel("Master", JLabel.CENTER);
            legendeMasterWait1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeMasterWait2Label = new JLabel("#Prod-Rank", JLabel.CENTER);
            legendeMasterWait2Label.setFont(Grafic.FONT_DEFAULT);
            legendeMasterWait.add(legendeMasterWait1Label);

            Box legendeCooperation = Box.createVerticalBox();
            legendeCooperation.setBackground(Grafic.COLOR_BACKGROUND);
            legendeCooperation.setOpaque(true);
            legendeCooperation.setAlignmentX(CENTER_ALIGNMENT);
            this.noOfCooperations = new JLabel("Cooperations : 0", JLabel.CENTER);
            this.noOfCooperations.setFont(Grafic.FONT_DEFAULT);
            legendeCooperation.add(this.noOfCooperations);

            this.slaveWaitPane = new JScrollPane(this.slaveWaitFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.slaveWaitPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerS);
            JScrollBar sbSlave = this.slaveWaitPane.getVerticalScrollBar();
            sbSlave.setPreferredSize(new Dimension(5, sbSlave.getPreferredSize().height));

            this.masterWaitPane = new JScrollPane(this.masterWaitFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.masterWaitPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerM);
            JScrollBar sbMaster = this.masterWaitPane.getVerticalScrollBar();
            sbMaster.setPreferredSize(new Dimension(5, sbMaster.getPreferredSize().height));

            this.cooperationPane = new JScrollPane(this.cooperationFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.cooperationPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerC);
            int w = this.cooperationPane.getPreferredSize().width;
            this.cooperationPane.setPreferredSize(new Dimension(w, this.anzVisible * 10));
            JScrollBar sbCooperation = this.cooperationPane.getVerticalScrollBar();
            sbCooperation.setPreferredSize(new Dimension(5, sbCooperation.getPreferredSize().height));

            JPanel p1 = new JPanel(new GridLayout(1, 2));
            JPanel p2 = new JPanel(new BorderLayout());
            p2.add(legendeMasterWait, BorderLayout.NORTH);
            p2.add(this.masterWaitPane, BorderLayout.CENTER);
            p2.add(this.masterWaitLength, BorderLayout.SOUTH);
            p1.add(p2);
            JPanel p3 = new JPanel(new BorderLayout());
            p3.add(legendeSlaveWait, BorderLayout.NORTH);
            p3.add(this.slaveWaitPane, BorderLayout.CENTER);
            p3.add(this.slaveWaitLength, BorderLayout.SOUTH);
            p1.add(p3);

            JPanel p4 = new JPanel();
            p4.setLayout(new BorderLayout());
            p4.add(legendeCooperation, BorderLayout.NORTH);
            p4.add(this.cooperationPane, BorderLayout.CENTER);
            p.setLayout(new BorderLayout());
            p.add(p4, BorderLayout.NORTH);
            p.add(p1, BorderLayout.CENTER);


            Dimension d = new Dimension((2 * this.entityDimension.width) + this.deltaSize.width + 80,
                (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 100);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        }
        this.update();
        if (this.waitingQueue.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            this.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
            p.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            p.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
            this.masterWaitFlow.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            this.masterWaitFlow.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
            this.slaveWaitFlow.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            this.slaveWaitFlow.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
        }

    }

    /**
     * get all views (viewId's) with WaitingQueue
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getWaitingQueues().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            WaitingQueue waitingQueue = model.getWaitingQueues().get(ids[i]);
            WaitingQueueGrafic waitingQueueGrafic = (WaitingQueueGrafic) waitingQueue.getGrafic();
            if (waitingQueueGrafic != null) {
                String viewId = waitingQueueGrafic.getViewId();
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
     * Construct the smallest rectangle, which include the positions of all waiting queues in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an WaitingQueueGrafic exist, null otherwise.
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getWaitingQueues().getAllIds();
        for (int i = 0; i < id.length; i++) {
            WaitingQueue waitingQueue = model.getWaitingQueues().get(id[i]);
            WaitingQueueGrafic waitingQueueGrafic = (WaitingQueueGrafic) waitingQueue.getGrafic();
            if (waitingQueueGrafic != null &&
                waitingQueueGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = waitingQueueGrafic.getBoundsExtern();
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
        //System.out.println("WaitingQueueGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * Initialize WaitingQueue panel
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getWaitingQueues().getAllIds();
        for (int i = 0; i < id.length; i++) {
            WaitingQueue waitingQueue = model.getWaitingQueues().get(id[i]);
            WaitingQueueGrafic waitingQueueGrafic = (WaitingQueueGrafic) waitingQueue.getGrafic();
            if (waitingQueueGrafic != null &&
                waitingQueueGrafic.getViewId().equals(viewId)) {
                waitingQueueGrafic.transform();
                panel.add(waitingQueueGrafic);
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
        Point p = waitingQueue.getModel().getModelGrafic().
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
        this.updateCooperationPanel();
        this.updateMasterWaitPanel();
        this.updateSlaveWaitPanel();
    }

    /**
     * submethod of update
     *
     * @throws ModelException
     */
    private void updateMasterWaitPanel() throws ModelException {
        this.masterWaitPanel.removeAll();
        this.masterWaitPanel.setLayout(new GridLayout(1, 1));
        String[][] content = this.waitingQueue.getContentMasters();
        int anzMasterEntry = content.length;
        JComponent[][] bx = new JComponent[2][anzMasterEntry];

        // 1.Zeile ProcessEntities
        for (int i = 0; i < anzMasterEntry; i++) {
            String proc = content[i][0];
            EntityGrafic en = (EntityGrafic) waitingQueue.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[0][i] = en;
        }
        // 2.Zeile Rank
        for (int i = 0; i < anzMasterEntry; i++) {
            String rank = content[i][1];
            JLabel l = new JLabel(rank);
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.waitingQueue.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            tab.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
        }
        masterWaitPanel.add(tab);


        this.setPreferredSize(getSize());
        this.masterWaitLength.setText(anzMasterEntry + "");
    }

    /**
     * submethod of update
     */
    private void updateSlaveWaitPanel() {
        String[][] content = this.waitingQueue.getContentSlaves();
        this.slaveWaitPanel.removeAll();
        this.slaveWaitPanel.setLayout(new GridLayout(1, 1));
        int anzSlaveEntry = content.length;
        JComponent[][] bx = new JComponent[2][anzSlaveEntry];

        // 1.Zeile ProcessEntities
        for (int i = 0; i < anzSlaveEntry; i++) {
            String proc = content[i][0];
            EntityGrafic en = (EntityGrafic) waitingQueue.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[0][i] = en;
        }
        // 2.Zeile Rank
        for (int i = 0; i < anzSlaveEntry; i++) {
            String rank = content[i][1];
            JLabel l = new JLabel(rank);
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.waitingQueue.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.waitingQueue.getModel().getCoordinatenListener());
            tab.addMouseListener(this.waitingQueue.getModel().getCoordinatenListener());
        }
        slaveWaitPanel.add(tab);

        this.setPreferredSize(getSize());
        this.slaveWaitLength.setText(anzSlaveEntry + "");
    }

    private void updateCooperationPanel() {
        this.cooperationPanel.removeAll();
        int cooperationsNo = this.waitingQueue.getCooperationsNo();
        if (this.horizontal) {
            this.cooperationPanel.setLayout(new GridLayout(2, cooperationsNo));
            for (int i = 0; i < cooperationsNo; i++) {
                JLabel master = new JLabel();
                master.setHorizontalAlignment(JLabel.CENTER);
                master.setText(this.waitingQueue.getCooperationMaster(i));
                master.setFont(Grafic.FONT_DEFAULT);
                master.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[i % 2]);
                master.setOpaque(true);
                master.setUI(new VerticalLabelUI(false));
                this.cooperationPanel.add(master);
            }
            for (int i = 0; i < cooperationsNo; i++) {
                JLabel slave = new JLabel();
                slave.setHorizontalAlignment(JLabel.CENTER);
                slave.setText(this.waitingQueue.getCooperationSlave(i));
                slave.setFont(Grafic.FONT_DEFAULT);
                slave.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[i % 2]);
                slave.setOpaque(true);
                slave.setUI(new VerticalLabelUI(false));
                this.cooperationPanel.add(slave);
            }
            this.cooperationPanel.setPreferredSize(new Dimension(10 * cooperationsNo, this.getBounds().height - 30));
        } else {
            this.cooperationPanel.setLayout(new GridLayout(cooperationsNo, 2));
            for (int i = 0; i < cooperationsNo; i++) {
                JLabel master = new JLabel();
                master.setHorizontalAlignment(JLabel.CENTER);
                master.setText(this.waitingQueue.getCooperationMaster(i));
                master.setFont(Grafic.FONT_DEFAULT);
                master.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[i % 2]);
                master.setOpaque(true);
                this.cooperationPanel.add(master);

                JLabel slave = new JLabel();
                slave.setHorizontalAlignment(JLabel.CENTER);
                slave.setText(this.waitingQueue.getCooperationSlave(i));
                slave.setFont(Grafic.FONT_DEFAULT);
                slave.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[i % 2]);
                slave.setOpaque(true);
                this.cooperationPanel.add(slave);
            }
            this.cooperationPanel.setPreferredSize(new Dimension(this.getBounds().width - 15, 10 * cooperationsNo));
        }
        this.noOfCooperations.setText("Cooperations: " + cooperationsNo);
        //System.out.println("CooperationPanel: "+this.cooperationPanel.getPreferredSize());
    }

    /**
     * compute max dimension of all entityGrafics in a ResourceGrafic
     *
     * @throws ModelException
     */
    private Dimension maxEntityDimension() throws ModelException {
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        EntityType defaultType = waitingQueue.getModel().getEntityTyps().get(defaultEntityTypeId);
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

        String[][] masterContent = this.waitingQueue.getContentMasters();
        for (int i = 0; i < masterContent.length; i++) {
            String proc = masterContent[i][0];
            desmoj.extensions.visualization2d.engine.model.Entity en = waitingQueue.getModel().getEntities().get(proc);
            width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
            height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
        }

        String[][] slaveContent = this.waitingQueue.getContentSlaves();
        for (int i = 0; i < slaveContent.length; i++) {
            String proc = slaveContent[i][0];
            desmoj.extensions.visualization2d.engine.model.Entity en = waitingQueue.getModel().getEntities().get(proc);
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
