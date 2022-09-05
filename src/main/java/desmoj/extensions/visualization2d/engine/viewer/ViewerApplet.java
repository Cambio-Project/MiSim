package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.JApplet;
import javax.swing.JLayeredPane;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import desmoj.extensions.visualization2d.engine.modelGrafic.ModelGraficException;


/**
 * Applet zum Animieren von Simulationen<br> siehe auch animation.viewer.ViewerFrame und animation.viewer.ViewerPanel
 * Parameter:<br> "cmdsUrl", "URL", "absolute oder relative URL of commands file"<br> "simulationIconDir", "URL",
 * "absolute oder relative URL of simulationIconDir"<br> "locale", "Sprachsteuerung fuer die Anwendung"<br>
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
public class ViewerApplet extends JApplet {

    private static final long serialVersionUID = 1L;
    private URL cmdsUrl = null;
    private URL simulationIconDir = null;
    private Locale locale = null;
    private ViewerPanel viewerPanel = null;

    public void init() {

        String cmdsUrlString = this.getParameter("cmdsUrl");
        String simulationIconDirString = this.getParameter("simulationIconDir");
        String localeString = this.getParameter("locale");
        System.out.println("cmdsUrl: " + cmdsUrlString);
        System.out.println("simulationIconDir: " + simulationIconDirString);
        System.out.println("locale: " + localeString);
        this.getAppletContext();

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

        this.showStatus("Nach Parameter Auswertung");
        System.out.println("Nach Parameter Auswertung");

        this.getContentPane().setLayout(new GridLayout(1, 1));

        if (this.cmdsUrl != null && this.simulationIconDir != null) {
            this.viewerPanel = new ViewerPanel(this.cmdsUrl,
                this.simulationIconDir, this.getAppletContext(),
                this.locale);
            // setings for ViewerApplet
            this.setJMenuBar(this.viewerPanel.createMenueBar(null));
            try {
                this.viewerPanel.fileOpen();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.showStatus("Nach ViewerPanel Initialisierung");
        System.out.println("Nach ViewerPanel Initialisierung");

        if (this.viewerPanel != null) {
            this.getContentPane().add(this.viewerPanel);
            // setings for ViewerApplet
            this.getLayeredPane().add(ViewerPanel.getInfoPane(), JLayeredPane.DRAG_LAYER);
        }
        this.setVisible(true);
        this.showStatus("End of init");
        System.out.println("end of init");
    }

    public void start() {
        if (this.viewerPanel != null) {
            this.viewerPanel.lastCall();
        }
        this.showStatus("End of start");
        System.out.println("end of start");
    }


    public void destroy() {
        if (this.viewerPanel != null) {
            try {
                this.viewerPanel.fileClose();
            } catch (ModelGraficException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("end of destroy");
    }

    public String[][] getParameterInfo() {
        String[][] out = {
            {"cmdsUrl", "URL", "absolute oder relative URL of commands file"},
            {"simulationIconDir", "URL", "absolute oder relative URL of simulationIconDir"},
            {"locale", "Locale", "Sprachsteuerung"},
        };
        return out;
    }

    public String getAppletInfo() {
        String out = "Viewer Applet: " + this.viewerPanel.getViewerName();
        return out;
    }
}
