package desmoj.extensions.experimentation.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Vector;

/**
 * Construct a tree with a root that has each experiment as a child. Each experiment has the URLs for the outputfiles as
 * children. By selecting a leaf in the tree the coresponding URL will be opened.
 *
 * @author Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */

public class URLTreePanel extends JPanel {

    /** The currently selected URL */
    protected URL url;
    /** Root of the URL tree */
    MutableTreeNode treeRoot = new DefaultMutableTreeNode("Simulation Output");
    /** Tree model to display */
    DefaultTreeModel treeModel = new DefaultTreeModel(treeRoot);
    /** GUI component */
    JTree tree = new JTree(treeModel);
    JScrollPane scrollPane;
    /** the root index of the currently active folder * */
    private int rootIndex = -1;
    /** A vector of simulation output directories */
    private final Vector directory = new Vector();
    /** A vector of outputfile appendixes */
    private final Vector appendix = new Vector();

    /** Creates a new URLTreePanel */
    public URLTreePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Initializes the user interface */
    private void jbInit() throws Exception {
        scrollPane = new JScrollPane(tree);
        scrollPane.getViewport().add(tree, null);
        this.setLayout(new BorderLayout());
        this.add(scrollPane);
    }

    /**
     * Add a new node to the tree and save the path name for the url of the corresponding simulation output.
     *
     * @param nodeText   String: text of node to create
     * @param dir        String: directory with simulation output belonging to this experiment
     * @param appendixes String[]: file type for debug, trace, error and report file (e.g. .xml)
     */
    public void createNode(String nodeText, String dir, String[] appendixes) {
        /** Test wether a new experiment name has been given * */
        boolean newExperiment = true;
        for (int i = 0; i < treeRoot.getChildCount(); i++) {
            /**
             * If no new name was given the tree won't have to be expanded.
             * However the directory and/or output types may have been changed,
             * so the data for them has to be updated.
             */
            if (nodeText.equals(treeRoot.getChildAt(i).toString())) {
                newExperiment = false;
                directory.removeElementAt(i);
                directory.add(i, dir);
                appendix.removeElementAt(i);
                appendix.add(i, appendixes);
                tree.setSelectionPath(new TreePath(treeRoot));
                rootIndex = -1;
            }
        }
        /**
         * If a new name was given new files have been created. New nodes will
         * be created in the tree for them.
         */
        if (newExperiment) {
            MutableTreeNode newNode = new DefaultMutableTreeNode(nodeText);
            directory.add(treeRoot.getChildCount(), dir);
            appendix.add(treeRoot.getChildCount(), appendixes);
            treeModel.insertNodeInto(newNode, treeRoot, treeRoot
                .getChildCount());
            newNode.insert(new DefaultMutableTreeNode("debug"), 0);
            newNode.insert(new DefaultMutableTreeNode("trace"), 1);
            newNode.insert(new DefaultMutableTreeNode("error"), 2);
            newNode.insert(new DefaultMutableTreeNode("report"), 3);
        }
    }

    /***************************************************************************
     * Two Actions listeners handle the selections and clicks on the tree.
     *
     * @param reportStylerPanel
     *            ReportStylerPanel: a reportStylerPanel that will be updated
     *            depending on the file types of a selected Experiment Node
     **************************************************************************/
    public void createActionListener(final ReportStylerPanel reportStylerPanel) {
        tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        final JFrame standardFrame = new JFrame();
        standardFrame.setSize(640, 480);
        standardFrame.setLocation(150, 100);
        standardFrame.setVisible(false);
        /**
         * The TreeSelectionListener listens for selections of experiment-nodes
         * in the tree. If such a selection is detected it will cause the
         * ReportStylerPanel to update itself.
         */
        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                if (node.isRoot()) {
                    return;
                }
                if (!node.isLeaf()) {
                    if (treeRoot.getIndex(node) != rootIndex) {
                        rootIndex = treeRoot.getIndex(node);
                        updateView(rootIndex, node, reportStylerPanel);
                    }
                } else {
                    if (treeRoot.getIndex(node.getParent()) != rootIndex) {
                        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
                            .getParent();
                        rootIndex = treeRoot.getIndex(parentNode);
                        updateView(rootIndex, parentNode, reportStylerPanel);
                    }
                }
            }
        };
        /**
         * The MouseListener listens for clicks on tree-leafs. If such a click
         * is detected it will open the corresponding file. Holding down shift
         * will open a new window.
         */
        MouseListener mouseListener = new MouseListener() {
            public void mouseEntered(MouseEvent e) {

            }

            public void mouseExited(MouseEvent e) {

            }

            public void mouseReleased(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {

            }

            public void mouseClicked(MouseEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                if (node.isRoot()) {
                    return;
                }
                if (node.isLeaf()) {
                    String[] appendixes = (String[]) appendix
                        .elementAt(treeRoot.getIndex(node.getParent()));
                    String app = appendixes[node.getParent().getIndex(node)];
                    if (app.equals(".xml")) {
                        app = ".html";
                    }
                    String u = directory.elementAt(treeRoot.getIndex(node
                        .getParent()))
                        + "/"
                        + node.getParent().toString()
                        + "_"
                        + node + app;
                    String windowTitle = node.getParent().toString() + " "
                        + node;
                    try {
                        url = new URL(u);
                        /**
                         * using the right mouse button or holding shift down
                         * opens a new frame
                         */
                        if (e.getButton() == 3 || e.isShiftDown()) {
                            JFrame newFrame = new JFrame(windowTitle);
                            newFrame.setSize(640, 480);
                            newFrame.setLocation(150, 100);
                            BrowserPanel browser = new BrowserPanel(false);
                            newFrame.getContentPane().add(browser);
                            browser.setPage(url);
                            newFrame.setVisible(true);
                        } else {
                            standardFrame.getContentPane().removeAll();
                            standardFrame.setTitle(windowTitle);
                            BrowserPanel browser = new BrowserPanel(false);
                            standardFrame.getContentPane().add(browser);
                            browser.setPage(url);
                            standardFrame.setVisible(true);
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        };
        tree.addTreeSelectionListener(treeSelectionListener);
        tree.addMouseListener(mouseListener);
    }

    /***************************************************************************
     * this method was made to enable the Applet to open reports in new windows
     * of the browser it is running in. currently unused.
     *
     * @param appletContext
     *            AppletContext: The context the applet is run in (e.g. a
     *            browser window)
     **************************************************************************/
    public void createActionListener(final AppletContext appletContext) {
        /**
         * Only single selection is allowed since only one file can be displayed
         * at a given time
         */
        tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
                    .getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                /** only leafs of the tree are connected to files */
                if (!node.isLeaf()) {
                    return;
                }
                /** the empty root is also a leaf but has no connected file */
                if (node.isRoot()) {
                    return;
                }
                String[] appendixes = (String[]) appendix.elementAt(treeRoot
                    .getIndex(node.getParent()));
                String app = appendixes[node.getParent().getIndex(node)];
                if (app.equals(".xml")) {
                    app = ".html";
                }
                String u = directory.elementAt(treeRoot.getIndex(node
                    .getParent()))
                    + "/"
                    + node.getParent().toString()
                    + "_"
                    + node + app;
                try {
                    url = new URL(u);
                    appletContext.showDocument(url, "reports");
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        };
        tree.addTreeSelectionListener(treeSelectionListener);
    }

    /**
     * the call of this method lets the reportStylerPanel update its current display and provides the necessary values
     * for it.
     */
    private void updateView(int rIndex, DefaultMutableTreeNode treeNode,
                            ReportStylerPanel reportStylerPanel) {
        String baseFilename = directory.elementAt(rIndex) + "/"
            + treeNode.toString();
        baseFilename = baseFilename.replaceFirst("file:", "");
        String[] appendixes = (String[]) appendix.elementAt(rIndex);
        String traceFilename = baseFilename + "_trace" + appendixes[1];
        traceFilename.replaceFirst("file:", "");
        String reportFilename = baseFilename + "_report" + appendixes[3];
        reportFilename.replaceFirst("file:", "");
        reportStylerPanel.updateView(baseFilename, traceFilename,
            reportFilename);
    }
}