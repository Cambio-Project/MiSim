package desmoj.extensions.visualization2d.engine.viewer;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import desmoj.extensions.visualization2d.engine.model.Model;

/**
 * Diese Klasse stellt DesmoJ- Versions und Lizenzdaten bereit. Bei der Initialisierung werden diese Daten aus
 * desmoj.core.simulator.Experiment bezogen, falls diese Klasse bekannt ist. Zusaetzlich sind diese Daten auch in dem
 * cmds-File enthalten. Auf diese Daten wird mit update zugegriffen. Dieser Weg wird beschritten, da
 * desmoj.extensions.visualization2d.engine auch unabhaengig vom Rest-System funktionsfaehig sein soll, z.B. als
 * Viewer-Applet.
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
public class DesmoJ_Info {

    private String desmoJ_version = "";
    private String desmoJ_license = "";
    private URL desmoJ_licenseURL = null;

    public DesmoJ_Info() {
        this.init();
    }

    /**
     * aktualisiert Versions- und Lizenzdaten aus dem Model
     *
     * @param model
     */
    protected void update(Model model) {
        desmoJ_version = model.getDesmojVersion();
        desmoJ_license = model.getDesmojLicense();
        try {
            desmoJ_licenseURL = new URL(model.getDesmojLicenseURL());
        } catch (MalformedURLException e) {
            this.desmoJ_licenseURL = null;
        }
    }

    protected String getDesmoJ_Version() {
        return this.desmoJ_version;
    }

    protected String getDesmoJ_License() {
        return this.desmoJ_license;
    }

    protected URL getDesmoJ_LicenseURL() {
        return this.desmoJ_licenseURL;
    }


    /**
     * Falls desmoj.core.simulator.Experiment bekannt ist, werden die Methoden getDesmoJVersion und getDesmoJLicense
     * ausgelesen. Dieser Weg wird beschritten, da desmoj.extensions.visualization2d.engine auch unabhaengig vom
     * Rest-System funktionsfaehig sein soll, z.B. als Viewer-Applet.
     */
    private void init() {
        String version = "";
        String license = "";
        String licenseURL = "";
        try {
            Class<?> c = Class.forName("desmoj.core.simulator.Experiment");
            Method m1 = c.getMethod("getDesmoJVersion");
            version = (String) m1.invoke(null);
            Method m2 = c.getMethod("getDesmoJLicense", Boolean.TYPE);
            license = (String) m2.invoke(null, false);
            licenseURL = (String) m2.invoke(null, true);
            int first = licenseURL.indexOf('=') + 1;
            int last = licenseURL.indexOf('>');
            licenseURL = licenseURL.substring(first, last);
        } catch (Exception e) {
            version = "";
            license = "";
            licenseURL = "";
        }
        desmoJ_version = version;
        desmoJ_license = license;
        try {
            desmoJ_licenseURL = new URL(licenseURL);
        } catch (MalformedURLException e) {
            this.desmoJ_licenseURL = null;
        }
    }


}
