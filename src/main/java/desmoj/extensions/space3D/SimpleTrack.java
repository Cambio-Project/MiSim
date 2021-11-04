package desmoj.extensions.space3D;

import org.scijava.vecmath.Vector3d;
import java.util.ArrayList;

import desmoj.core.simulator.Model;

/**
 * A SimpleTrack contains a start position and a destiny position. The route between them are modeled through way
 * points. Each point (incl. the start point) is connected with the successor point through a straight line. By
 * following the connected way points from the start point the destiny will be reached at the end.
 *
 * @author Fred Sun
 */
public class SimpleTrack extends Track {

    //the way points of this track
    private ArrayList<double[]> _wayPoints = null;

    /**
     * Constructs a SimpleTreck object with the start-, the destination position and the way points.</br></br> Each
     * element of the double[] ArrayList will be interpreted as a way point. So the size of the ArrayList must exactly
     * match the number of the way points needed.
     *
     * @param owner         The model this entity is associated to.
     * @param trackName     The name of this Track.
     * @param startPosition The start position of the Track. It must be a ExtendedLength[] with a length of 3.
     * @param destination   The end position of the Track. It must be a ExtendedLength[] with a length of 3.
     * @param wayPoints     The way points (double[] with length of 3) which describes the path between the start and
     *                      the destination.
     * @param showInTrace   Flag for showing entity in trace-files. Set it to true if entity should show up in trace.
     *                      Set to false in entity should not be shown in trace.
     */
    public SimpleTrack(Model owner, String trackName,
                       Length[] startPosition, Length[] destination,
                       ArrayList<double[]> wayPoints, boolean showInTrace) {
        super(owner, trackName, null, null, null, null,
            startPosition, destination, showInTrace);
        _wayPoints = wayPoints;
    }

    /**
     * Constructs a SimpleTrack with the start- and the destination positions. The way points can be used to describe
     * the path between the start and the destination. The name of the start location, the destination location, the
     * entry point and the exit point are optional and describe where the start- and the destination position are
     * associated to.</br></br> Each element of the double[] ArrayList will be interpreted as a way point. So the size
     * of the ArrayList must exactly match the number of the way points needed.
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
     * @param wayPoints               The way points (double[] with length of 3) which describes the path between the
     *                                start and the destination.
     * @param showInTrace             Flag for showing entity in trace-files. Set it to true if entity should show up in
     *                                trace. Set to false in entity should not be shown in trace.
     */
    public SimpleTrack(Model owner, String trackName, String startLocationName,
                       String destinationLocationName, String exitPointName,
                       String entryPointName, Length[] startPosition,
                       Length[] destiny, ArrayList<double[]> wayPoints,
                       boolean showInTrace) {
        super(owner, trackName, startLocationName, destinationLocationName,
            exitPointName, entryPointName, startPosition,
            destiny, showInTrace);

        _wayPoints = wayPoints;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.Track#getDirectionAt(double)
     */
    @Override
    public Vector3d getDirectionAt(double length) {
        //the result vector in double[3]
        double[] resultVec = new double[3];
        //start and the destiny positions
        double[] start = this.getStartPositionValue();
        double[] destiny = this.getDestinationValue();

        //number of the way points
        int numWayPoints = 0;
        if (_wayPoints != null) {
            numWayPoints = _wayPoints.size();
        }

        //if this track contains no way points, the vector points from the
        //start to the destiny
        if (_wayPoints == null || numWayPoints == 0) {
            resultVec[0] = destiny[0] - start[0];
            resultVec[1] = destiny[1] - start[1];
            resultVec[2] = destiny[2] - start[2];
        } else {
            //now, this track contains one or more way points
            //the distance between the start position and the first way point.
            double sectionLength =
                this.getDistanceBetweenTwoPoints(start,
                    _wayPoints.get(0));

            //for the case that it only has one way point
            if (numWayPoints == 1) {
                //if the length lies between the start position and the first way
                //point, save the vector which points from the start to the way point
                if (sectionLength > length) {
                    double[] headingWayPoint = _wayPoints.get(0);
                    resultVec[0] = headingWayPoint[0] - start[0];
                    resultVec[1] = headingWayPoint[1] - start[1];
                    resultVec[2] = headingWayPoint[2] - start[2];
                } else {
                    double[] lastWayPoint = _wayPoints.get(0);
                    resultVec[0] = destiny[0] - lastWayPoint[0];
                    resultVec[1] = destiny[1] - lastWayPoint[1];
                    resultVec[2] = destiny[2] - lastWayPoint[2];
                }
            }

            //for the case that this track has more than one way point
            if (numWayPoints > 1) {
                //check the length lies between each way points.
                int index = 0;
                while (index + 1 < numWayPoints && sectionLength <= length) {
                    index++;
                    sectionLength += this.getDistanceBetweenTwoPoints(_wayPoints.get(index - 1),
                        _wayPoints.get(index));
                }

                if (length == 0.0 || index == 0) {
                    //for the case that the length is 0
                    double[] headingWayPoint = _wayPoints.get(0);
                    resultVec[0] = headingWayPoint[0] - start[0];
                    resultVec[1] = headingWayPoint[1] - start[1];
                    resultVec[2] = headingWayPoint[2] - start[2];
                } else if (sectionLength > length) {
                    //if the moved length is exceeded, save the current vector.
                    double[] headingWayPoint = _wayPoints.get(index);
                    double[] lastWayPoint = _wayPoints.get(index - 1);
                    resultVec[0] = headingWayPoint[0] - lastWayPoint[0];
                    resultVec[1] = headingWayPoint[1] - lastWayPoint[1];
                    resultVec[2] = headingWayPoint[2] - lastWayPoint[2];
                } else {
                    //the length exceed the last way point. Take the vector from the
                    //last way point to the destination.
                    double[] lastWayPoint = _wayPoints.get(index);
                    resultVec[0] = destiny[0] - lastWayPoint[0];
                    resultVec[1] = destiny[1] - lastWayPoint[1];
                    resultVec[2] = destiny[2] - lastWayPoint[2];
                }
            }
        }

        //create the result vector.
        Vector3d result = new Vector3d(resultVec[0], resultVec[1], resultVec[2]);
        //normalize it
        result.normalize();

        return result;
    }

    /**
     * Gets the positive distance between 2 points.
     *
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The distance.
     */
    private double getDistanceBetweenTwoPoints(double[] point1, double[] point2) {
        assert (point1.length == 3 && point2.length == 3) : "The points must be a" +
            "double[] of length 3";

        double[] diffVec = new double[3];
        diffVec[0] = point1[0] - point2[0];
        diffVec[1] = point1[1] - point2[1];
        diffVec[2] = point1[2] - point2[2];

        return Math.sqrt((diffVec[0] * diffVec[0]) +
            (diffVec[1] * diffVec[1]) +
            (diffVec[2] * diffVec[2]));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.Track#getLength()
     */
    @Override
    public double getLength() {
        double[] startPosition = this.getStartPositionValue();
        double[] destinationPosition = this.getDestinationValue();

        //number of the way points
        int numWayPoints = 0;
        if (_wayPoints != null) {
            numWayPoints = _wayPoints.size();
        }

        if (_wayPoints == null || numWayPoints == 0) {
            //if there is no way points between the start and the destination
            //position, the length of the track is the distance from the
            //start position to the destination position.

            return this.getDistanceBetweenTwoPoints(startPosition,
                destinationPosition);
        } else {
            //if there are way points, calculate the length of each section
            //and return the sum.

            //the length between the start position and the first way point.
            double length = this.getDistanceBetweenTwoPoints(startPosition,
                _wayPoints.get(0));

            //if there are more than one way point, add the length between
            //each way points to it.
            for (int i = 1; i < numWayPoints; i++) {
                length += this.getDistanceBetweenTwoPoints(_wayPoints.get(i - 1),
                    _wayPoints.get(i));
            }

            //add the length between the last way point and the destination
            //position to it.
            length += this.getDistanceBetweenTwoPoints(_wayPoints.get(numWayPoints - 1),
                destinationPosition);
            return length;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.Track#getPositionAt(double)
     */
    @Override
    public Length[] getPositionAt(double length) {
        Length[] result = new Length[3];
        //get the position in double
        double[] positionValue = this.getPositionAtValue(length);
        //transform the double values into ExtendedLength
        result[0] = new Length(positionValue[0]);
        result[1] = new Length(positionValue[1]);
        result[2] = new Length(positionValue[2]);
        return result;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.Track#getPositionAt(double)
     */
    @Override
    public double[] getPositionAtValue(double length) {
        assert (length <= this.getLength()) : "The given length can't be greater" +
            "than the total length of this track.";

        double[] currentSectionVec = new double[3];
        //start and the destiny positions
        double[] start = this.getStartPositionValue();
        double[] destiny = this.getDestinationValue();


        //number of the way points
        int numWayPoints = 0;
        if (_wayPoints != null) {
            numWayPoints = _wayPoints.size();
        }

        //if there is no way points in this track
        if (_wayPoints == null || numWayPoints == 0) {
            //get the total length of the track
            double totalLength = this.getDistanceBetweenTwoPoints(start, destiny);

            //if the total length is greater than the given length
            if (totalLength > length) {
                //get the vector between the start and the destination
                currentSectionVec[0] = destiny[0] - start[0];
                currentSectionVec[1] = destiny[1] - start[1];
                currentSectionVec[2] = destiny[2] - start[2];

                //the ratio between the total length and the length already moved
                double lengthRatio = length / totalLength;

                //adjust the length of the currentSectionVec
                currentSectionVec[0] *= lengthRatio;
                currentSectionVec[1] *= lengthRatio;
                currentSectionVec[2] *= lengthRatio;

                //add the vector to the start position for getting the current
                //position
                currentSectionVec[0] += start[0];
                currentSectionVec[1] += start[1];
                currentSectionVec[2] += start[2];

                return currentSectionVec;
            }

            //for the case that the asked position is the destination position
            if (totalLength == length) {
                return destiny;
            }
        }

        //if there is only one way point in this track
        if (numWayPoints == 1) {
            //the length of the first section of the track
            double sectionLength = this.getDistanceBetweenTwoPoints(start,
                _wayPoints.get(0));

            //if the length lies in the first section of the track
            if (sectionLength > length) {
                double[] headingWayPoint = _wayPoints.get(0);
                currentSectionVec[0] = headingWayPoint[0] - start[0];
                currentSectionVec[1] = headingWayPoint[1] - start[1];
                currentSectionVec[2] = headingWayPoint[2] - start[2];

                //the length ratio
                double lengthRatio = length / sectionLength;

                //adjust the length of the currentSectionVec
                currentSectionVec[0] *= lengthRatio;
                currentSectionVec[1] *= lengthRatio;
                currentSectionVec[2] *= lengthRatio;

                //add the vector to the start position for getting the current
                //position
                currentSectionVec[0] += start[0];
                currentSectionVec[1] += start[1];
                currentSectionVec[2] += start[2];

                return currentSectionVec;
            }
            //for the case that the first way point is the position we want
            //to get
            if (sectionLength == length) {
                return _wayPoints.get(0);
            }

            //if the length is greater than the length of the first
            //section, subtract it.
            length -= sectionLength;

            //the length of the second section of the track
            sectionLength = this.getDistanceBetweenTwoPoints(_wayPoints.get(0),
                destiny);

            //the second section is longer than the given length
            if (sectionLength > length) {
                double[] lastWayPoint = _wayPoints.get(0);
                currentSectionVec[0] = destiny[0] - lastWayPoint[0];
                currentSectionVec[1] = destiny[1] - lastWayPoint[1];
                currentSectionVec[2] = destiny[2] - lastWayPoint[2];

                double lengthRatio = length / sectionLength;

                //adjust the length of the section vector
                currentSectionVec[0] *= lengthRatio;
                currentSectionVec[1] *= lengthRatio;
                currentSectionVec[2] *= lengthRatio;

                //add the last way point to get the absolute position
                currentSectionVec[0] += lastWayPoint[0];
                currentSectionVec[1] += lastWayPoint[1];
                currentSectionVec[2] += lastWayPoint[2];

                return currentSectionVec;
            }

            //for the case that the asked position is the destination position
            if (sectionLength == length) {
                return destiny;
            }

        }

        //excluded the cases numWayPoints<=1, this track must contain more
        //than one way point now.

        //initialize the variables
        double[] lastWayPoint = start;
        double[] headingWayPoint = _wayPoints.get(0);
        double sectionLength = this.getDistanceBetweenTwoPoints(start,
            headingWayPoint);

        //if the asked position does not lie in the first section
        if (sectionLength < length) {
            //find the section which exceeds the given length
            int index = 1;
            do {
                length -= sectionLength;
                lastWayPoint = _wayPoints.get(index - 1);
                headingWayPoint = _wayPoints.get(index);
                sectionLength = this.getDistanceBetweenTwoPoints(lastWayPoint,
                    headingWayPoint);
                //if one of the way point has the exact position we want to
                //get
                if (sectionLength == length) {
                    return headingWayPoint;
                }
                index++;
            } while (index < numWayPoints && sectionLength < length);

            //check the reason for the loop termination
            //if at the end of the loop the last section is still not
            //the section we want to get, then use the destination position
            //as the next heading way point.
            if (sectionLength < length) {
                length -= sectionLength;
                lastWayPoint = _wayPoints.get(index - 1);
                headingWayPoint = destiny;
            }//other wise the right section must be found
        }

        //for the case that the heading way point is the position we want to
        //get
        if (sectionLength == length) {
            return headingWayPoint;
        }

        //if the right section is found
        if (sectionLength > length) {
            //get the vector
            currentSectionVec[0] = headingWayPoint[0] - lastWayPoint[0];
            currentSectionVec[1] = headingWayPoint[1] - lastWayPoint[1];
            currentSectionVec[2] = headingWayPoint[2] - lastWayPoint[2];

            //get the length ratio
            double lengthRatio = length / sectionLength;

            //adjust the length of the vector
            currentSectionVec[0] *= lengthRatio;
            currentSectionVec[1] *= lengthRatio;
            currentSectionVec[2] *= lengthRatio;

            //get the absolute position
            currentSectionVec[0] += lastWayPoint[0];
            currentSectionVec[1] += lastWayPoint[1];
            currentSectionVec[2] += lastWayPoint[2];

            return currentSectionVec;
        }
        //otherwise, some case are not covered. return null
        return null;
    }

    /**
     * Gets the way points of this track.
     *
     * @return The way points in double[] of length 3. The first element represents the x-, the second element the y-
     *     and the third element the z-coordinate.
     */
    public ArrayList<double[]> getWayPoints() {
        return _wayPoints;
    }

}
