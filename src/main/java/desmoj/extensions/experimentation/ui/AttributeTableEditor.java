package desmoj.extensions.experimentation.ui;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import desmoj.extensions.experimentation.util.Filename;

/**
 * A generic editor for elements in Swing tables. This class contains concrete editors (e.g. for elements of type
 * boolean) as inner classes.
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
public class AttributeTableEditor extends AbstractCellEditor implements
    TableCellEditor {

    /** The currently active cell editor */
    private AttribEditor currentEditor = null;

    /** HashMap of specialized editors for known types */
    private final HashMap editors = new HashMap();

    /** default editor used if no specialized editor is found in editor map */
    private final AttribEditor defaultEditor;

    /** The current attribute's old value */
    private Object oldValue;

    /**
     * Creates a new AttributeTableEditor and registers the known cell editors.
     */
    public AttributeTableEditor() {
        defaultEditor = createDefaultEditor();
        registerEditors();
    }

    /**
     * Registers a new cell editor for a special type.
     *
     * @param c    an attribute editor
     * @param type the class to edit with this editor
     */
    protected void registerEditor(AttribEditor c, Class type) {
        editors.put(type, c);
    }

    /**
     * Registers the default editors. Might be extended in subclasses to register or exchange other specialized editors
     */
    protected void registerEditors() {
        // Use floating point editor for Float and Double values
        AttribEditor e = new FloatingPointNumberEditor();
        registerEditor(e, Float.class);
        registerEditor(e, Double.class);

        // Use IntegerEditor for Integer, Short, Byte und Long
        e = new IntegerNumberEditor();
        registerEditor(e, Integer.class);
        registerEditor(e, Short.class);
        registerEditor(e, Byte.class);
        registerEditor(e, Long.class);

        // Register Boolean editor
        e = new BooleanAttribEditor();
        registerEditor(e, Boolean.class);

        // Register String editor
        e = new StringEditor();
        registerEditor(e, String.class);

        // Register TimeUnit editor
        e = new TimeUnitAttribEditor();
        registerEditor(e, TimeUnit.MICROSECONDS.getClass());
        registerEditor(e, TimeUnit.MILLISECONDS.getClass());
        registerEditor(e, TimeUnit.SECONDS.getClass());
        registerEditor(e, TimeUnit.MINUTES.getClass());
        registerEditor(e, TimeUnit.SECONDS.getClass());
        registerEditor(e, TimeUnit.HOURS.getClass());
        registerEditor(e, TimeUnit.DAYS.getClass());

        // Register FilenameEditor
        e = new FilenameAttribEditor();
        registerEditor(e, Filename.class);
    }

    /**
     * Creates the default editor. This is the editor, fields of a non specified type are edited with. Might be
     * overridden in subclasses to exchange default editor.
     *
     * @return the default editor (a TextBasedEditor)
     */
    protected AttribEditor createDefaultEditor() {
        return new TextBasedEditor();
    }

    /**
     * Implemented for interface TableCellEditor. Returns the editor for the given table cell value.
     *
     * @param table    the current JTable
     * @param value    the object to edit
     * @param selected flag indicating if the cell containing the object is selected
     * @param row      row of table cell
     * @param col      column of table cell
     * @return the editor's GUI component.
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean selected, int row, int col) {
        Class c = value.getClass();
        currentEditor = (AttribEditor) editors.get(c);
        if (c == null) {
            currentEditor = defaultEditor;
        }
        oldValue = value;
        currentEditor.setValue(oldValue);
        return currentEditor.getComponent();
    }

    /**
     * Returns the value of the current cell editor
     *
     * @return edited object
     */
    public Object getCellEditorValue() {
        Object value = currentEditor.getValue();
        if (value == null) {
            return oldValue;
        } else {
            return value;
        }
    }

    /**
     * A generic table cell editor interface. Implement this interface for specialized cell editors.
     */
    public interface AttribEditor {

        /**
         * Returns the editor component
         *
         * @return AWT or Swing component of editor
         */
        Component getComponent();

        /**
         * Returns the currently edited cell value
         *
         * @return edited object.
         */
        Object getValue();

        /**
         * Sets the cell value diplayed by the component.
         *
         * @param object to initialize editor with
         */
        void setValue(Object o);
    }

    /** A combo box editor component for boolean attributes */
    public static class BooleanAttribEditor implements AttribEditor {

        /** A combo box diplaying the values TRUE and FALSE */
        JComboBox c = new JComboBox(
            new Object[] {Boolean.TRUE, Boolean.FALSE});

        /** @return combo box */
        public Component getComponent() {
            return c;
        }

        /** @return selected item of the combo box (true of false). */
        public Object getValue() {
            return c.getSelectedItem();
        }

        /**
         * Sets value of the combo box to the given object.
         *
         * @param o a Boolean object. If this object does not equal Boolean.TRUE the edited value is set to FALSE.
         */
        public void setValue(Object o) {
            if (o.equals(Boolean.TRUE)) {
                c.setSelectedIndex(0);
            } else {
                c.setSelectedIndex(1);
            }
        }
    }

    /**
     * An editor component for filename attributes of type desmoj.util.Filename
     */
    public static class FilenameAttribEditor implements AttribEditor {

        /** Button to press for FileDialog */
        JButton button;

        /** A file open dialog */
        JDialog fileDlg;

        /** A standard file chooser component */
        JFileChooser jfc;

        /** Filename currently edited */
        String fname;

        /** Flag indicating if a directory is edited */
        boolean isDir;

        /** Creates a new filename editor. */
        public FilenameAttribEditor() {

            // init file chooser
            jfc = new JFileChooser();
            jfc.setDialogType(JFileChooser.OPEN_DIALOG);
            jfc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getActionCommand().equals(
                        JFileChooser.APPROVE_SELECTION)) {
                        fname = jfc.getSelectedFile().getAbsolutePath();
                        fileDlg.setVisible(false);
                    } else if (e.getActionCommand().equals(
                        JFileChooser.CANCEL_SELECTION)) {
                        fileDlg.setVisible(false);
                    }
                }
            });

            // init dialog
            fileDlg = new JDialog();
            fileDlg.getContentPane().add(jfc);
            fileDlg.setTitle("Open file...");
            fileDlg.setModal(true);
            fileDlg.setSize(400, 300);

            // init edit button
            button = new JButton("Edit File Name...");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileDlg.setVisible(true);
                }
            });

        }

        /** @return a JButton to press for file dialog to appear. */
        public Component getComponent() {
            return button;
        }

        /** @return a Filename object. */
        public Object getValue() {
            return new Filename(fname, isDir);
        }

        /**
         * Sets the currently edited value.
         *
         * @param o a Filename object.
         */
        public void setValue(Object o) {
            Filename fn = (Filename) o;
            fname = fn.toString();
            isDir = fn.isDirectory();
            jfc.setCurrentDirectory(new File(fname));
            if (isDir) {
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
        }
    }

    /**
     * A default text editor consisting of a single TextField. Can edit all data types with a constructor whose single
     * argument is a String.
     */
    public static class TextBasedEditor implements AttribEditor {

        /** The text field */
        protected JTextField c;

        /** The class of the currently edited object */
        protected Class currentClass;

        /** The editor's number format. */
        private final Format format;

        /** Creates a new TextBasedEditor */
        public TextBasedEditor() {
            c = new JTextField();
            format = createFormat();
        }

        /** @return a JTextField to enter text content */
        public Component getComponent() {
            return c;
        }

        /**
         * Returns null as an empty format. Subclasses can override this method to implement formats the edited text is
         * validated against.
         *
         * @return a new Format
         */
        protected Format createFormat() {
            return null;
        }

        /** @return the format used by this editor to validate text entries. */
        public Format getFormat() {
            return format;
        }

        /**
         * Returns the value from the text field as an object of the current edited class. Tries to create an object by
         * invoking the current class' constructor with a single String argument. If this action fails null is
         * returned.
         *
         * @return edited object
         */
        public Object getValue() {
            String s = c.getText();
            if (format == null) {
                try {
                    if (currentClass == null) {
                        return null;
                    } else {
                        Constructor cons = currentClass
                            .getConstructor(String.class);
                        return cons.newInstance(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                try {
                    return format.parseObject(s);
                } catch (java.text.ParseException ex) {
                    return null;
                }
            }
        }

        /**
         * Sets the text fields value to the given value. If the value is null the text field is initialized with an
         * empty string
         *
         * @param object to init textfield with
         */
        public void setValue(Object value) {
            if (value == null) {
                c.setText("");
                currentClass = null;
            } else {
                if (format != null) {
                    c.setText(format.format(value));
                } else {
                    c.setText(value.toString());
                }
                currentClass = value.getClass();
            }
        }
    }

    /** Editor for a string component */
    public static class StringEditor extends TextBasedEditor {

        /** @return text displayed in editor text field. */
        public Object getValue() {
            // System.out.println("String editor getValue");
            return c.getText();
        }

        /**
         * Sets contents of editor text field to the given string.
         *
         * @param a String object.
         */
        public void setValue(Object o) {
            // System.out.println("String editor setValue");
            if (o == null) {
                c.setText("");
            } else {
                c.setText((String) o);
            }
        }
    }

    /** Editor for all integer based types (Byte, Short, Integer, Long) */
    public static class IntegerNumberEditor extends TextBasedEditor {

        /**
         * Reads and returns integral number from the editor component. If it cannot be parsed correctly null is
         * returned
         *
         * @return a Number object whose actual subclass depends on the edited object.
         */
        public Object getValue() {
            try {
                Number num = ((DecimalFormat) getFormat()).parse(c.getText());
                if (currentClass.equals(Byte.class)) {
                    return Byte.valueOf(num.byteValue());
                } else if (currentClass.equals(Short.class)) {
                    return Short.valueOf(num.shortValue());
                } else if (currentClass.equals(Integer.class)) {
                    return Integer.valueOf(num.intValue());
                } else if (currentClass.equals(Long.class)) {
                    return Long.valueOf(num.longValue());
                } else {
                    return null;
                }
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(getComponent(),
                    "Enter a valid integer number!");

                return null;
            }
        }

        /**
         * Returns a decimal format with no allowed fraction digits.
         *
         * @return format to validate inputs against.
         */
        protected Format createFormat() {
            NumberFormat n = NumberFormat.getNumberInstance();
            n.setMaximumFractionDigits(0);
            return n;
        }
    }

    /** Editor for all floating point types (Float, Double) */
    public static class FloatingPointNumberEditor extends TextBasedEditor {

        /**
         * Reads and returns a floating point number from the editor component. If it cannot be parsed correctly null is
         * returned
         *
         * @return a Number object whose actual subclass depends on the edited object.
         */
        public Object getValue() {
            try {
                Number num = ((DecimalFormat) getFormat()).parse(c.getText());
                if (currentClass.equals(Double.class)) {
                    return new Double(num.doubleValue());
                } else if (currentClass.equals(Float.class)) {
                    return new Float(num.floatValue());
                } else {
                    return null;
                }
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(getComponent(),
                    "Enter a valid floating point number!");

                return null;
            }
        }

        /** @return a US style decimal format */
        protected Format createFormat() {
            return NumberFormat.getNumberInstance(java.util.Locale.US);
        }
    }

    /** A combo box editor component for TimeUnits */
    public static class TimeUnitAttribEditor implements AttribEditor {

        /** A combo box diplaying TimeUnits */
        JComboBox c = new JComboBox(
            new Object[] {TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS, TimeUnit.SECONDS,
                TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS});

        /** @return combo box */
        public Component getComponent() {
            return c;
        }

        /** @return selected item of the combo box. */
        public Object getValue() {
            return c.getSelectedItem();
        }

        /**
         * Sets value of the combo box to the given object.
         *
         * @param o a TimeUnit object.
         */
        public void setValue(Object o) {
            if (o.equals(TimeUnit.MICROSECONDS)) {
                c.setSelectedIndex(0);
            } else if (o.equals(TimeUnit.MILLISECONDS)) {
                c.setSelectedIndex(1);
            } else if (o.equals(TimeUnit.SECONDS)) {
                c.setSelectedIndex(2);
            } else if (o.equals(TimeUnit.MINUTES)) {
                c.setSelectedIndex(3);
            } else if (o.equals(TimeUnit.HOURS)) {
                c.setSelectedIndex(4);
            } else if (o.equals(TimeUnit.DAYS)) {
                c.setSelectedIndex(5);
            }
        }
    }
}