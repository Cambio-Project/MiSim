package desmoj.extensions.visualization3d;

import desmoj.extensions.visualEvents.VisualEvent;

/**
 * This is the interface every visualization module should implement. It will be called from the VisualizationControl to
 * handle the incoming events.
 *
 * @author Fred Sun
 */
public interface VisualModule {

    /**
     * Gets the name of the VisualModule.
     *
     * @return The name of the VisualModule.
     */
    String getModuleName();

    /**
     * Handle the received event.
     *
     * @param event The event to be handled.
     */
    void handleEvent(VisualEvent event);

    /**
     * If a VisualizationControl is set, it will be removed from the module's local attribute by calling this method.
     * This method will be called by VisualizationControl at the removing of the module.
     */
    void removeVisualizationControl();

    /**
     * Saves the VisualizationControl this module as a local attribute. This method will be called by
     * VisualizationControl at the registration of the module.
     *
     * @param visCon The owner of this module
     */
    void setVisualizationControl(VisualizationControl visCon);
}
