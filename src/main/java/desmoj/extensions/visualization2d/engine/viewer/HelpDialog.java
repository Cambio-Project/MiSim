package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Generate HelpDialog for offline help
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
public class HelpDialog extends HelpDialogTemplate {

    public HelpDialog(ViewerPanel viewer) {
        super(viewer);
    }

    protected void buildDialog() throws MalformedURLException {
        String title = this.getLanguage().getString("Help_about") +
            " " + this.getViewer().getViewerName();
        JDialog dialog = new JDialog((JFrame) null, title, true);
        dialog.setBounds(100, 100, 600, 450);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(this.getHeaderBox(), BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(this.getHelpBox(dialog));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private Box getHeaderBox() {
        JLabel u_hamburg = new JLabel(new ImageIcon(this.getInternURL("u_hamburg_title_icon")));
        JLabel th_wildau = new JLabel(new ImageIcon(this.getInternURL("th_wildau_title_icon")));
        JLabel title = new JLabel(this.getViewer().getViewerName());
        title.setFont(ViewerPanel.FONT_BIG);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        Box outBox = Box.createHorizontalBox();
        outBox.add(u_hamburg);
        outBox.add(Box.createHorizontalGlue());
        outBox.add(title);
        outBox.add(Box.createHorizontalGlue());
        outBox.add(th_wildau);
        outBox.setBackground(ViewerPanel.HEADER_BG_COLOR);
        outBox.setOpaque(true);
        return outBox;
    }

    private Box getHelpBox(JDialog dialog) throws MalformedURLException {
        URL visualization2d_project = this.getExternURL("VISUALIZATION_PROJECT_URL");
        URL visualization2d_onlineHelp = this.getExternURL("VISUALIZATION_HELP_URL");
        URL helpHtml = this.getInternURL("help_html");
        String content = this.readContent(helpHtml, dialog.getWidth() - 30);
        JLabel contentLabel = new JLabel(content);
        Box contentBox = Box.createHorizontalBox();
        contentBox.add(contentLabel);
        contentBox.add(Box.createHorizontalGlue());

        JButton projectButton = new JButton(this.getLanguage().getString("Help_moreInformation"));
        projectButton.addActionListener(new Click(visualization2d_project, this.getAppletContext()));
        projectButton.setFont(ViewerPanel.FONT_MIDDLE);
        JButton contributorsButton = new JButton(this.getLanguage().getString("Menu_HelpOnline"));
        contributorsButton.addActionListener(new Click(visualization2d_onlineHelp, this.getAppletContext()));
        contributorsButton.setFont(ViewerPanel.FONT_MIDDLE);
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(contributorsButton);
        buttonBox.add(projectButton);

        Box outBox = Box.createVerticalBox();
        outBox.add(contentBox);
        outBox.add(buttonBox);
        return outBox;
    }


}
