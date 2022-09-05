package desmoj.extensions.experimentation.ui;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

/**
 * A simple HTML Browser for displaying experiment reports, traces, error- and debug-files.
 *
 * @author Nicolas Knaak
 * @author edited by Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class BrowserPanel extends JPanel {

    /** Layout of main component */
    BorderLayout borderLayout1 = new BorderLayout();

    /** Scroll pane for browser (editor) component */
    JScrollPane scrollPane = new JScrollPane();

    /** Editor component used as browser */
    JEditorPane editorPane = new JEditorPane();

    /** Status line showing URL or error information */
    JLabel statusLine = new JLabel();

    /** Tree for showing report URLs */
    URLTreePanel urlTree = new URLTreePanel();

    /** Scroll pane for URLTree */
    JScrollPane treeScroll = new JScrollPane();

    /** shall the urlTree be shown or not */
    boolean showUrlTree = true;

    /** Creates a new Browser panel */
    public BrowserPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new Browser panel
     *
     * @param showUrlTree A flag indicating if a URL tree will be shown in this BrowserPanel
     */
    public BrowserPanel(boolean showUrlTree) {
        this.showUrlTree = showUrlTree;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the current page to the given URL.
     *
     * @param url URL of page to display in browser
     */
    public void setPage(URL url) {
        try {
            editorPane.setPage(url);
            statusLine.setText(url.toString());
        } catch (IOException e) {
            statusLine.setText("** ERROR: " + e.getMessage());
        }
    }

    /** Initializes the user interface */
    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        statusLine.setText("URL:");
        this.add(statusLine, BorderLayout.SOUTH);
        scrollPane.getViewport().add(editorPane, null);
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    setPage(evt.getURL());
                }
            }
        });
        editorPane.setEditable(false);
        if (showUrlTree) {
            treeScroll.getViewport().add(urlTree.tree, null);
            this.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll,
                scrollPane), BorderLayout.CENTER);
        } else {
            this.add(scrollPane);
        }
    }
}