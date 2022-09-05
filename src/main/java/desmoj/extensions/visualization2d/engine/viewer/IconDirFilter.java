package desmoj.extensions.visualization2d.engine.viewer;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * DirectoryFilter for Icon Directories, used in Icons -menu
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
public class IconDirFilter extends FileFilter {

    public boolean accept(File f) {
        boolean out = true;
        out |= f.isDirectory();
        return out;
    }

    public String getDescription() {
        return "Icon Directory";
    }

}
