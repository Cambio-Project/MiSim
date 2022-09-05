package desmoj.extensions.experimentation.ui;

/**
 * Represents an arbitrary context (e.g. a JDesktop) for graphical observers.
 *
 * @author Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public interface GraphicalObserverContext {

    /**
     * Should add a graphical observer to the context.
     *
     * @param o the graphical observer to add
     */
    void add(IGraphicalObserver o);

    /**
     * Should remove a graphical observer from the context.
     *
     * @param o the graphical observer to remove
     */
    void remove(IGraphicalObserver o);

    /**
     * Should set the given graphical observer (in)visible
     *
     * @param o       the graphical observer
     * @param visible ...to see or not to see... ;-)
     */
    void setVisible(IGraphicalObserver o, boolean visible);

    /**
     * Should returns all observers in this context.
     *
     * @return Graphical observer array
     */
    IGraphicalObserver[] getChildren();

    /**
     * Should set the given observer's window size
     *
     * @param o      a graphical observer
     * @param width  window width
     * @param height window height
     */
    void setSize(IGraphicalObserver o, int width, int height);

    /**
     * Should set the given observer's window position
     *
     * @param o a graphical observer
     * @param x horizontal coordinate of upper left edge
     * @param y vertical coordinate of upper left edge
     */
    void setLocation(IGraphicalObserver o, int x, int y);

    /**
     * Should update the given graphical observer's display
     *
     * @param graphical observer to update
     */
    void update(IGraphicalObserver o);
}