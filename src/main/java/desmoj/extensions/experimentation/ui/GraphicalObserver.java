package desmoj.extensions.experimentation.ui;

import java.awt.Component;

import desmoj.core.util.SimRunListener;

/**
 * Base class for graphical observers in the experiment launcher. A graphical observer can display itself inside a
 * graphical observer context (e.g. a JDesktop component).
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
public abstract class GraphicalObserver implements SimRunListener, IGraphicalObserver {

    /** Context to display this graphical observer in */
    private final GraphicalObserverContext context;

    /** The graphical observer's name */
    private final String name;

    /**
     * Creates a new graphical observer.
     *
     * @param name Name of the graphical observer
     * @param c    the context this graphical observer is displayed in
     */
    public GraphicalObserver(String name, GraphicalObserverContext c) {
        super();
        this.name = name;
        this.context = c;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#getContext()
     */
    public GraphicalObserverContext getContext() {
        return context;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#getGUI()
     */
    public abstract Component getGUI();

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#register()
     */
    public void register() {
        context.add(this);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#deregister()
     */
    public void deregister() {
        context.remove(this);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        context.setVisible(this, visible);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#setSize(int, int)
     */
    public void setSize(int width, int height) {
        context.setSize(this, width, height);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#setLocation(int, int)
     */
    public void setLocation(int x, int y) {
        context.setLocation(this, x, y);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.experimentation.ui.IGraphicalObserver#update()
     */
    public void update() {
        context.update(this);
    }
}