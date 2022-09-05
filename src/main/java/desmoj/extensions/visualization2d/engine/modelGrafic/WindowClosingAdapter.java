package desmoj.extensions.visualization2d.engine.modelGrafic;

/**
 * Adapter to close a Frame
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
public class WindowClosingAdapter extends java.awt.event.WindowAdapter {
    private final boolean exitSystem;

    public WindowClosingAdapter(boolean exit) {
        this.exitSystem = exit;
    }

    public WindowClosingAdapter() {
        this.exitSystem = false;
    }

    public void windowClosing(java.awt.event.WindowEvent e) {
        e.getWindow().setVisible(false);
        e.getWindow().dispose();
        if (this.exitSystem) {
            System.exit(0);
        }
    }

}
