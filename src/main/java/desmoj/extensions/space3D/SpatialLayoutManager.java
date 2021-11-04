package desmoj.extensions.space3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import desmoj.core.simulator.ModelComponent;

/**
 * This class organizes the layout files and Track objects. This instance manages all the tracks in this model. Models
 * can be insert and removed during the simulation.
 *
 * @author Fred Sun
 */
public class SpatialLayoutManager {

    //a SpatialLayoutManager as singleton
    private static SpatialLayoutManager _manager = null;
    //saves the created SpatialObejcts
    private static HashMap<String, SpatialObject> _spatialObjectsMap;
    //a flag whether the generated track's flag showInTrace should be true
    //or false
    private boolean _traceGeneratedTracks = false;
    //a list to keep all the tracks
    private final ArrayList<Track> _trackList = new ArrayList<Track>();

    /**
     * Returns the singleton SpatialLayoutManager.
     *
     * @return The SpatialLayoutManager
     */
    public static SpatialLayoutManager getSpatialLayoutManager() {
        if (_manager == null) {
            _manager = new SpatialLayoutManager();
            _spatialObjectsMap = new HashMap<String, SpatialObject>();
        }
        return _manager;
    }

    /**
     * Register a new SpatialObject.
     *
     * @param name          The name of the SpatialObject.
     * @param spatialObject The SpatialObject to be registered.
     */
    public void addSpatialObject(String name, SpatialObject spatialObject) {
        if (spatialObject != null) {
            _spatialObjectsMap.put(name, spatialObject);
        }
    }

    /**
     * Register a new track.
     *
     * @param track The track to be registered.
     */
    public void addTrack(Track track) {
        if (track != null) {
            _trackList.add(track);
        }
    }

    /**
     * Returns the SpatialObject with the specified name.
     *
     * @param objectName The name of the SpatialObject to be get.
     * @return The SpatialObject with the specified name.
     */
    public SpatialObject getSpatialObject(String objectName) {
        return _spatialObjectsMap.get(objectName);
    }

    /**
     * Gets a registered track which leads the moving object to the destiny. If no appropriate track between them can be
     * found, null will be returned.</br> If the moving object isn't located at any SpatialObject, a SimpleTrack which
     * leads it directly to the destiny will be generated and returned.
     * </br></br>
     * The parameter destiny must be specified.
     *
     * @param mover       The object which should move to the destiny.
     * @param destination The destiny the mover should move to.
     * @return A track which lead the mover to the destiny. Null, if the mover is located at a SpatialObject and no
     *     track between it and the destiny is found.
     */
    public Track getTrack(MovableSpatialObject mover,
                          SpatialObject destination) {
        assert (destination != null) : "The destination must be specified.";

        SpatialObject startLocation = mover.getCurrentLocation();
        if (startLocation == null) {
            //if the mover isn't located in any location, straight to destiny
            Length[] moverPosition = new Length[3];
            moverPosition[0] = mover.getPosX();
            moverPosition[1] = mover.getPosY();
            moverPosition[2] = mover.getPosZ();

            Length[] destinationPosition = new Length[3];
            destinationPosition[0] = destination.getPosX();
            destinationPosition[1] = destination.getPosY();
            destinationPosition[2] = destination.getPosZ();

            return new SimpleTrack(((ModelComponent) mover).getModel(),
                "mover: " + mover.getName() + " to destination: " +
                    destination.getName(), moverPosition,
                destinationPosition, null, _traceGeneratedTracks);
        } else {

            //check for the start and the destiny names
            //the name need to be modified, because DESMO-J adds suffix automatically
            //to names
            String startLocationName = ((ModelComponent) startLocation).getName();
            int sharpPos1 = startLocationName.indexOf('#');
            if (sharpPos1 > 0) {
                startLocationName = startLocationName.substring(0, sharpPos1);
            }
            String destinationLocationName = ((ModelComponent) destination).getName();
            int sharpPos2 = destinationLocationName.indexOf('#');
            if (sharpPos2 > 0) {
                destinationLocationName = destinationLocationName.substring(0, sharpPos2);
            }

            //else, try to find a track which connects the start location
            //and the destiny
            Iterator<Track> it = _trackList.iterator();
            Track result = null;
            while (it.hasNext()) {
                result = it.next();
                if (result.getStartLocationName().equals(startLocationName) &&
                    result.getDestinyLocationName().equals(destinationLocationName)) {
                    //get the first result and exit
                    return result;
                }
            }
        }
        //if no track can be found, return null.
        //TODO Exception "No Track from "+mover+"'s position to "+destination+" can be found."
        return null;
    }

    /**
     * Gets the track base on the given start- and destination location. The specific entry and exit point are optional.
     * If it's given, they also will be considered as a search condition.
     *
     * @param startLocation       The start location.
     * @param exitPointName       The exit point at the start location.
     * @param destinationLocation The destination location.
     * @param entryPointName      The entry point at the destination location.
     * @return The track which matches the given parameter
     */
    public Track getTrack(SpatialObject startLocation, String exitPointName,
                          SpatialObject destinationLocation, String entryPointName) {
        //if the start and the destination location isn't specified, return null
        if (startLocation == null || destinationLocation == null) {
            return null;
        }

        //check whether the exit and entry points should be checked
        boolean exitPointTest = false;
        boolean entryPointTest = false;
        if (exitPointName != null) {
            exitPointTest = true;
        }
        if (entryPointName != null) {
            entryPointTest = true;
        }

        //generate condition variables for checking the exit and
        //entry points
        boolean exitPointMatch;
        boolean entryPointMatch;

        //check for the start and the destiny names
        //the name need to be modified, because DESMO-J adds suffix automatically
        //to names
        String startLocationName = ((ModelComponent) startLocation).getName();
        int sharpPos1 = startLocationName.indexOf('#');
        if (sharpPos1 > 0) {
            startLocationName = startLocationName.substring(0, sharpPos1);
        }
        String destinationLocationName = ((ModelComponent) destinationLocation).getName();
        int sharpPos2 = destinationLocationName.indexOf('#');
        if (sharpPos2 > 0) {
            destinationLocationName = destinationLocationName.substring(0, sharpPos2);
        }

        Iterator<Track> it = _trackList.iterator();
        Track result = null;
        while (it.hasNext()) {
            result = it.next();
            //check for the start, the destiny, the exit and the
            //entry point names
            if (result.getStartLocationName().equals(startLocationName) &&
                result.getDestinyLocationName().equals(destinationLocationName)) {

                //reset the condition variables
                exitPointMatch = true;
                entryPointMatch = true;

                //if the entry and the exit points should be tested, test them
                if (exitPointTest) {
                    exitPointMatch = result.getExitPointName().equals(exitPointName);
                }
                if (entryPointTest) {
                    entryPointMatch = result.getEnrtyPointName().equals(entryPointName);
                }
                if (exitPointMatch && entryPointMatch) {
                    //get the first result and exit
                    return result;
                }
            }
        }
        //no matched track found, return null.
        //TODO Exception "No Track from "+startLocationName+":"+exitPointName+" to "+destinationLocationName+":"+entryPointName+" can be found.")
        return null;
    }

    /**
     * Search through the registered tracks for a track with the given name. If none of the registered track has the
     * given name, null will be returned.
     *
     * @param name The name of the track which should be get.
     * @return The track with the specified name. Null, if there is none.
     */
    public Track getTrack(String name) {
        Iterator<Track> it = _trackList.iterator();
        Track result = null;
        while (it.hasNext()) {
            result = it.next();
            //check for the track name
            if (result.getName().equals(name)) {
                //get the first result and exit
                return result;
            }
        }
        //TODO Exception "No Track with the name "+name+" can be found."
        return null;
    }

    /**
     * Generates a SimpleTrack which leads directly to the given destination. If the mover object is currently at a
     * SpatialObject (current location), an exit point of this SpatialObject can be specified as start position of the
     * track.</br> Otherwise the position of the current location will be the start position.</br> If the mover isn't at
     * any SpatialObject, its current position will be the start position of the track.</br> The entry point of the
     * destination location can also be specified. If this parameter is set to null, the position of the destination
     * location will be used as the destination position of the generated SimpleTrack
     *
     * @param mover       The object which should move to the given position.
     * @param exitPoint   The specific exit point which should be the start position of the generated track.
     * @param destination The destination object the mover should steered directly into.
     * @param entryPoint  The specific entry point of the destination object.
     * @return A SimpleTrack which lead the mover object directly to the given destination.
     */
    public SimpleTrack getTrackStraightTo(MovableSpatialObject mover, String exitPoint,
                                          SpatialObject destination, String entryPoint) {
        Length[] moverPosition;
        SpatialObject currentLocation = mover.getCurrentLocation();
        //if the mover is at a location currently and a exit point is
        //specified, get the position of the exit point
        if (exitPoint != null && currentLocation != null) {
            moverPosition = currentLocation.getExitPointGlobal(exitPoint);
            //if the exit point can't be found, use the position of the
            //current location
            if (moverPosition == null) {
                ((ModelComponent) mover).sendWarning("The given exit point" +
                        "at the start location can't be found. The position" +
                        "of the start location will be use as the start position" +
                        "of the generated SimpleTrack.",
                    this + ": getTrackStraightTo " +
                        mover + ":" + exitPoint +
                        " to " + destination.toString() + ":" + entryPoint,
                    "The exit point: " + exitPoint + " can't be found at the current" +
                        "location of the mover.",
                    "Please check the code or the model.");
                moverPosition = new Length[3];
                moverPosition[0] = currentLocation.getPosX();
                moverPosition[1] = currentLocation.getPosY();
                moverPosition[2] = currentLocation.getPosZ();
            }
        } else if (currentLocation != null) {
            //if there is a current location but no exit point given, use the
            //position of the current location
            moverPosition = new Length[3];
            moverPosition[0] = currentLocation.getPosX();
            moverPosition[1] = currentLocation.getPosY();
            moverPosition[2] = currentLocation.getPosZ();
        } else {
            //if the mover isn't at a location, use its current position as
            //start position
            moverPosition = new Length[3];
            moverPosition[0] = mover.getPosX();
            moverPosition[1] = mover.getPosY();
            moverPosition[2] = mover.getPosZ();
        }

        Length[] destinationPosition;

        //if the entry point at the destination is specified, try to find
        //the position of it. Otherwise use the destination location as
        //the destination position.
        if (entryPoint != null) {
            destinationPosition = destination.getExitPointGlobal(entryPoint);
            if (destinationPosition == null) {
                ((ModelComponent) mover).sendWarning("The given entry point" +
                        "at the destination location can't be found. The position" +
                        "of the destination location will be use as the destiny position" +
                        "of the generated SimpleTrack.",
                    this + ": getTrackStraightTo " +
                        mover + ":" + exitPoint +
                        " to " + destination + ":" + entryPoint,
                    "The entry point: " + entryPoint + " can't be found at the destiny" +
                        "location.",
                    "Please check the code or the model");
                destinationPosition = new Length[3];
                destinationPosition[0] = destination.getPosX();
                destinationPosition[1] = destination.getPosY();
                destinationPosition[2] = destination.getPosZ();
            }
        } else {
            destinationPosition = new Length[3];
            destinationPosition[0] = destination.getPosX();
            destinationPosition[1] = destination.getPosY();
            destinationPosition[2] = destination.getPosZ();
        }

        return new SimpleTrack(((ModelComponent) mover).getModel(),
            "mover: " + mover.getName() + " to destination: " +
                destination,
            moverPosition,
            destinationPosition, null, _traceGeneratedTracks);
    }

    /**
     * Generates a SimpleTrack which leads directly to the given position. If the mover object is currently at a
     * SpatialObject (current location), an exit point of this SpatialObject can be specified as start position of the
     * track.</br> Otherwise the position of the current location will be the start position.</br> If the mover isn't at
     * any SpatialObject, its current position will be the start position of the track.
     *
     * @param mover     The object which should move to the given position.
     * @param exitPoint The specific exit point which should be the start position of the generated track.
     * @param x         The x-coordinate of the destiny position.
     * @param y         The y-coordinate of the destiny position.
     * @param z         The z-coordinate of the destiny position.
     * @return A SimpleTrack which lead the mover object directly to the given position.
     */
    public SimpleTrack getTrackStraightTo(MovableSpatialObject mover, String exitPoint,
                                          Length x, Length y, Length z) {

        Length[] moverPosition;
        SpatialObject currentLocation = mover.getCurrentLocation();
        //if the mover is at a location currently and a exit point is
        //specified, get the position of the exit point
        if (exitPoint != null && currentLocation != null) {
            moverPosition = currentLocation.getExitPointGlobal(exitPoint);
            //if the exit point can't be found, use the position of the
            //current location
            if (moverPosition == null) {
                ((ModelComponent) mover).sendWarning("The given exit point" +
                        "at the start location can't be found. The position" +
                        "of the start location will be use as the start position" +
                        "of the generated SimpleTrack.",
                    this + ": getTrackStraightTo " +
                        mover + ":" + exitPoint +
                        " to " + "(" + x + "," + y + "," + z + ")",
                    "The exit point: " + exitPoint + " can't be found at the current" +
                        "location of the mover.",
                    "Please check the code or the model.");
                moverPosition = new Length[3];
                moverPosition[0] = currentLocation.getPosX();
                moverPosition[1] = currentLocation.getPosY();
                moverPosition[2] = currentLocation.getPosZ();
            }
        } else if (currentLocation != null) {
            //if there is a current location but no exit point given, use the
            //position of the current location
            moverPosition = new Length[3];
            moverPosition[0] = currentLocation.getPosX();
            moverPosition[1] = currentLocation.getPosY();
            moverPosition[2] = currentLocation.getPosZ();
        } else {
            //if the mover isn't at a location, use its current position as
            //start position
            moverPosition = new Length[3];
            moverPosition[0] = mover.getPosX();
            moverPosition[1] = mover.getPosY();
            moverPosition[2] = mover.getPosZ();
        }
        Length[] destinationPosition = new Length[3];
        destinationPosition[0] = x;
        destinationPosition[1] = y;
        destinationPosition[2] = z;

        return new SimpleTrack(((ModelComponent) mover).getModel(),
            "mover: " + mover.getName() + " to destination: " +
                "X: " + x.toString() +
                " Y: " + y.toString() +
                " Z: " + z.toString(),
            moverPosition,
            destinationPosition, null, _traceGeneratedTracks);
    }

    /**
     * Get an iterator of all the tracks managed by this layout manager.
     *
     * @return An iterator of all the tracks registered.
     */
    public Iterator<Track> getTracksIterator() {
        return _trackList.iterator();
    }

    /**
     * Removes a specific track from the layout manager. If the same track exist more than once in the track list of the
     * manager, the first one will be removed.
     *
     * @param track The track to be removed.
     * @return True, if the layout manager contains the given track. False, if it doesn't.
     */
    public boolean removeTrack(Track track) {
        return _trackList.remove(track);

    }

    /**
     * Sets the flag, whether the tracks generated from this class should be traced or not. At constructing of these
     * tracks, the showInTrace flag will be set with this value.
     *
     * @param showInTrace The showInTrace flag which will be forwarded at to the constructor of the Track.
     */
    public void setTrace(boolean showInTrace) {
        _traceGeneratedTracks = showInTrace;
    }

    /**
     * Gets whether the showInTrace flag will be set at generating a new Track.
     *
     * @return True if the generated tracks will be traced. False, if not.
     */
    public boolean showTrace() {
        return _traceGeneratedTracks;
    }

}
