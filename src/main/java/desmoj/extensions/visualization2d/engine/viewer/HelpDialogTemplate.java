package desmoj.extensions.visualization2d.engine.viewer;

import java.applet.AppletContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Superclass of all HelpDialogs
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
public abstract class HelpDialogTemplate implements ActionListener {

    private ViewerPanel viewer = null;
    private LanguageSupport language = null;


    public HelpDialogTemplate(ViewerPanel viewer) {
        this.viewer = viewer;
        this.language = viewer.getLanguage();
    }

    protected abstract void buildDialog() throws MalformedURLException;

    protected ViewerPanel getViewer() {
        return this.viewer;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            this.buildDialog();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
    }

    protected AppletContext getAppletContext() {
        return viewer.getAppletContext();
    }

    protected LanguageSupport getLanguage() {
        return this.language;
    }

    protected URL getInternURL(String key) {
        return this.language.getInternURL(key);
    }

    protected URL getExternURL(String key) {
        return this.language.getExternURL(key);
    }

    protected String readContent(URL url, int width) {
        StringBuffer content = new StringBuffer();
        String line = "";
        //System.out.println(url);
        try {
            BufferedReader f = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = f.readLine()) != null) {
                content.append(line + "\n");
            }
            f.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (width >= 0) {
            String width_old = "width=\"xxx\"";
            String width_new = "width=\"" + width + "\"";
            int v0 = content.indexOf(width_old);
            int v1 = v0 + width_old.length();
            content.replace(v0, v1, width_new);
        }

        return content.toString();
    }


}
