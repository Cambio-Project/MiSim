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
import desmoj.extensions.visualization2d.engine.model.Stock;
import desmoj.extensions.visualization2d.engine.util.MyTableUtilities;
import desmoj.extensions.visualization2d.engine.util.VerticalLabelUI;


/**
 * Grafic of Stock
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
public class StockGrafic extends JPanel implements Grafic {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Point pointIntern;
    private final Point pointExtern;
    private boolean printable;
    private final JScrollPane consumerPane;
    private final JScrollPane producerPane;
    private final JPanel consumerFlow;
    private final JPanel consumerPanel;
    private final JPanel producerFlow;
    private final JPanel producerPanel;
    private final JPanel stockPanel;
    private final JLabel consumerLength;
    private final JLabel producerLength;
    private JLabel stockValue, stockLwb, stockUpb;
    private final boolean horizontal;
    private final Dimension entityDimension;
    private final int anzVisible;
    private final String defaultEntityTypeId;
    private final String code;
    private final Stock stock;
    private Dimension deltaSize;  // Abweichung von default Groesse der Grafic
    private final String viewId;


    /**
     * Constructor
     *
     * @param stock               stock instance
     * @param viewId              Id of view
     * @param pointExtern         middle point in resource panel
     * @param defaultEntityTypeId default entity type of process entities
     * @param anzVisible          No. of visible entities. When more, a scrollbar is used.
     * @param horizontal          true, when entities are horizontal arranged. false. when vertical
     * @param deltaSize           value to increment default size of grafic
     * @throws ModelException, when ?????????????
     */
    public StockGrafic(Stock stock, String viewId, Point pointExtern,
                       String defaultEntityTypeId, int anzVisible, boolean horizontal,
                       Dimension deltaSize) throws ModelException {

        this.stock = stock;
        if (viewId == null) {
            viewId = "main";
        }
        this.viewId = viewId;
        this.pointExtern = pointExtern;
        this.transform();
        this.defaultEntityTypeId = defaultEntityTypeId;
        this.anzVisible = anzVisible;
        this.horizontal = horizontal;
        if (stock.getName() != null) {
            this.code = this.stock.getName();
        } else {
            this.code = "id: " + this.stock.getId();
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

        // stack count initialization
        this.stockPanel = this.buildStockPanel(horizontal);

        // producer content initialization
        this.producerFlow = new JPanel();
        this.producerFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.producerFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.producerPanel = new JPanel();
        this.producerFlow.add(this.producerPanel);
        this.producerPanel.setOpaque(false);
        this.producerLength = new JLabel("0");
        this.producerLength.setFont(Grafic.FONT_DEFAULT);
        this.producerLength.setHorizontalAlignment(JLabel.CENTER);
        this.producerLength.setForeground(Color.blue);
        this.producerLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.producerLength.setOpaque(true);

        // consumer content initialization
        this.consumerFlow = new JPanel();
        this.consumerFlow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.consumerFlow.setBackground(ModelGrafic.COLOR_BACKGROUND);
        this.consumerPanel = new JPanel();
        this.consumerFlow.add(this.consumerPanel);
        this.consumerPanel.setOpaque(false);
        this.consumerLength = new JLabel("0");
        this.consumerLength.setFont(Grafic.FONT_DEFAULT);
        this.consumerLength.setHorizontalAlignment(JLabel.CENTER);
        this.consumerLength.setForeground(Color.blue);
        this.consumerLength.setBackground(Grafic.COLOR_BACKGROUND);
        this.consumerLength.setOpaque(true);

        JPanel cornerC = new JPanel();
        cornerC.setLayout(new GridLayout(1, 1));
        cornerC.setBackground(Grafic.COLOR_BACKGROUND);
        JPanel cornerP = new JPanel();
        cornerP.setLayout(new GridLayout(1, 1));
        cornerP.setBackground(Grafic.COLOR_BACKGROUND);

        if (this.horizontal) {
            Box legendeConsumer = Box.createHorizontalBox();
            legendeConsumer.setBackground(Grafic.COLOR_BACKGROUND);
            legendeConsumer.setOpaque(true);
            JLabel legendeConsumer1Label = new JLabel("Consumer", JLabel.CENTER);
            legendeConsumer1Label.setFont(Grafic.FONT_DEFAULT);
            legendeConsumer1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeConsumer2Label = new JLabel("Rank-#Prod", JLabel.CENTER);
            legendeConsumer2Label.setFont(Grafic.FONT_DEFAULT);
            legendeConsumer2Label.setUI(new VerticalLabelUI(false));
            legendeConsumer.add(legendeConsumer1Label);
            legendeConsumer.add(legendeConsumer2Label);

            Box legendeProducer = Box.createHorizontalBox();
            legendeProducer.setBackground(Grafic.COLOR_BACKGROUND);
            legendeProducer.setOpaque(true);
            JLabel legendeProducer1Label = new JLabel("Producer", JLabel.CENTER);
            legendeProducer1Label.setFont(Grafic.FONT_DEFAULT);
            legendeProducer1Label.setUI(new VerticalLabelUI(false));
            JLabel legendeProducer2Label = new JLabel("Rank-#Prod", JLabel.CENTER);
            legendeProducer2Label.setFont(Grafic.FONT_DEFAULT);
            legendeProducer2Label.setUI(new VerticalLabelUI(false));
            legendeProducer.add(legendeProducer1Label);
            legendeProducer.add(legendeProducer2Label);

            this.consumerPane = new JScrollPane(this.consumerFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.consumerPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerC);
            JScrollBar sbQueue = this.consumerPane.getHorizontalScrollBar();
            sbQueue.setPreferredSize(new Dimension(sbQueue.getPreferredSize().width, 5));

            this.producerPane = new JScrollPane(this.producerFlow, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.producerPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, cornerP);
            JScrollBar sbProcess = this.producerPane.getHorizontalScrollBar();
            sbProcess.setPreferredSize(new Dimension(sbProcess.getPreferredSize().width, 5));

            p.setLayout(new GridLayout(2, 1));
            JPanel p1 = new JPanel(new BorderLayout());
            p1.add(legendeProducer, BorderLayout.WEST);
            p1.add(this.producerPane, BorderLayout.CENTER);
            p1.add(this.producerLength, BorderLayout.EAST);
            p.add(p1);
            JPanel p2 = new JPanel(new BorderLayout());
            p2.add(legendeConsumer, BorderLayout.WEST);
            p2.add(this.consumerPane, BorderLayout.CENTER);
            p2.add(this.consumerLength, BorderLayout.EAST);
            p.add(p2);

            this.add(this.stockPanel, BorderLayout.WEST);
            Dimension d = new Dimension((this.entityDimension.width * this.anzVisible) + this.deltaSize.width + 75,
                2 * this.entityDimension.height + this.deltaSize.height + 90);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        } else {
            Box legendeConsumer = Box.createVerticalBox();
            legendeConsumer.setBackground(Grafic.COLOR_BACKGROUND);
            legendeConsumer.setOpaque(true);
            JLabel legendeConsumer1Label = new JLabel("Consumer", JLabel.CENTER);
            legendeConsumer1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeConsumer2Label = new JLabel("#Prod-Rank", JLabel.CENTER);
            legendeConsumer2Label.setFont(Grafic.FONT_DEFAULT);
            legendeConsumer.add(legendeConsumer1Label);
            legendeConsumer.add(legendeConsumer2Label);

            Box legendeProducer = Box.createVerticalBox();
            legendeProducer.setBackground(Grafic.COLOR_BACKGROUND);
            legendeProducer.setOpaque(true);
            JLabel legendeProducer1Label = new JLabel("Producer", JLabel.CENTER);
            legendeProducer1Label.setFont(Grafic.FONT_DEFAULT);
            JLabel legendeProducer2Label = new JLabel("#Prod-Rank", JLabel.CENTER);
            legendeProducer2Label.setFont(Grafic.FONT_DEFAULT);
            legendeProducer.add(legendeProducer1Label);
            legendeProducer.add(legendeProducer2Label);

            this.consumerPane = new JScrollPane(this.consumerFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.consumerPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerC);
            JScrollBar sbQueue = this.consumerPane.getVerticalScrollBar();
            sbQueue.setPreferredSize(new Dimension(5, sbQueue.getPreferredSize().height));

            this.producerPane = new JScrollPane(this.producerFlow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            this.producerPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerP);
            JScrollBar sbProcess = this.producerPane.getVerticalScrollBar();
            sbProcess.setPreferredSize(new Dimension(5, sbProcess.getPreferredSize().height));

            p.setLayout(new GridLayout(1, 2));
            JPanel p1 = new JPanel(new BorderLayout());
            p1.add(legendeProducer, BorderLayout.NORTH);
            p1.add(this.producerPane, BorderLayout.CENTER);
            p1.add(this.producerLength, BorderLayout.SOUTH);
            p.add(p1);
            JPanel p2 = new JPanel(new BorderLayout());
            p2.add(legendeConsumer, BorderLayout.NORTH);
            p2.add(this.consumerPane, BorderLayout.CENTER);
            p2.add(this.consumerLength, BorderLayout.SOUTH);
            p.add(p2);


            this.add(this.stockPanel, BorderLayout.NORTH);
            Dimension d = new Dimension((2 * this.entityDimension.width) + this.deltaSize.width + 90,
                (this.entityDimension.height * this.anzVisible) + this.deltaSize.height + 100);
            this.setBounds(this.pointIntern.x - d.width / 2, this.pointIntern.y - d.height / 2, d.width, d.height);
        }
        this.update();
        if (this.stock.getModel().getCoordinatenListener() != null) {
            this.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            this.addMouseListener(this.stock.getModel().getCoordinatenListener());
            p.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            p.addMouseListener(this.stock.getModel().getCoordinatenListener());
            this.producerFlow.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            this.producerFlow.addMouseListener(this.stock.getModel().getCoordinatenListener());
            this.consumerFlow.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            this.consumerFlow.addMouseListener(this.stock.getModel().getCoordinatenListener());
        }

    }

    /**
     * get all views (viewId's) with Stock
     *
     * @return
     */
    public static String[] getViews(Model model) {
        TreeSet<String> views = new TreeSet<String>();
        String[] ids = model.getStocks().getAllIds();
        for (int i = 0; i < ids.length; i++) {
            Stock stock = model.getStocks().get(ids[i]);
            StockGrafic stockGrafic = (StockGrafic) stock.getGrafic();
            if (stockGrafic != null) {
                String viewId = stockGrafic.getViewId();
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
     * Construct the smallest rectangle, which include the positions of all stocks in view
     *
     * @param viewId id of view
     * @return smallest rectangle when an StockGrafic exist, null otherwise.
     */
    public static Rectangle getBoundsExternGlobal(Model model, String viewId) {
        boolean found = false;
        int minX = Integer.MAX_VALUE / 2;
        int minY = Integer.MAX_VALUE / 2;
        int maxX = Integer.MIN_VALUE / 2;
        int maxY = Integer.MIN_VALUE / 2;
        String[] id = model.getStocks().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Stock stock = model.getStocks().get(id[i]);
            StockGrafic stockGrafic = (StockGrafic) stock.getGrafic();
            if (stockGrafic != null &&
                stockGrafic.getViewId().equals(viewId)) {
                found = true;
                Rectangle r = stockGrafic.getBoundsExtern();
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
        //System.out.println("StockGrafic: BoundsExtern: "+r);
        return r;
    }

    /**
     * Initialize resource panel
     *
     * @param panel
     */
    public static void updateInit(Model model, String viewId, JComponent panel) {
        String[] id = model.getStocks().getAllIds();
        for (int i = 0; i < id.length; i++) {
            Stock stock = model.getStocks().get(id[i]);
            StockGrafic stockGrafic = (StockGrafic) stock.getGrafic();
            if (stockGrafic != null &&
                stockGrafic.getViewId().equals(viewId)) {
                stockGrafic.transform();
                panel.add(stockGrafic);
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
        Point p = stock.getModel().getModelGrafic().
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
        this.updateStockPanel();
        this.updateProducerPanel();
        this.updateConsumerPanel();
    }

    /**
     * submethod of update
     *
     * @throws ModelException
     */
    private void updateProducerPanel() throws ModelException {
        this.producerPanel.removeAll();
        this.producerPanel.setLayout(new GridLayout(1, 1));
        String[][] content = stock.getContentProducer();
        int anzProducerEntry = content.length;
        JComponent[][] bx = new JComponent[3][anzProducerEntry];

        // 1.Zeile Product Anzahl
        for (int i = 0; i < anzProducerEntry; i++) {
            String prod = content[i][2];
            JLabel l = new JLabel(prod);
            l.setFont(Grafic.FONT_DEFAULT);
            bx[0][i] = l;
        }
        // 2.Zeile ProcessEntities
        for (int i = 0; i < anzProducerEntry; i++) {
            String proc = content[i][0];
            EntityGrafic en = (EntityGrafic) stock.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = en;
        }
        // 3.Zeile Rank
        for (int i = 0; i < anzProducerEntry; i++) {
            String rank = content[i][1];
            JLabel l = new JLabel(rank);
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[2][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.stock.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            tab.addMouseListener(this.stock.getModel().getCoordinatenListener());
        }
        producerPanel.add(tab);

        this.setPreferredSize(getSize());
        this.producerLength.setText(anzProducerEntry + "");
    }

    /**
     * submethod of update
     */
    private void updateConsumerPanel() {
        String[][] content = stock.getContentConsumer();
        this.consumerPanel.removeAll();
        this.consumerPanel.setLayout(new GridLayout(1, 1));
        int anzConsumerEntry = content.length;
        JComponent[][] bx = new JComponent[3][anzConsumerEntry];

        // 1.Zeile Product Anzahl
        for (int i = 0; i < anzConsumerEntry; i++) {
            String r = content[i][2];
            JLabel l = new JLabel(r);
            l.setFont(Grafic.FONT_DEFAULT);
            bx[0][i] = l;
        }
        // 2.Zeile ProcessEntities
        for (int i = 0; i < anzConsumerEntry; i++) {
            String proc = content[i][0];
            EntityGrafic en = (EntityGrafic) stock.getModel().getEntities().get(proc).getGrafic();
            en.setAlignmentX(CENTER_ALIGNMENT);
            en.setAlignmentY(TOP_ALIGNMENT);
            bx[1][i] = en;
        }
        // 3.Zeile Rank
        for (int i = 0; i < anzConsumerEntry; i++) {
            String rank = content[i][1];
            JLabel l = new JLabel(rank);
            l.setFont(Grafic.FONT_DEFAULT);
            l.setAlignmentX(CENTER_ALIGNMENT);
            l.setAlignmentY(TOP_ALIGNMENT);
            bx[2][i] = l;
        }
        JComponent tab = new MyTableUtilities().makeCompactGrid(bx, horizontal);
        if (this.stock.getModel().getCoordinatenListener() != null) {
            tab.addMouseMotionListener(this.stock.getModel().getCoordinatenListener());
            tab.addMouseListener(this.stock.getModel().getCoordinatenListener());
        }
        consumerPanel.add(tab);

        this.setPreferredSize(getSize());
        this.consumerLength.setText(anzConsumerEntry + "");
    }

    private JPanel buildStockPanel(boolean horizontal) {
        JPanel stockPanel = new JPanel();
        stockPanel.setLayout(new BorderLayout());
        stockPanel.setBackground(ModelGrafic.COLOR_BACKGROUND);
        stockPanel.setOpaque(true);
        JPanel stockFlow = new JPanel();
        stockFlow.setOpaque(false);
        stockFlow.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.stockValue = new JLabel("");
        this.stockValue.setFont(FONT_BIG);
        stockFlow.add(this.stockValue);
        stockPanel.add(stockFlow, BorderLayout.CENTER);
        this.stockLwb = new JLabel("");
        this.stockLwb.setFont(Grafic.FONT_DEFAULT);
        this.stockUpb = new JLabel("");
        this.stockUpb.setFont(Grafic.FONT_DEFAULT);
        if (horizontal) {
            this.stockLwb.setUI(new VerticalLabelUI(false));
            this.stockUpb.setUI(new VerticalLabelUI(false));
            stockPanel.add(this.stockUpb, BorderLayout.NORTH);
            stockPanel.add(this.stockLwb, BorderLayout.SOUTH);
        } else {
            stockPanel.add(this.stockUpb, BorderLayout.EAST);
            stockPanel.add(this.stockLwb, BorderLayout.WEST);
        }
        return stockPanel;

    }

    private void updateStockPanel() {
        this.stockValue.setText(Long.toString(stock.getCount()));
        this.stockLwb.setText("LWB: " + stock.getLwb());
        this.stockUpb.setText("UPB: " + stock.getUpb());
        this.stockValue.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[0]);
        this.stockLwb.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[2]);
        this.stockUpb.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[2]);
        if (stock.getCount() <= stock.getLwb()) {
            this.stockValue.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[1]);
            this.stockLwb.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[1]);
        }
        if (stock.getCount() >= stock.getUpb()) {
            this.stockValue.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[1]);
            this.stockUpb.setForeground(Grafic.COLOR_SWITCH_STOCK_BOUND[1]);
        }
    }

    /**
     * compute max dimension of all entityGrafics in a ResourceGrafic
     *
     * @throws ModelException
     */
    private Dimension maxEntityDimension() throws ModelException {
        FontMetrics fm = this.getFontMetrics(Grafic.FONT_DEFAULT);
        EntityType defaultType = stock.getModel().getEntityTyps().get(defaultEntityTypeId);
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
        String[][] consumerContent = this.stock.getContentConsumer();
        for (int i = 0; i < consumerContent.length; i++) {
            String proc = consumerContent[i][0];
            desmoj.extensions.visualization2d.engine.model.Entity en = stock.getModel().getEntities().get(proc);
            width = Math.max(width, ((EntityGrafic) en.getGrafic()).getWidth());
            height = Math.max(height, ((EntityGrafic) en.getGrafic()).getHeight());
        }
        String[][] producerContent = this.stock.getContentProducer();
        for (int i = 0; i < producerContent.length; i++) {
            String proc = producerContent[i][0];
            desmoj.extensions.visualization2d.engine.model.Entity en = stock.getModel().getEntities().get(proc);
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
