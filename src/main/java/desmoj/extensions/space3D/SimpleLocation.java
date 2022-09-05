package desmoj.extensions.space3D;

import desmoj.core.simulator.Model;

/**
 * This class represents a simple abstract location.
 *
 * @author Fred Sun
 */
public class SimpleLocation extends SpatialEntity {

    /**
     * Creates a SimpleLocation.
     *
     * @param owner       The model this location is associated to.
     * @param name        The name of the location.
     * @param type        The type attribute belongs to the SpatialObject interface.
     * @param showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in trace. Set
     *                    to false in entity should not be shown in trace.
     */
    public SimpleLocation(Model owner, String name, String type,
                          boolean showInTrace) {
        super(owner, name, type, showInTrace);
    }

    /**
     * Creates a SimpleLocation with specified position.
     *
     * @param owner          The model this location is associated to.
     * @param name           The name of the location.
     * @param type           The type attribute belongs to the SpatialObject interface.
     * @param showInTrace    Flag for showing entity in trace-files. Set it to true if entity should show up in trace.
     *                       Set to false in entity should not be shown in trace.
     * @param startPositionX The x start position in ExtendedLength.
     * @param startPositionY The y start position in ExtendedLength.
     * @param startPositionZ The z start position in ExtendedLength.
     */
    public SimpleLocation(Model owner, String name, String type,
                          boolean showInTrace, Length startPositionX,
                          Length startPositionY, Length startPositionZ) {
        super(owner, name, type, showInTrace, startPositionX, startPositionY,
            startPositionZ);
    }

}
