package desmoj.extensions.visualization2d.engine.viewer;

import java.applet.AppletContext;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * ActionHandler to open a browser with given URL
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
public class Click implements ActionListener {

    private URL url = null;
    private Desktop desktop = null;
    private AppletContext appletContext = null;

    /**
     * ActionHandler to open a given url For Desktop-Applications Desktop.browse is used. For Applets
     * appletContext.showDocument is used
     *
     * @param url           URL to show
     * @param appletContext when null, Desktop.browse is used
     */
    public Click(URL url, AppletContext appletContext) {
        this.url = url;
        this.appletContext = appletContext;
        this.desktop = this.initDesktopSupport();
    }

    public void actionPerformed(ActionEvent arg0) {
        if (this.appletContext != null) {
            this.appletContext.showDocument(this.url, "_blank");
        } else if (desktop != null) {
            try {
                desktop.browse(this.url.toURI());
            } catch (Exception e) {
            }
        }
    }

    private Desktop initDesktopSupport() {
        Desktop desktop = null;
        // pruefe ob Browser angesprochen werden kann
        if (Desktop.isDesktopSupported()) {
            // Desktop wird vom OS unterstuetzt
            desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop = null;
            }
        }
        return desktop;
    }

}
