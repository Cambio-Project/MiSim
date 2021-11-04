package desmoj.extensions.visualization2d.engine.util;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;

/**
 * Utillity to paint and manage a table
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
public class MyTableUtilities {

    boolean horizontal;

    public MyTableUtilities() {

    }

    public JComponent makeCompactGrid(JComponent[][] comps, boolean horizontal) {

        this.horizontal = horizontal;
        MyTableModel myTableModel = new MyTableModel(comps);
        MyTableColumnModel myTableColumnModel = new MyTableColumnModel(myTableModel);
        JTable table = new JTable(myTableModel, myTableColumnModel);
        table.setDefaultRenderer(JComponent.class, new MyTableCellRenderer());
        for (int i = 0; i < table.getRowCount(); i++) {
            int height = 1;
            for (int j = 0; j < table.getColumnCount(); j++) {
                JComponent cell = (JComponent) table.getValueAt(i, j);
                height = Math.max(height, cell.getPreferredSize().height);
            }
            table.setRowHeight(i, height);
        }
        table.addMouseListener(new MyTableMouseListener(table));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        return table;
    }

    class MyTableModel implements TableModel {

        private final JComponent[][] comps;
        private final int rows;
        private int cols;

        public MyTableModel(JComponent[][] comps) {
            this.comps = comps;
            this.rows = comps.length;
            this.cols = 0;
            if (this.rows > 0) {
                this.cols = comps[0].length;
            }
            boolean flag = true;
            for (int i = 0; i < this.rows; i++) {
                if (comps[i].length != this.cols) {
                    flag = false;
                }
            }
            if (!flag) {
                System.out.println("Dimension falsch");
            }
            //System.out.println("rows: "+rows+"   cols: "+cols);

        }

        public void addTableModelListener(TableModelListener arg0) {
            // TODO Auto-generated method stub
        }

        public Class<?> getColumnClass(int arg0) {
            return JComponent.class;
        }

        public int getColumnCount() {
            int out;
            if (horizontal) {
                out = this.cols;
            } else {
                out = this.rows;
            }
            return out;
        }

        public String getColumnName(int arg0) {
            return Integer.toString(arg0);
        }

        public int getRowCount() {
            int out;
            if (horizontal) {
                out = this.rows;
            } else {
                out = this.cols;
            }
            return out;
        }

        public Object getValueAt(int row, int col) {
            //System.out.println("row: "+row+"   col: "+col);
            if (row >= 0 && col >= 0) {
                Object out;
                if (horizontal) {
                    out = this.comps[row][col];
                } else {
                    out = this.comps[col][row];
                }
                return out;
            } else {
                return null;
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void removeTableModelListener(TableModelListener arg0) {
            // TODO Auto-generated method stub
        }

        public void setValueAt(Object comp, int row, int col) {
            if (horizontal) {
                this.comps[row][col] = (JComponent) comp;
            } else {
                this.comps[col][row] = (JComponent) comp;
            }
        }

    }

    class MyTableColumnModel extends DefaultTableColumnModel {
        private static final long serialVersionUID = 1L;

        public MyTableColumnModel(MyTableModel data) {
            super();
            for (int i = 0; i < data.getColumnCount(); i++) {
                int width = 1;
                for (int j = 0; j < data.getRowCount(); j++) {
                    JComponent comp = (JComponent) data.getValueAt(j, i);
                    width = Math.max(width, comp.getPreferredSize().width);
                }
                TableColumn col = new TableColumn(i, width);
                this.addColumn(col);
            }
        }
    }

    class MyTableCellRenderer implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            if (horizontal) {
                panel.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[column % 2]);
            } else {
                panel.setBackground(Grafic.COLOR_SWITCH_BACKGROUND[row % 2]);
            }
            panel.setOpaque(true);
            JComponent cont = (JComponent) value;
            panel.add(cont);
            return panel;
        }
    }

    class MyTableMouseListener implements MouseListener {

        private final JTable table;

        public MyTableMouseListener(JTable table) {
            this.table = table;
        }

        private void forwardEvent(MouseEvent e) {
            //System.out.println("tableEvent forward Point: "+e.getPoint());
            int row = this.table.rowAtPoint(e.getPoint());
            int col = this.table.columnAtPoint(e.getPoint());
            //System.out.println("tableEvent forward   row: "+row+"   col: "+col);
            JComponent comp = (JComponent) table.getValueAt(row, col);
            if (comp != null) {
                Point cellLoc = table.getCellRect(row, col, true).getLocation();
                Point compLoc = comp.getLocation();
                Point shift = new Point(cellLoc.x + compLoc.x, cellLoc.y + compLoc.y);
                e.translatePoint(-shift.x, -shift.y);
                comp.dispatchEvent(e);
            }
        }

        public void mouseClicked(MouseEvent e) {
            this.forwardEvent(e);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

    }

}
