package desmoj.extensions.experimentation.util;

/**
 * A simple class representing filenames. This class is used in the experiment starter to distinguish strings from
 * filenames which are edited using a file chooser widget.
 *
 * @author Nicolas Knaak
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */

public class Filename {

    private final String name;

    private final boolean isDir;

    /**
     * Creates a new Filename object
     *
     * @param name  a valid path or file name
     * @param isDir this flag indicates if the filename specifies a directory
     */
    public Filename(String name, boolean isDir) {
        this.name = name;
        this.isDir = isDir;
    }

    /**
     * Creates a new Filename that does not refer to a directory.
     *
     * @param name a valid path or file name
     */
    public Filename(String name) {
        this(name, false);
    }

    /** @return the filename as a string */
    public String getName() {
        return name;
    }

    /** @return true iff this filename specifies a directory */
    public boolean isDirectory() {
        return isDir;
    }

    /** @return the filename as a string */
    public String toString() {
        return name;
    }
}