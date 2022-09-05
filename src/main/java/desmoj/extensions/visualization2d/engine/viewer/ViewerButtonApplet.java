package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import desmoj.extensions.visualization2d.engine.command.Parameter;


/**
 * Applet zum Animieren von Simulationen<br> siehe auch animation.viewer.ViewerFrame und animation.viewer.ViewerPanel
 * Parameter:<br> "cmdsUrl", "URL", "absolute oder relative URL of commands file"<br> "simulationIconDir", "URL",
 * "absolute oder relative URL of simulationIconDir"<br> "locale", "Sprachsteuerung fuer die Anwendung"<br> "title",
 * "Beschriftung des Button"<br> "buttonColor", "Button-Farbe in red|green|blue Notation"<br> "viewerRectangle",
 * "ViewerRectangle in x|y|width|height Notation"<br>
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
public class ViewerButtonApplet extends JApplet {

    private static final long serialVersionUID = 1L;
    private static final Color DefaultButtonColor = Color.lightGray;
    private static final Rectangle DefaultViewerRect = new Rectangle(100, 100, 400, 300);

    private URL cmdsUrl = null;
    private URL simulationIconDir = null;
    private Locale locale = null;
    private String title = null;
    private Color buttonColor = null;
    private Rectangle viewerRectangle = null;
    private ViewerPanel viewerPanel = null;
    private ViewerButtonFrame frame = null;

    public void init() {

        String cmdsUrlString = this.getParameter("cmdsUrl");
        String simulationIconDirString = this.getParameter("simulationIconDir");
        String localeString = this.getParameter("locale");
        String titleString = this.getParameter("title");
        String buttonColorString = this.getParameter("buttonColor");
        String viewerRectangleString = this.getParameter("viewerRectangle");
        System.out.println("cmdsUrl: " + cmdsUrlString);
        System.out.println("simulationIconDir: " + simulationIconDirString);
        System.out.println("locale: " + localeString);
        System.out.println("title: " + titleString);
        System.out.println("buttonColor: " + buttonColorString);
        System.out.println("viewerRectangle: " + viewerRectangleString);

        this.showStatus("Begin of init");

        if (cmdsUrlString != null) {
            try {
                this.cmdsUrl = new URL(cmdsUrlString);
            } catch (MalformedURLException e) {
                this.cmdsUrl = null;
            }
            if (this.cmdsUrl == null) {
                try {
                    this.cmdsUrl = new URL(this.getCodeBase(), cmdsUrlString);
                } catch (MalformedURLException e) {
                    this.cmdsUrl = null;
                }
            }
            if (this.cmdsUrl == null) {
                this.showStatus("The Parameter cmdsUrl: " + cmdsUrlString + " hasn't a valid syntax!");
                System.out.println("The Parameter cmdsUrl: " + cmdsUrlString + " hasn't a valid syntax!");
            }
        } else {
            this.cmdsUrl = null;
            this.showStatus("There is no parameter cmdsUrl");
            System.out.println("There is no parameter cmdsUrl");
        }

        if (simulationIconDirString != null) {
            try {
                this.simulationIconDir = new URL(simulationIconDirString);
            } catch (MalformedURLException e) {
                this.simulationIconDir = null;
            }
            if (this.simulationIconDir == null) {
                try {
                    this.simulationIconDir = new URL(this.getCodeBase(), simulationIconDirString);
                } catch (MalformedURLException e) {
                    this.simulationIconDir = null;
                }
            }
            if (this.simulationIconDir == null) {
                this.showStatus(
                    "The Parameter simulationIconDir: " + simulationIconDirString + " hasn't a valid syntax!");
                System.out.println(
                    "The Parameter simulationIconDir: " + simulationIconDirString + " hasn't a valid syntax!");
            }
        } else {
            this.simulationIconDir = null;
            this.showStatus("There is no parameter simulationIconDir");
            System.out.println("There is no parameter simulationIconDir");
        }

        if (localeString != null) {
            this.locale = new Locale(localeString);
            if (this.locale == null) {
                this.locale = Locale.ENGLISH;
            }
        } else {
            this.locale = Locale.ENGLISH;
        }

        this.buttonColor = ViewerButtonApplet.DefaultButtonColor;
        try {
            String[] rgb = Parameter.split(buttonColorString);
            this.buttonColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        } catch (Exception e) {
            this.buttonColor = ViewerButtonApplet.DefaultButtonColor;
        }

        this.viewerRectangle = ViewerButtonApplet.DefaultViewerRect;
        try {
            String[] rect = Parameter.split(viewerRectangleString);
            this.viewerRectangle =
                new Rectangle(Integer.parseInt(rect[0]), Integer.parseInt(rect[1]), Integer.parseInt(rect[2]),
                    Integer.parseInt(rect[3]));
        } catch (Exception e) {
            this.viewerRectangle = ViewerButtonApplet.DefaultViewerRect;
        }

        this.showStatus("Nach Parameter Auswertung");
        System.out.println("Nach Parameter Auswertung");

        this.viewerPanel = new ViewerPanel(this.cmdsUrl,
            this.simulationIconDir, this.getAppletContext(),
            this.locale);
        this.getContentPane().setLayout(new GridLayout(1, 1));

        if (titleString != null) {
            this.title = titleString;
        } else {
            this.title = this.viewerPanel.getViewerName();
        }
        JButton button = new JButton();
        button.setBackground(this.buttonColor);
        button.setText(this.title);
        button.addActionListener(new Click());
        this.getContentPane().add(button);
        this.setVisible(true);
        this.showStatus("End of init");
        System.out.println("end of init");
    }

    public void start() {
        System.out.println("end of start");
    }


    public void destroy() {
        if (this.frame != null) {
            // SimulationsThread beenden
            if (viewerPanel.getSimulationThread() != null) {
                viewerPanel.getSimulationThread().interrupt();
                try {
                    viewerPanel.getSimulationThread().join();
                } catch (InterruptedException ei) {
                }
                viewerPanel.setSimulationThreadNull();
            }
            this.frame.setVisible(false);
            this.frame.dispose();
        }
        System.out.println("end of destroy");
    }

    public String[][] getParameterInfo() {
        String[][] out = {
            {"cmdsUrl", "URL", "absolute oder relative URL of commands file"},
            {"simulationIconDir", "URL", "absolute oder relative URL of simulationIconDir"},
            {"locale", "Locale", "Sprachsteuerung"},
            {"title", "String", "Beschriftung des Button"},
            {"buttonColor", "String", "Button-Farbe in red|green|blue Notation"},
            {"viewerRectangle", "String", "ViewerRectangle in x|y|width|height Notation"},
        };
        return out;
    }

    public String getAppletInfo() {
        String out = "Viewer ButtonApplet: " + this.viewerPanel.getViewerName();
        return out;
    }

    /**
     * startet ViewerButtonFrame
     */
    class Click implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            //System.out.println("Click");
            frame = new ViewerButtonFrame();
            if (cmdsUrl != null && simulationIconDir != null) {
                try {
                    viewerPanel.fileOpen();
                    viewerPanel.lastCall();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Eigenstaendiger Frame zum Anzeigen des ViewerPanel
     */
    class ViewerButtonFrame extends JFrame {
        private static final long serialVersionUID = 1L;

        public ViewerButtonFrame() {
            super(title);
            if (viewerPanel != null) {
                this.getContentPane().setLayout(new GridLayout(1, 1));
                this.getContentPane().add(viewerPanel);
                // setings for ViewerApplet
                this.setJMenuBar(viewerPanel.createMenueBar(null));
                this.getLayeredPane().add(ViewerPanel.getInfoPane(), JLayeredPane.DRAG_LAYER);
            }
            this.setBounds(viewerRectangle);
            this.setVisible(true);
        }
    }
}
