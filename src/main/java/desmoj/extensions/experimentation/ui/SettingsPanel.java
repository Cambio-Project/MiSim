package desmoj.extensions.experimentation.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

/**
 * A GUI panel containing 2 tables for model and experiment parameters.
 *
 * @author Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class SettingsPanel extends JPanel {

    /** Layout of main panel */
    GridLayout gridLayout1 = new GridLayout();

    /** Panel for model data */
    JPanel modelPanel = new JPanel();

    /** Panel for experiment data */
    JPanel expPanel = new JPanel();

    /** Layout of model panel */
    BorderLayout borderLayout1 = new BorderLayout();

    /** Layout of experiment panel */
    BorderLayout borderLayout2 = new BorderLayout();

    /** Title of model panel */
    JLabel modelLabel = new JLabel();

    /** Title of experiment panel */
    JLabel expLabel = new JLabel();

    /** Scroll pane for model parameter table */
    JScrollPane modelScrollPane = new JScrollPane();

    /** Scroll pane for experiment settings table */
    JScrollPane expScrollPane = new JScrollPane();

    /** Table for model parameters */
    JTable modelTable = new JTable();

    /** Table for experiment parameters */
    JTable expTable = new JTable();

    /** Creates a new settings panel. */
    public SettingsPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Inits the user interface */
    private void jbInit() throws Exception {
        this.setLayout(gridLayout1);
        expPanel.setLayout(borderLayout1);
        modelPanel.setLayout(borderLayout2);
        modelLabel.setText("Model Parameters");
        expLabel.setText("Experiment Parameters");
        modelScrollPane.setBorder(null);
        expScrollPane.setBorder(null);
        expTable.setBackground(Color.white);
        modelTable.setBackground(Color.white);
        modelPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        expPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        this.add(modelPanel, null);
        modelPanel.add(modelLabel, BorderLayout.NORTH);
        modelPanel.add(modelScrollPane, BorderLayout.CENTER);
        modelScrollPane.getViewport().add(modelTable, null);
        this.add(expPanel, null);
        expPanel.add(expLabel, BorderLayout.NORTH);
        expPanel.add(expScrollPane, BorderLayout.CENTER);
        expScrollPane.getViewport().add(expTable, null);
        modelTable.setDefaultEditor(Object.class, new AttributeTableEditor());
        modelTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        expTable.setDefaultEditor(Object.class, new AttributeTableEditor());
        expTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }
}