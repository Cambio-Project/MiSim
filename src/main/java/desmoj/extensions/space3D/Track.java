package desmoj.extensions.space3D;

import org.scijava.vecmath.Vector3d;

import desmoj.core.exception.DESMOJException;
import desmoj.core.report.ErrorMessage;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;


/**
 * This is the basic class of the Track. All Tracks should inherit from this class.
 *
 * @author Fred Sun
 */
public abstract class Track extends ModelComponent {

    //The name of the start location
    private final String _startLocationName;

    //The name of the destination location
    private final String _destinationLocationName;

    //The name of the exit point
    private final String _exitPointName;

    //The name of the entry point
    private final String _entryPointName;

    /**
     * The start position of the Track. It should be a double[3] array.
     */
    private final double[] _startPosition;

    /**
     * The end position of the Track. It should be a double[3] array.
     */
    private final double[] _destination;

    /**
     * Constructs a Track and initialize the basic attributes. The name for the start- and the destiny location, the
     * entry- and exit points are optional. The start- and the destiny position must be specified.
     *
     * @param owner                   The model this entity is associated to.
     * @param trackName               The name of this Track.
     * @param startLocationName       The name of the start location.
     * @param destinationLocationName The name of the destiny location.
     * @param exitPointName           The name of the exit point.
     * @param entryPointName          The name of the entry point.
     * @param startPosition           The start position of the Track. It must be a ExtendedLength[] with a length of
     *                                3.
     * @param destination             The end position of the Track. It must be a ExtendedLength[] with a length of 3.
     * @param showInTrace             Flag for showing entity in trace-files. Set it to true if entity should show up in
     *                                trace. Set to false in entity should not be shown in trace.
     */
    public Track(Model owner, String trackName, String startLocationName,
                 String destinationLocationName, String exitPointName,
                 String entryPointName, Length[] startPosition,
                 Length[] destination, boolean showInTrace) {
        super(owner, trackName, showInTrace);

        if (startPosition != null && startPosition.length == 3) {
            _startPosition = new double[3];
            _startPosition[0] = startPosition[0].getValue();
            _startPosition[1] = startPosition[1].getValue();
            _startPosition[2] = startPosition[2].getValue();
        } else {
            throw new DESMOJException(new ErrorMessage(owner,
                "constructing a new Track", this +
                " Track(Model owner, String trackName, " +
                "String startLocationName, " +
                "String destinationLocationName, " +
                "String exitPointName, String entryPointName, " +
                "ExtendedLength[] startPosition, ExtendedLength[] destination, " +
                "boolean showInTrace)",
                "The start position isn't" +
                    " specified or the given position doesn't " +
                    " contain 3 values.",
                "Please recheck the code or the layout file",
                this.presentTime()));
        }

        if (destination != null && destination.length == 3) {
            _destination = new double[3];
            _destination[0] = destination[0].getValue();
            _destination[1] = destination[1].getValue();
            _destination[2] = destination[2].getValue();
        } else {
            throw new DESMOJException(new ErrorMessage(owner,
                "constructing a new Track", this +
                " Track(Model owner, String trackName, " +
                "String startLocationName, " +
                "String destinationLocationName, " +
                "String exitPointName, String entryPointName, " +
                "ExtendedLength[] startPosition, ExtendedLength[] destination, " +
                "boolean showInTrace)",
                "The destiny position doesn't" +
                    " contain 3 values.",
                "Please recheck the code or the layout file",
                this.presentTime()));
        }

        //		if(startLocationName.isEmpty() || destinationLocationName.isEmpty() ||
        //				startLocationName==null || destinationLocationName==null){
        //			throw new DESMOJException(new ErrorMessage(owner,
        //					"constructing a new Track", this.toString()+
        //					" Track(Model owner, String trackName," +
        //					"String startLocationName," +
        //					"String destinationLocationName, String exitPointName," +
        //					"String entryPointName, boolean showInTrace)",
        //					"The start and the destiny location must be specified",
        //					"Please recheck the code or the layout file",
        //					this.presentTime()));
        //		}

        _startLocationName = startLocationName;
        _destinationLocationName = destinationLocationName;
        _exitPointName = exitPointName;
        _entryPointName = entryPointName;
    }

    /**
     * Gets the name of the destiny location.
     *
     * @return The name of the destiny. Null, if it'n not specified.
     */
    public String getDestinyLocationName() {
        return _destinationLocationName;
    }

    /**
     * Gets the destiny position in ExtendedLength[]. The first element represents the x-position, the second the
     * y-position and the third the z-position.
     *
     * @return The destiny position in ExtendedLength.
     */
    public Length[] getDestination() {
        Length[] result = new Length[3];
        result[0] = new Length(_destination[0]);
        result[1] = new Length(_destination[1]);
        result[2] = new Length(_destination[2]);
        return result;
    }

    /**
     * Gives the end position of the Track.
     *
     * @return The end position in a double[3]. The value represents meters.
     */
    public double[] getDestinationValue() {
        return _destination;
    }

    /**
     * Gets the current moving direction based on the specified length from the start position on the track.
     *
     * @param length The length from the start position to the current position.
     * @return The normalized vector which points to the current moving direction.
     */
    public abstract Vector3d getDirectionAt(double length);

    /**
     * Gets the name of the entry point at the destiny location.
     *
     * @return The name of the entry point. Null, if it's not specified.
     */
    public String getEnrtyPointName() {
        return _entryPointName;
    }

    /**
     * Gets the name of the exit point at the start location.
     *
     * @return The name of the exit point. Null, if it's not specified.
     */
    public String getExitPointName() {
        return _exitPointName;
    }

    /**
     * Gives the total length of the Track.
     *
     * @return The length of the Track in a double value.
     */
    public abstract double getLength();

    /**
     * Gets the coordinates (in double) of the current position based on the specified length from the start position on
     * the track.
     *
     * @param length The length from the start position to the position which we want to know.
     * @return A double[3] array with the X, Y, and Z-coordinates. The value represents meters.
     */
    abstract double[] getPositionAtValue(double length);

    /**
     * Gets the coordinates (in ExtendedLength) of the current position based on the specified length from the start
     * position on the track.
     *
     * @param length The length from the start position to the position which we want to know.
     * @return A ExtendedLength[3] array which represents the x-, y- and the z-coordinates.
     */
    public abstract Length[] getPositionAt(double length);

    /**
     * Gets the name of the start location.
     *
     * @return The name of the start location. Null, if it's not specified.
     */
    public String getStartLocationName() {
        return _startLocationName;
    }

    /**
     * Gets the start position in ExtendedLength[]. The first element represents the x-position, the second the
     * y-position and the third the z-position.
     *
     * @return The start position in ExtendedLength.
     */
    public Length[] getStarLengthUnits() {
        Length[] result = new Length[3];
        result[0] = new Length(_startPosition[0]);
        result[1] = new Length(_startPosition[1]);
        result[2] = new Length(_startPosition[2]);
        return result;
    }

    /**
     * Gives the start Position of the Track.
     *
     * @return The start position in a double[3]. The value represents meters.
     */
    public double[] getStartPositionValue() {
        return _startPosition;
    }

    /**
     * Sets the start position of this track.
     *
     * @param position The position to be set. It must be a ExtendedLength of length 3.
     */
    public void setStartPosition(Length[] position) {
        if (position != null && position.length == 3) {
            _startPosition[0] = position[0].getValue();
            _startPosition[1] = position[1].getValue();
            _startPosition[2] = position[2].getValue();
        } else {
            throw new DESMOJException(new ErrorMessage(this.getModel(),
                "set the start position", this +
                " setStartPosition(ExtendedLength[] position)",
                "The start position isn't" +
                    " specified or the given position doesn't " +
                    " contain 3 values.",
                "Please recheck the code or the layout file",
                this.presentTime()));
        }
    }

    /**
     * Sets the destiny position of this track.
     *
     * @param position The position to be set. It must be a ExtendedLength of length 3.
     */
    public void setDestinyPosition(Length[] position) {
        if (position != null && position.length == 3) {
            _destination[0] = position[0].getValue();
            _destination[1] = position[1].getValue();
            _destination[2] = position[2].getValue();
        } else {
            throw new DESMOJException(new ErrorMessage(this.getModel(),
                "set the destiny position", this +
                " setDestinyPosition(ExtendedLength[] position)",
                "The destiny position doesn't" +
                    " contain 3 values.",
                "Please recheck the code or the layout file",
                this.presentTime()));
        }
    }

}
