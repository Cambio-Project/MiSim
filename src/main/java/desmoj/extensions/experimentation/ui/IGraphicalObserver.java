package desmoj.extensions.experimentation.ui;

import java.awt.Component;

public interface IGraphicalObserver {

    /**
     * Returns the context this observer is displayed in
     *
     * @return The context (e.g. a JDesktop component).
     */
    GraphicalObserverContext getContext();

    /**
     * Should return the graphical observer's main GUI component
     *
     * @return an AWT or Swing component.
     */
    Component getGUI();

    /** Registers the observer with the context */
    void register();

    /** Deregisters the observer from the context */
    void deregister();

    /** Sets the observer visible withing the context */
    void setVisible(boolean visible);

    /**
     * Sets the observer's main window's size.
     *
     * @param width  window width
     * @param height window height
     */
    void setSize(int width, int height);

    /**
     * Sets the position of the observer's main window's upper left edge.
     *
     * @param x horizontal position
     * @param y vertical position
     */
    void setLocation(int x, int y);

    /**
     * Returns the observer's name
     *
     * @return name
     */
    String getName();

    /**
     * Requests an update of the observer's display from the context.
     */
    void update();

}