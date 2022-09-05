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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import desmoj.extensions.visualization2d.engine.model.Model;

/**
 * Generate HelpDialog about simulation model
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
public class AboutModelDialog extends HelpDialogTemplate {

    private Model model = null;

    public AboutModelDialog(ViewerPanel viewer) {
        super(viewer);
        model = this.getViewer().getModel();
    }

    protected void buildDialog() throws MalformedURLException {
        if (model.isValid()) {
            String projectName = this.getViewer().getModel().getModelProjectName();
            if (projectName == null) {
                projectName = "Simulation Project";
            }
            String text = this.getLanguage().getString("Help_about") + " " + projectName;
            JDialog dialog = new JDialog((JFrame) null, text, true);
            dialog.setBounds(100, 100, 600, 400);
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(this.getHeaderBox(), BorderLayout.NORTH);
            JScrollPane scrollpane = new JScrollPane(this.getProjectBox(dialog));
            scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            dialog.getContentPane().add(scrollpane, BorderLayout.CENTER);
            dialog.setResizable(false);
            dialog.setVisible(true);
        }
    }

    private Box getHeaderBox() throws MalformedURLException {
        String projectName = this.getViewer().getModel().getModelProjectName();

        JLabel projectIconLabel = new JLabel(this.getProjectTitleIcon());
        JLabel title = new JLabel(projectName);
        title.setFont(ViewerPanel.FONT_BIG);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        Box outBox = Box.createHorizontalBox();
        outBox.add(Box.createHorizontalGlue());
        outBox.add(title);
        outBox.add(Box.createHorizontalGlue());
        outBox.add(projectIconLabel);
        outBox.setBackground(ViewerPanel.HEADER_BG_COLOR);
        outBox.setOpaque(true);
        return outBox;
    }

    private Box getProjectBox(JDialog dialog) throws MalformedURLException {
        String projectURLString = this.getViewer().getModel().getModelProjectURL();
        URL projectURL = null;
        try {
            if (projectURLString != null) {
                projectURL = new URL(projectURLString);
            }
        } catch (MalformedURLException e) {
            projectURL = null;
        }

        String content = "";
        content += "<html><body><center>";
        content += "<table border=0 width=\"" + (dialog.getSize().width - 10) + "\">";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Project") +
            " </b></td><td>" + this.getViewer().getModel().getModelProjectName() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Model") +
            " </b></td><td>" + this.getViewer().getModel().getModelName() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Author") +
            " </b></td><td>" + this.getViewer().getModel().getModelAuthor() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_CreatedAt") +
            " </b></td><td>" + this.getViewer().getModel().getModelCreatedAt() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Description") +
            " </b></td><td>" + this.getViewer().getModel().getModelDescription() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Remarks") +
            " </b></td><td>" + this.getViewer().getModel().getModelRemark() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_LicenseModel") +
            " </b></td><td>" + this.getViewer().getModel().getModelLicence() + "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_SimulationFramework") +
            " </b></td><td>" + this.getLanguage().getString("Help_Version") + " " +
            this.getViewer().getDesmoJ_Info().getDesmoJ_Version() +
            " <img align=\"middle\" src=\"" + this.getInternURL("u_hamburg_menue_icon").toString() + "\" > " +
            "</td></tr>";
        content += "<tr><td><b>" + this.getLanguage().getString("Help_Visualization2dEngine") +
            " </b></td><td>" + this.getLanguage().getString("Help_Version") + " " +
            this.getLanguage().getString("VISUALIZATION_VERSION") +
            " <img align=\"middle\" src=\"" + this.getInternURL("th_wildau_menue_icon").toString() + "\" > " +
            "</td></tr>";
        content += "</table></center></body></html>";

        Box contentBox = Box.createHorizontalBox();
        JLabel contentLabel = new JLabel(content);
        contentLabel.setFont(ViewerPanel.FONT_MIDDLE);
        contentBox.add(contentLabel);
        contentBox.add(Box.createHorizontalGlue());

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        if (projectURL != null) {
            JButton projectButton = new JButton("more Information");
            projectButton.addActionListener(new Click(projectURL, this.getAppletContext()));
            projectButton.setFont(ViewerPanel.FONT_MIDDLE);
            buttonBox.add(projectButton);
        }

        Box outBox = Box.createVerticalBox();
        outBox.add(contentBox);
        outBox.add(buttonBox);
        return outBox;
    }

    private ImageIcon scale(Image src, int height) {
        int w = src.getWidth(this.getViewer());
        int h = src.getHeight(this.getViewer());
        double scale = (double) height / (double) h;
        int width = (int) Math.round(scale * w);
        int type = BufferedImage.BITMASK;
        BufferedImage dst = new BufferedImage(width, height, type);
        Graphics2D g2 = dst.createGraphics();
        g2.drawImage(src, 0, 0, width, height, this.getViewer());
        g2.dispose();
        return new ImageIcon(dst);
    }

    private ImageIcon getProjectIcon(int height) {
        ImageIcon out = null;
        Model model = this.getViewer().getModel();
        String projectIconId = model.getModelProjectIconId();
        if (projectIconId != null) {
            try {
                Image image = model.getImage(projectIconId);
                out = this.scale(image, height);
                //out = new ImageIcon(image);
            } catch (Exception e) {
                out = null;
            }
        }
        return out;
    }

    protected ImageIcon getProjectTitleIcon() {
        return this.getProjectIcon(60);
    }

    protected ImageIcon getProjectMenueIcon() {
        // bestimme Referenzhoehe fuer Icon
        int h = new ImageIcon(this.getInternURL("th_wildau_menue_icon")).getIconHeight();
        return this.getProjectIcon(h);
    }


}
