package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Generate HelpDialog about SimulationEngine
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
public class AboutEngineDialog extends HelpDialogTemplate {

    public AboutEngineDialog(ViewerPanel viewer) {
        super(viewer);
    }

    protected void buildDialog() throws MalformedURLException {
        String text = this.getLanguage().getString("Help_about") +
            " " + this.getViewer().getViewerName();
        JDialog dialog = new JDialog((JFrame) null, text, true);
        dialog.setBounds(100, 100, 600, 450);
        dialog.getContentPane().setLayout(new GridLayout(1, 1));
        Box mainBox = Box.createVerticalBox();
        mainBox.add(this.getHeaderBox());
        mainBox.add(Box.createRigidArea(new Dimension(10, 10)));
        mainBox.add(this.getDesmojBox(dialog));
        mainBox.add(Box.createRigidArea(new Dimension(10, 10)));
        mainBox.add(this.getVisualization2dBox(dialog));
        mainBox.add(Box.createRigidArea(new Dimension(10, 10)));
        mainBox.add(this.getLicenseBox());
        dialog.getContentPane().add(mainBox);
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

    private Box getDesmojBox(JDialog dialog) throws MalformedURLException {
        URL desmoJ_project = this.getExternURL("DESMOJ_PROJECT_URL");
        URL desmoJ_contributors = this.getExternURL("DESMOJ_CONTRIBUTORS_URL");
        URL desmojHtml = this.getInternURL("desmoj_html");

        String content = this.readContent(desmojHtml, dialog.getWidth() - 30);
        Box contentBox = Box.createHorizontalBox();
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(ViewerPanel.FONT_MIDDLE);
        contentBox.add(contentLabel);
        contentBox.add(Box.createHorizontalGlue());

        JButton projectButton = new JButton(this.getLanguage().
            getString("Help_moreInformation"));
        projectButton.addActionListener(new Click(desmoJ_project, this.getAppletContext()));
        projectButton.setFont(ViewerPanel.FONT_MIDDLE);
        JButton contributorsButton = new JButton(this.getLanguage().
            getString("Help_Contributors"));
        contributorsButton.addActionListener(new Click(desmoJ_contributors, this.getAppletContext()));
        contributorsButton.setFont(ViewerPanel.FONT_MIDDLE);
        String text = this.getLanguage().getString("Help_Version") + " " +
            this.getViewer().getDesmoJ_Info().getDesmoJ_Version();
        JLabel version = new JLabel(text);
        version.setFont(ViewerPanel.FONT_MIDDLE);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(version);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(contributorsButton);
        buttonBox.add(projectButton);

        Box outBox = Box.createVerticalBox();
        outBox.add(contentBox);
        outBox.add(buttonBox);
        return outBox;
    }

    private Box getVisualization2dBox(JDialog dialog) throws MalformedURLException {
        URL visualization2d_project = this.getExternURL("VISUALIZATION_PROJECT_URL");
        URL visualization2d_contributors = this.getExternURL("VISUALIZATION_CONTRIBUTORS_URL");
        URL visualization2dHtml = this.getInternURL("visualization2d_html");

        String content = this.readContent(visualization2dHtml, dialog.getWidth() - 30);
        Box contentBox = Box.createHorizontalBox();
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(ViewerPanel.FONT_MIDDLE);
        contentBox.add(contentLabel);
        contentBox.add(Box.createHorizontalGlue());

        JButton projectButton = new JButton(this.getLanguage().
            getString("Help_moreInformation"));
        projectButton.addActionListener(new Click(visualization2d_project, this.getAppletContext()));
        projectButton.setFont(ViewerPanel.FONT_MIDDLE);
        JButton contributorsButton = new JButton(this.getLanguage().
            getString("Help_Contributors"));
        contributorsButton.addActionListener(new Click(visualization2d_contributors, this.getAppletContext()));
        contributorsButton.setFont(ViewerPanel.FONT_MIDDLE);
        String text = this.getLanguage().getString("Help_Version") + " " +
            this.getLanguage().getString("VISUALIZATION_VERSION");
        JLabel version = new JLabel(text);
        version.setFont(ViewerPanel.FONT_MIDDLE);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(version);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(contributorsButton);
        buttonBox.add(projectButton);

        Box outBox = Box.createVerticalBox();
        outBox.add(contentBox);
        outBox.add(buttonBox);
        outBox.setBackground(ViewerPanel.HEADER_BG_COLOR);
        outBox.setOpaque(true);
        return outBox;
    }

    private Box getLicenseBox() throws MalformedURLException {
        DesmoJ_Info info = this.getViewer().getDesmoJ_Info();
        URL licence = info.getDesmoJ_LicenseURL();

        String text = this.getLanguage().getString("Help_License") + " " +
            info.getDesmoJ_License();
        JLabel licenseLabel = new JLabel(text);
        licenseLabel.setFont(ViewerPanel.FONT_MIDDLE);
        JButton licenseButton = new JButton(this.getLanguage().
            getString("Help_moreInformation"));
        licenseButton.addActionListener(new Click(licence, this.getAppletContext()));
        licenseButton.setFont(ViewerPanel.FONT_MIDDLE);
        Box licenseBox = Box.createHorizontalBox();
        licenseBox.add(licenseLabel);
        licenseBox.add(Box.createHorizontalGlue());
        licenseBox.add(licenseButton);
        return licenseBox;
    }

}
