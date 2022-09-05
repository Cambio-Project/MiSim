package desmoj.extensions.experimentation.ui;

import javax.swing.table.AbstractTableModel;
import java.util.Map;

import desmoj.extensions.experimentation.util.AccessUtil;

/**
 * Adapter to show a map of access points in a swing table.
 *
 * @author Nicolas Knaak
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */

public class AccessPointTableModel extends AbstractTableModel {

    private final Map accessPoints;

    private String[] names;

    private Object[] values;

    /**
     * Creates a new table model for the given table of access points
     *
     * @param accessPoints a map containing access point objects
     */
    public AccessPointTableModel(Map accpt) {
        this.accessPoints = accpt;
        getValues();
    }

    /**
     * @return The number of columns. It is always 2.
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * @return The number of rows (number of access points)
     */
    public int getRowCount() {
        return names.length;
    }

    /**
     * @param col an index
     * @return The column name at the given index (0: "Attribute", 1: "Value")
     */
    public String getColumnName(int col) {
        if (col == 0) {
            return "Attribute";
        } else if (col == 1) {
            return "Value";
        } else {
            return "Column " + col;
        }
    }

    /**
     * @param row row index
     * @param col column index
     * @return The value at the given index
     */
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return names[row];
        } else if (col == 1) {
            return values[row];
        } else {
            return null;
        }
    }

    /**
     * Sets the value at the given index.
     *
     * @param row row index
     * @param col column index
     * @param o   The value to be set
     */
    public void setValueAt(Object o, int row, int col) {
        if (isCellEditable(row, col)) {
            values[row] = o;
        }
    }

    /**
     * Returns the editable status of the cell at the given index. Only cells representing MutableAccessPoints are
     * editable
     *
     * @param row row index
     * @param col column index
     * @return true iff the cell at the given index can be edited.
     */
    public boolean isCellEditable(int row, int col) {
        boolean result = (col == 1 && AccessUtil.isMutable(names[row],
            accessPoints));
        // System.out.println(row + ", " + col + " is editable: " + result);
        return result;
    }

    /** Reads all values from the access points into the table model */
    public void getValues() {
        names = AccessUtil.getAccessPointNames(accessPoints);
        values = AccessUtil.getAccessPointValues(accessPoints);
        this.fireTableDataChanged();
    }

    /** Writes all values from the table model to the access points. */
    public void setValues() {
        for (int i = 0; i < names.length; i++) {
            // System.out.println("Setting " + names[i] + " to " + values[i]);
            AccessUtil.setValue(accessPoints, names[i], values[i]);
        }
        this.fireTableDataChanged();
    }

    /** @return The access points represented by the table model */
    public Map getAccessPoints() {
        return accessPoints;
    }
}