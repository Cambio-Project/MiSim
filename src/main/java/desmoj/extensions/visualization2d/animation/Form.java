package desmoj.extensions.visualization2d.animation;

import java.awt.Dimension;


/**
 * Describes the form of a simple animation object, such as statistic object
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
public class Form {

    private Dimension deltaSize;

    /**
     * standard constructor
     *
     * @param deltaSize changes default size of animation object [pixel]
     */
    public Form(Dimension deltaSize) {
        this.deltaSize = deltaSize;
        if (this.deltaSize == null) {
            this.deltaSize = new Dimension(0, 0);
        }
    }

    /**
     * standard constructor
     *
     * @param w width of deltaSize
     * @param h height of deltaSize
     */
    public Form(int w, int h) {
        this(new Dimension(w, h));
    }

    /**
     * standard constructor generate deltaSize = new Dimension(0,0)
     */
    public Form() {
        this(null);
    }

    public Dimension getDeltaSize() {
        return this.deltaSize;
    }
}
