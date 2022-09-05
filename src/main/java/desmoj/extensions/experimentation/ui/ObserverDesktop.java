package desmoj.extensions.experimentation.ui;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import java.util.HashMap;

/**
 * Desktop for graphical observers in the experiment starter (a JDesktopPane).
 *
 * @author Nicolas Knaak
 * @author modified by Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class ObserverDesktop extends JDesktopPane implements
    GraphicalObserverContext {

    /** Automatic horizontal shift of window position */
    private static final int X_SHIFT = 25;

    /** Automatic vertical shift of window psoition */
    private static final int Y_SHIFT = 25;

    /** Graphical observers indexed by their name */
    private HashMap graphicalObservers = null;

    /** Horizontal position of next observer window to display */
    private int xOffset = 0;

    /** Vertical position of next observer window to display */
    private int yOffset = 0;

    /**
     * Adds a new graphical observer to the desktop
     *
     * @param o the new graphical observer
     */
    public void add(IGraphicalObserver o) {
        if (graphicalObservers == null) {
            graphicalObservers = new HashMap();
        }
        JInternalFrame frame = new JInternalFrame(o.getName(), true, false,
            true, true);
        frame.getContentPane().add(o.getGUI());
        graphicalObservers.put(o, frame);
        this.add(frame);
        frame.setLocation(xOffset, yOffset);
        xOffset += X_SHIFT;
        yOffset += Y_SHIFT;
    }

    /**
     * Removes graphical observer from the desktop
     *
     * @param o the graphical observer to remove
     */
    public void remove(IGraphicalObserver o) {
        JInternalFrame frame = getFrame(o);
        if (frame != null) {
            frame.setVisible(false);
            this.remove(frame);
            graphicalObservers.remove(o);
        }
    }

    /**
     * Sets the given graphical observer (in)visible
     *
     * @param o       a graphical observer registered with teh desktop
     * @param visible visibility flag (true = visible).
     */
    public void setVisible(IGraphicalObserver o, boolean visible) {
        JInternalFrame frame = getFrame(o);
        if (frame != null) {
            frame.setVisible(visible);
        }
    }

    /**
     * Returns an array of graphical observers registered with the desktop.
     *
     * @return registered observers as an array.
     */
    public IGraphicalObserver[] getChildren() {
        if (graphicalObservers == null) {
            return new IGraphicalObserver[0];
        }
        Object[] o = graphicalObservers.keySet().toArray();
        IGraphicalObserver[] children = new IGraphicalObserver[o.length];
        System.arraycopy(o, 0, children, 0, children.length);
        return children;
    }

    /**
     * Sets the size of a registered graphical observer's window
     *
     * @param o      the observer to change size
     * @param width  new window width
     * @param height new window height
     */
    public void setSize(IGraphicalObserver o, int width, int height) {
        JInternalFrame frame = getFrame(o);
        if (frame != null) {
            frame.setSize(width, height);
        }
    }

    /**
     * Sets the position of a registered graphical observer's window.
     *
     * @param o    the observer to change position
     * @param xLoc new horizontal position
     * @param yLoc new vertical position
     */
    public void setLocation(IGraphicalObserver o, int xLoc, int yLoc) {
        JInternalFrame frame = getFrame(o);
        if (frame != null) {
            frame.setLocation(xLoc, yLoc);
        }
    }

    /** Resets the position offset for the next window to zero. */
    public void resetOffset() {
        xOffset = 0;
        yOffset = 0;
    }

    /**
     * Updates the given graphical observer's display
     *
     * @param o A graphical observer registered with the desktop
     */
    public void update(IGraphicalObserver o) {

        //JInternalFrame f = (JInternalFrame) graphicalObservers.get(o);
        //if (f != null) {
        //}
        //
        // Fragmentary code? Inserted comments since no effect (JG, 11.03.09)
    }

    /**
     * Returns the internal frame representing the given observer.
     *
     * @param o a registered observer.
     * @return a JInternalFrame
     */
    private JInternalFrame getFrame(IGraphicalObserver o) {
        if (graphicalObservers == null) {
            return null;
        } else {
            return (JInternalFrame) graphicalObservers.get(o);
        }
    }
}