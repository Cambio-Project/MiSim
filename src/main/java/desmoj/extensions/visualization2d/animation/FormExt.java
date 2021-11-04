package desmoj.extensions.visualization2d.animation;

import java.awt.Dimension;


/**
 * Describes the form of a complex animation object, such as queue, process, ...
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
public class FormExt extends Form {

    private final boolean horizontal;
    private final int nrVisible;
    private final String defaultType;

    /**
     * constructor with full parameterization
     *
     * @param horizontal  horizontal when true, vertical otherwise
     * @param nrVisible   nr of entities always visible in animation object
     * @param defaultType defaultEntityType of entities in animation object
     * @param deltaSize   changes default size of animation object [pixel]
     */
    public FormExt(boolean horizontal, int nrVisible, String defaultType, Dimension deltaSize) {
        super(deltaSize);
        this.horizontal = horizontal;
        this.nrVisible = nrVisible;
        this.defaultType = defaultType;
    }

    /**
     * constructor with full parameterization
     *
     * @param horizontal  horizontal when true, vertical otherwise
     * @param nrVisible   nr of entities always visible in animation object
     * @param defaultType defaultEntityType of entities in animation object
     * @param width       width of deltaSize
     * @param height      height of deltaSize
     */
    public FormExt(boolean horizontal, int nrVisible, String defaultType, int width, int height) {
        this(horizontal, nrVisible, defaultType, new Dimension(width, height));
    }

    /**
     * constructor with deltsSize = new Dimension(0,0)
     *
     * @param horizontal  horizontal when true, vertical otherwise
     * @param nrVisible   nr of entities always visible in animation object
     * @param defaultType defaultEntityType of entities in animation object
     */
    public FormExt(boolean horizontal, int nrVisible, String defaultType) {
        this(horizontal, nrVisible, defaultType, null);
    }

    public boolean isHorizontal() {
        return this.horizontal;
    }

    public int getNrVisible() {
        return this.nrVisible;
    }

    public String getDefaultType() {
        return this.defaultType;
    }

}
