package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.model.Attribute;
import desmoj.extensions.visualization2d.engine.model.Entity;
import desmoj.extensions.visualization2d.engine.model.EntityType;
import desmoj.extensions.visualization2d.engine.model.Model;
import desmoj.extensions.visualization2d.engine.model.Statistic;
import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StatisticGrafic;


/**
 * Internal window in viewer application to show attributes of entities
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
public class InfoPane extends JTabbedPane implements ActionListener, MouseListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private InfoPane infopane = null;
    private Dimension dimension = null;
    private Point old = null;
    private ViewerPanel viewer = null;
    private List<InfoContent> content = null;
    private Model model = null;
    private Color bg_default = null;
    private Point moveClick = null;

    /**
     * Internal window in viewer application to show attributes of entities
     *
     * @param root RootWindow of this InfoPane-Instance
     */
    public InfoPane(ViewerPanel viewer) {
        this.viewer = viewer;
        this.model = viewer.getModel();
        this.infopane = this;
        this.content = new LinkedList<InfoContent>();
        this.setTabPlacement(JTabbedPane.TOP);
        this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.dimension = new Dimension(400, 300);
        this.old = new Point(100, 100);
        this.setBounds(old.x, old.y, dimension.width, dimension.height);
        this.bg_default = this.getBackground();
        this.setVisible(false);
        this.addMouseListener(this);

    }

    /**
     * remove all entities and makes it invisible
     */
    public void reset() {
        this.content.removeAll(content);
        this.removeAll();
        this.setVisible(false);
    }

    /**
     * remove entity with given id
     *
     * @param entityId
     */
    public void removeEntry(int j) {
        this.content.remove(j);
        if (this.content.isEmpty()) {
            this.setVisible(false);
        } else {
            this.updateTab();
            this.setSelectedIndex(0);
            this.setPreferredSize(getSize());
        }
        //System.out.println("checkEmpty"+ content.size());
    }

    /**
     * fuegt neues Entity Objekt in content Liste am Anfang eingefuegt. Wird von EntityGrafic actionPerformed
     * aufgerufen
     *
     * @param entityId Id der Entity
     * @param code     Code zur Bezeichnung des Statistic Objects
     */
    public void addEntity(String entityId, String code) {
        //System.out.println("add entity: "+entityId);
        InfoContent nc = new InfoContent(entityId, code);
        this.content.add(0, nc);
        for (int i = 1; i < this.content.size(); i++) {
            InfoContent ic = this.content.get(i);
            if (ic.equals(nc)) {
                this.content.remove(i);
            }
        }
        this.updateTab();
        this.setSelectedIndex(0);
        this.setPreferredSize(getSize());
    }

    /**
     * fuegt neues Statistic Objekt in content Liste am Anfang eingefuegt. Wird von StatisticGrafic actionPerformed
     * aufgerufen
     *
     * @param statisticId Id des Statistic Objects
     * @param code        Code zur Bezeichnung des Statistic Objects
     * @param typ
     * @param isInt
     */
    public void addStatistic(String statisticId, String code, int typ, boolean isInt) {
        //System.out.println("addStatistic: "+name);
        InfoContent nc = new InfoContent(statisticId, code, typ, isInt);
        this.content.add(0, nc);
        for (int i = 1; i < this.content.size(); i++) {
            InfoContent ic = this.content.get(i);
            if (ic.equals(nc)) {
                this.content.remove(i);
            }
        }
        this.updateTab();
        this.setSelectedIndex(0);
        this.setPreferredSize(getSize());
    }

    /**
     * Wird aufgerufen vom Viewer  Start actionPerformed Dort werden mit reload() alle AnimationsElemente neu erstellt.
     * Damit die zuvor ausgewaehlten Statistiken weiterverwendet werden koennen, muessen die Grafikobjekte neu erstellt
     * werden. Alle EntityObjekte werden geloescht, das sie veraltet sind.
     */
    public void refresh() {
        //System.out.println("InfoPane refresh");
        Iterator<InfoContent> it;
        for (it = this.content.iterator(); it.hasNext(); ) {
            InfoContent ic = it.next();
            if (!ic.entity) {
                ic.refresh();
            } else {
                it.remove();
            }
        }
        this.updateTab();
        if (this.content.isEmpty()) {
            this.setVisible(false);
        }
    }

    /**
     * uebertraegt den Inhalt von content in den JTabedPane
     */
    private void updateTab() {
        this.removeAll();
        for (int i = 0; i < this.content.size(); i++) {
            InfoContent ic = this.content.get(i);
            if (ic.entity) {
                JComponent comp = new TabContent(ic.entityId);
                this.addTab(ic.code, comp);
                this.setTabComponentAt(i, new ButtonTabComponent(this));
            } else {
                this.addTab(ic.code, ic.statistikComp);
                this.setTabComponentAt(i, new ButtonTabComponent(this));
            }
        }
    }

    /**
     * set its by click invisible, when its empty Listener is set in Constructor of TabContent
     */
    public void actionPerformed(ActionEvent e) {
        if (this.content.isEmpty()) {
            this.setVisible(false);
        } else {
            this.setVisible(!this.isVisible());
        }
        //System.out.println("InfoPane visible: "+this.isVisible());
    }

    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub
    }

    public void mousePressed(MouseEvent e) {
        // Markiere InfoPane zum verschieben
        this.moveClick = new Point(e.getX(), e.getY());
        this.setOpaque(true);
        this.setBackground(Grafic.COLOR_INFOPANE_MARKED);
    }

    public void mouseReleased(MouseEvent e) {
        // Verschiebe InfoPane zu Koodinaten von e
        int rand = 40;  //Pixel, die vom InfoPane mindestens zusehen bleiben
        Point mc = new Point(0, 0);
        if (this.moveClick != null) {
            mc = this.moveClick;
        }
        this.moveClick = null;

        Rectangle rect_viewer = this.viewer.getBounds();
        Rectangle rect_info = this.getBounds();
        // verschiebe rect_info
        rect_info.translate(e.getX() - mc.x, e.getY() - mc.y);
        // begrenze Verschiebung links
        if (rect_viewer.x > rect_info.x) {
            rect_info.translate(rect_viewer.x - rect_info.x, 0);
        }
        // begrenze Verschiebung oben
        if (rect_viewer.y > rect_info.y) {
            rect_info.translate(0, rect_viewer.y - rect_info.y);
        }
        // begrenze Verschiebung rechts
        if (rect_viewer.x + rect_viewer.width - rand < rect_info.x) {
            rect_info.translate(rect_viewer.x + rect_viewer.width - rand - rect_info.x, 0);
        }
        // begrenze Verschiebung unten
        if (rect_viewer.y + rect_viewer.height - rand < rect_info.y) {
            rect_info.translate(0, rect_viewer.y + rect_viewer.height - rand - rect_info.y);
        }
        // setze die neue Position
        this.setBounds(rect_info);
        // entferne die Markierung
        this.setOpaque(false);
        this.setBackground(bg_default);
    }


    /**
     * A item of content. Its a entity or a statistic.
     */
    class InfoContent {

        String code = null;
        boolean entity;
        String entityId = null;
        String statisticId = null;
        int statistikTyp;
        boolean statistikIsIntValue;
        StatisticGrafic statistikComp = null;

        /**
         * Konstruktor fuer Entity
         *
         * @param entityId
         */
        public InfoContent(String entityId, String code) {
            this.code = code;
            this.entity = true;
            this.entityId = entityId;
        }

        /**
         * Konstructor fuer Statistik Objekt
         *
         * @param name
         * @param typ        siehe StatisticGrafic.ANIMATION_....
         * @param isIntValue
         */
        public InfoContent(String statisticId, String code, int typ, boolean isIntValue) {
            this.code = code;
            this.entity = false;
            this.statisticId = statisticId;
            this.statistikTyp = typ;
            this.statistikIsIntValue = isIntValue;
            this.refresh();
        }

        /**
         * aktualisiert StatisticGrafic Object
         */
        public void refresh() {
            if (!this.entity) {
                Statistic statistic = model.getStatistics().get(statisticId);
                this.statistikComp =
                    (StatisticGrafic) statistic.createGrafic(null, 0, 0, statistikTyp, statistikIsIntValue, null, true);
                this.statistikComp.update();
                //System.out.println("InfoContent refresh");
            }
        }

        public boolean equals(InfoContent val) {
            boolean out = false;
            if (this.entity) {
                out = this.entityId.equals(val.entityId);
            } else {
                out = (this.statisticId.equals(val.statisticId)) &&
                    (this.statistikTyp == val.statistikTyp) &&
                    (this.statistikIsIntValue == val.statistikIsIntValue);
            }
            return out;
        }
    }

    /**
     * manages the gui of InfoPane-Content. It's swing table stuff.
     *
     * @author Christian
     */
    class TabContent extends JPanel {
        private static final long serialVersionUID = 1L;

        private Vector<Vector<String>> rowData = null;
        private List<Integer> headerRows = null;
        private List<Integer> oldValueRows = null;

        public TabContent(String entityId) {
            rowData = new Vector<Vector<String>>();
            headerRows = new LinkedList<Integer>();
            oldValueRows = new LinkedList<Integer>();

            List<String> columNames = new LinkedList<String>();
            columNames.add("Key");
            columNames.add("Value");
            columNames.add("Since");

            if (model.getEntities().exist(entityId)) {
                this.setLayout(new BorderLayout());
                this.createContent(entityId);
                ColoredTableCellRenderer ctcr = new ColoredTableCellRenderer(headerRows, oldValueRows);
                JTable table = new JTable(rowData, new Vector<String>(columNames));
                table.setDefaultRenderer(Object.class, ctcr);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                table.getColumnModel().getColumn(0).setPreferredWidth(100);
                table.getColumnModel().getColumn(1).setPreferredWidth(150);
                table.getColumnModel().getColumn(2).setPreferredWidth(150);
                table.setDragEnabled(false);
                table.setColumnSelectionAllowed(false);
                table.setRowSelectionAllowed(false);
                JScrollPane scroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                Icon icon = new ImageIcon(viewer.getLanguage().getInternURL("viewer_delete_icon"));
                JButton exit = new JButton(icon);
                exit.addActionListener(infopane);
                scroll.setCorner(JScrollPane.LOWER_RIGHT_CORNER, exit);
                this.add(scroll, BorderLayout.CENTER);
            } else {
                this.add(new JLabel("This entity does not exist."));
            }
        }

        private void createContent(String entityId) {
            Vector<String> row;
            Entity entity = model.getEntities().get(entityId);
            EntityType entityType = model.getEntityTyps().get(entity.getEntityTypeId());
            String[] posAttr = entityType.getPossibleAttributes();
            String[] posStates = entityType.getPossibleStates();
            List<Attribute> v = null;

            int i = 0;
            row = new Vector<String>();
            row.add("Id");
            row.add(entity.getId());
            row.add("");
            rowData.add(row);
            headerRows.add(new Integer(i++));
            row = new Vector<String>();
            row.add("EntityTypeId");
            row.add(entityType.getId());
            row.add("");
            rowData.add(row);
            i++;
            v = entity.getStateHistory();
            for (int k = v.size() - 1; k > -1; k--) {
                row = new Vector<String>();
                row.add("State (" + k + ")");
                row.add(v.get(k).getValue());
                row.add(SimulationTime.getTimeString(v.get(k).getSince(), SimulationTime.SHOW_DATE_TIME));
                if (k < v.size() - 1) {
                    oldValueRows.add(new Integer(i));
                }
                rowData.add(row);
                i++;
            }
            v = entity.getNameAttribute();
            for (int k = v.size() - 1; k > -1; k--) {
                row = new Vector<String>();
                row.add("NameAttr (" + k + ")");
                row.add(v.get(k).getValue());
                row.add(SimulationTime.getTimeString(v.get(k).getSince(), SimulationTime.SHOW_DATE_TIME));
                if (k < v.size() - 1) {
                    oldValueRows.add(new Integer(i));
                }
                rowData.add(row);
                i++;
            }
            v = entity.getPriorityAttribute();
            for (int k = v.size() - 1; k > -1; k--) {
                row = new Vector<String>();
                row.add("PriorityAttr (" + k + ")");
                row.add(v.get(k).getValue());
                row.add(SimulationTime.getTimeString(v.get(k).getSince(), SimulationTime.SHOW_DATE_TIME));
                if (k < v.size() - 1) {
                    oldValueRows.add(new Integer(i));
                }
                rowData.add(row);
                i++;
            }
            v = entity.getVelocityAttribute();
            for (int k = v.size() - 1; k > -1; k--) {
                row = new Vector<String>();
                row.add("VelocityAttr (" + k + ")");
                row.add(v.get(k).getValue());
                row.add(SimulationTime.getTimeString(v.get(k).getSince(), SimulationTime.SHOW_DATE_TIME));
                if (k < v.size() - 1) {
                    oldValueRows.add(new Integer(i));
                }
                rowData.add(row);
                i++;
            }
            row = new Vector<String>();
            row.add("History:");
            row.add("");
            row.add("");
            rowData.add(row);
            headerRows.add(new Integer(i++));

            v = entity.getContainerHistory();
            for (int k = v.size() - 1; k > -1; k--) {
                String[] ky = Parameter.split(v.get(k).getKey());
                if (!(ky[1].equals("free")) || (ky[1].equals("static"))) {
                    row = new Vector<String>();
                    row.add(ky[1] + "  (" + ky[0] + ")");
                    row.add(v.get(k).getValue());
                    row.add(SimulationTime.getTimeString(v.get(k).getSince(), SimulationTime.SHOW_DATE_TIME));
                    rowData.add(row);
                    i++;
                }
            }

            row = new Vector<String>();
            row.add("Attributes:");
            row.add("");
            rowData.add(row);
            headerRows.add(new Integer(i++));
            //Attributes with Values
            for (int j = 0; j < posAttr.length; j++) {
                v = entity.getAttributeHistory(posAttr[j]);
                if (v != null) {
                    for (int k = v.size() - 1; k > -1; k--) {
                        row = new Vector<String>();
                        row.add(posAttr[j] + " (" + k + ")");
                        Attribute attr = v.get(k);
                        row.add(attr.getValue());
                        row.add(SimulationTime.getTimeString(attr.getSince(), SimulationTime.SHOW_DATE_TIME));
                        if (k < v.size() - 1) {
                            oldValueRows.add(new Integer(i));
                        }
                        rowData.add(row);
                        i++;
                    }
                }
            }

            //Attributes without Values
            for (int j = 0; j < posAttr.length; j++) {
                v = entity.getAttributeHistory(posAttr[j]);
                if (v == null) {
                    row = new Vector<String>();
                    row.add(posAttr[j]);
                    row.add("");
                    row.add("");
                    rowData.add(row);
                    oldValueRows.add(new Integer(i++));
                }
            }

            row = new Vector<String>();
            row.add("possible States:");
            row.add("");
            rowData.add(row);
            headerRows.add(new Integer(i++));
            for (int j = 0; j < posStates.length; j++) {
                row = new Vector<String>();
                row.add("");
                row.add(posStates[j]);
                row.add("");
                rowData.add(row);
                i++;
            }
            row = new Vector<String>();
            row.add("");
            row.add("");
            row.add("");
            rowData.add(row);
            headerRows.add(new Integer(i++));
        }
    }

    class ColoredTableCellRenderer implements TableCellRenderer {

        private static final String begin = "<html><body>";
        private static final String end = "</body></html>";
        private static final String shift = "&nbsp;&nbsp;&nbsp;";
        private List<Integer> headerRows = null;
        private List<Integer> oldValueRows = null;

        public ColoredTableCellRenderer(List<Integer> headerRows, List<Integer> oldValueRows) {
            this.headerRows = headerRows;
            this.oldValueRows = oldValueRows;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            // create JLabel
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setFont(ViewerPanel.FONT_SMALL);
            if (headerRows.contains(new Integer(row))) {
                label.setText((String) value);
                label.setBackground(ViewerPanel.INFO_HEADER_BG_COLOR);
                label.setForeground(ViewerPanel.INFO_HEADER_FG_COLOR);
            } else if (oldValueRows.contains(new Integer(row))) {
                label.setText(begin + shift + value + end);
                label.setBackground(ViewerPanel.INFO_TEXT_BG_COLOR);
                label.setForeground(ViewerPanel.INFO_OLD_FG_COLOR);
                this.setRowHeight(table, row, label);
            } else {
                label.setText(begin + shift + value + end);
                label.setBackground(ViewerPanel.INFO_TEXT_BG_COLOR);
                label.setForeground(ViewerPanel.INFO_TEXT_FG_COLOR);
                this.setRowHeight(table, row, label);
            }
            return label;
        }

        private void setRowHeight(JTable table, int row, JLabel label) {
            String content = label.getText();
            // zaehle zeilen
            int next = -1, lines = 0;
            do {
                next = content.indexOf("<br>", next + 1);
                lines++;
            } while (next >= 0);
            if (lines > 1) {
                int h = label.getFontMetrics(label.getFont()).getHeight();
                content = content.replaceAll("<br>", "<br>" + shift);
                content = content.replaceAll("<BR>", "<br>" + shift);
                content = content.replaceAll(" ", "&nbsp;");
                label.setText(content);
                table.setRowHeight(row, lines * h);
            }
        }

    }


}
