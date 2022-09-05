package desmoj.extensions.experimentation.util;

/**
 * A simple FileFilter for .xsl files
 *
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @author Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class XSLFilter extends FileFilter {

    /**
     * Accepts all directories and xsl files.
     *
     * @param file
     *            file: file to be filtered
     */
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        if (ext != null) {
            return ext.equals("xsl");
        }
        return false;
    }

    /** @return The description of this filter */
    public String getDescription() {
        return "*.xsl";
    }
}