package desmoj.extensions.experimentation.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Dialog to load a model- and experiment runner class dynamically in the experiment starter.
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

public class LoadModelDialog extends JDialog {
    GridLayout gridLayout1 = new GridLayout(3, 2);

    JLabel loadModelLabel = new JLabel();

    JTextField modelNameField = new JTextField();

    JLabel loadExpLabel = new JLabel();

    JTextField expNameField = new JTextField(
        "desmoj.extensions.experimentation.util.ExperimentRunner");

    JButton loadButton = new JButton();

    JButton cancelButton = new JButton();

    JLabel statusLabel = new JLabel();

    private Class expRunner = null;

    private Class model = null;

    /** Creates a new dialog window */
    public LoadModelDialog() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoadModelDialog().setVisible(true);
    }

    /** Inits the GUI */
    private void jbInit() throws Exception {
        loadModelLabel.setText("Model:");
        loadExpLabel.setText("Experiment Runner:");
        this.getContentPane().setLayout(new BorderLayout());
        JPanel editPane = new JPanel();
        editPane.setLayout(gridLayout1);
        loadButton.setText("Open");
        ActionListener loadListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryLoad();
            }
        };
        loadButton.addActionListener(loadListener);
        modelNameField.addActionListener(loadListener);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        this.setModal(true);
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusLabel.setText("Status: ");
        editPane.add(loadModelLabel);
        editPane.add(modelNameField);
        editPane.add(loadExpLabel);
        editPane.add(expNameField);
        editPane.add(loadButton);
        editPane.add(cancelButton);
        this.getContentPane().add(editPane, BorderLayout.CENTER);

        this.getContentPane().add(statusLabel, BorderLayout.SOUTH);
        this.pack();
        this.setTitle("Create new experiment...");
    }

    /** Tries to load a model- and experiment runner class dynamically */
    void tryLoad() {
        try {
            ClassLoader c = ClassLoader.getSystemClassLoader();
            model = c.loadClass(modelNameField.getText());
            expRunner = c.loadClass(expNameField.getText());
            setVisible(false);
        } catch (ClassNotFoundException ex) {
            if (model == null) {
                statusLabel.setText("Status: Model class not Found!");
            } else if (expRunner == null) {
                statusLabel
                    .setText("Status: Experiment runner class not found!");
            }
        }
    }

    /** Closes the dialog */
    void cancel() {
        model = null;
        expRunner = null;
        this.setVisible(false);
    }

    /** Returns the loaded model class */
    public Class getModelClass() {
        return model;
    }

    /** Returns the loaded experiment runner class */
    public Class getExpRunnerClass() {
        return expRunner;
    }
}