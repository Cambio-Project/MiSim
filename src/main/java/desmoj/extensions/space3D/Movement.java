package desmoj.extensions.space3D;

import org.scijava.vecmath.Vector3d;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * The Movement class stores the track, the duration informations of each phase of a movement and its associated speed.
 * A movement will be divided into 3 phases, the acceleration phase, the maximum speed phase and the deceleration phase.
 * It's allowed to set the initial speed greater than the maximum speed. Then the acceleration phase will be used to
 * reduce the speed to the maximum speed.
 *
 * @author Fred Sun
 */
public class Movement {


    /**
     * The duration of the acceleration phase.
     */
    private double _accDuration;

    /**
     * The duration of the phase when the movement has the maximum speed.
     */
    private double _maxSpeedDuration;

    /**
     * The duration of the deceleration phase.
     */
    private double _decDuration;

    /**
     * The initial speed of the movement.
     */
    private double _initialSpeed;

    /**
     * The maximum speed of the movement.
     */
    private double _maxSpeed;

    /**
     * The end speed of the movement.
     */
    private double _endSpeed;

    /**
     * The Track of the movement.
     */
    private Track _track;

    /**
     * Constructs a Movement object with empty values.
     */
    public Movement() {
        _accDuration = 0.0;
        _maxSpeedDuration = 0.0;
        _decDuration = 0.0;
        _initialSpeed = 0.0;
        _maxSpeed = 0.0;
        _endSpeed = 0.0;
    }

    /**
     * It constructs a Movement object with given speed and duration of each phases.
     *
     * @param track            The track associated to this movement.
     * @param accDuration      The duration of the acceleration phase.
     * @param maxSpeedDuration The duration of the phase when the movement has the maximum speed.
     * @param decDuration      The duration of the deceleration phase.
     * @param initialSpeed     The initial speed.
     * @param maxSpeed         The maximum speed.
     * @param endSpeed         The end speed.
     */
    public Movement(Track track, TimeSpan accDuration, TimeSpan maxSpeedDuration,
                    TimeSpan decDuration, double initialSpeed, double maxSpeed,
                    double endSpeed) {
        _accDuration = accDuration.getTimeAsDouble(TimeUnit.SECONDS);
        _maxSpeedDuration = maxSpeedDuration.getTimeAsDouble(TimeUnit.SECONDS);
        _decDuration = decDuration.getTimeAsDouble(TimeUnit.SECONDS);
        _initialSpeed = initialSpeed;
        _maxSpeed = maxSpeed;
        _endSpeed = endSpeed;
        _track = track;
    }

    /**
     * It constructs a Movement object with given speed and duration of each phases. The durations are in double and
     * measured in the unit of the reference time.
     *
     * @param track            The track associated to this movement.
     * @param accDuration      The duration of the acceleration phase.
     * @param maxSpeedDuration The duration of the phase when the movement has the maximum speed.
     * @param decDuration      The duration of the deceleration phase.
     * @param initialSpeed     The initial speed.
     * @param maxSpeed         The maximum speed.
     * @param endSpeed         The end speed.
     */
    public Movement(Track track, double accDuration, double maxSpeedDuration,
                    double decDuration, double initialSpeed, double maxSpeed,
                    double endSpeed) {
        _accDuration = accDuration;
        _maxSpeedDuration = maxSpeedDuration;
        _decDuration = decDuration;
        _initialSpeed = initialSpeed;
        _maxSpeed = maxSpeed;
        _endSpeed = endSpeed;
        _track = track;
    }

    /**
     * Gets the duration of the acceleration phase.
     *
     * @return The duration of the acceleration phase.
     */
    public TimeSpan getAccDuration() {
        return new TimeSpan(_accDuration, TimeUnit.SECONDS);
    }

    /**
     * Sets the acceleration duration
     *
     * @param accDuration the acceleration duration to set
     */
    public void setAccDuration(TimeSpan accDuration) {
        _accDuration = accDuration.getTimeAsDouble(TimeUnit.SECONDS);
    }

    /**
     * Gets the moved distance based on the elapsed SimTime.
     *
     * @param movingStartTime The SimTime, when the movement has started.
     * @param now             The current SimTime.
     * @return The moved distance in ExtendedLength.
     */
    public Length getCurrentMovedDistance(TimeInstant movingStartTime, TimeInstant now) {
        if (!this.isValid()) {
            throw new IllegalArgumentException("The movement object isn't valid.");
        }
        return new Length(this.getMovedLength(movingStartTime, now));
    }

    /**
     * Gets the current moving direction on this movement based on the elapsed SimTime.
     *
     * @param movingStartTime The SimTime, when the movement has started.
     * @param now             The current SimTime.
     * @return The current direction in double[3].
     */
    public Vector3d getCurrentMovingDirection(TimeInstant movingStartTime, TimeInstant now) {
        if (!this.isValid()) {
            throw new IllegalArgumentException("The movement object isn't valid.");
        }
        if ((_accDuration + _maxSpeedDuration + _decDuration) - (now.getTimeAsDouble(TimeUnit.SECONDS) -
            movingStartTime.getTimeAsDouble(TimeUnit.SECONDS)) < 0.0001) {
            //if moved time is the total duration, full length of the track has been moved
            return _track.getDirectionAt(_track.getLength());
        } else {
            return _track.getDirectionAt(this.getMovedLength(movingStartTime, now));
        }
    }

    /**
     * Gets the current position on this movement based on elapsed SimTime.
     *
     * @param movingStartTime The SimTime, when the movement has started.
     * @param now             The current SimTime.
     * @return The current position in double[3].
     */
    public double[] getCurrentPosition(TimeInstant movingStartTime, TimeInstant now) {
        if (!this.isValid()) {
            throw new IllegalArgumentException("The movement object isn't valid.");
        }
        return _track.getPositionAtValue(this.getMovedLength(movingStartTime, now));
    }

    /**
     * Gets the duration of the deceleration phase.
     *
     * @return The duration of the deceleration phase.
     */
    public TimeSpan getDecDuration() {
        return new TimeSpan(_decDuration, TimeUnit.SECONDS);
    }

    /**
     * Sets the deceleration duration.
     *
     * @param decDuration the deceleration duration to set
     */
    public void setDecDuration(TimeSpan decDuration) {
        _decDuration = decDuration.getTimeAsDouble(TimeUnit.SECONDS);
    }

    /**
     * Gets the end speed.
     *
     * @return the end speed
     */
    public double getEndSpeed() {
        return _endSpeed;
    }

    /**
     * Sets the end speed.
     *
     * @param endSpeed the end speed to set
     */
    public void setEndSpeed(double endSpeed) {
        _endSpeed = endSpeed;
    }

    /**
     * Gets the initial speed.
     *
     * @return the initial speed
     */
    public double getInitialSpeed() {
        return _initialSpeed;
    }

    /**
     * Sets the initial speed.
     *
     * @param initialSpeed the initial speed to set
     */
    public void setInitialSpeed(double initialSpeed) {
        _initialSpeed = initialSpeed;
    }

    /**
     * Gets the maximum speed.
     *
     * @return the maximum speed
     */
    public double getMaxSpeed() {
        return _maxSpeed;
    }

    /**
     * Sets the maximum speed.
     *
     * @param maxSpeed the maximum speed to set
     */
    public void setMaxSpeed(double maxSpeed) {
        _maxSpeed = maxSpeed;
    }

    /**
     * Gets the duration when the movement has the maximum speed.
     *
     * @return The duration when the movement has the maximum speed.
     */
    public TimeSpan getMaxSpeedDuration() {
        return new TimeSpan(_maxSpeedDuration, TimeUnit.SECONDS);
    }

    /**
     * Sets the duration of the maximum speed.
     *
     * @param maxSpeedDuration the duration with the maximum speed to set
     */
    public void setMaxSpeedDuration(TimeSpan maxSpeedDuration) {
        _maxSpeedDuration = maxSpeedDuration.getTimeAsDouble(TimeUnit.SECONDS);
    }

    //The the moved length
    private double getMovedLength(TimeInstant movingStartTime, TimeInstant now) {
        //the length already moved will be stored here
        double length = 0.0;
        //the duration how long the object has already moved
        //		double timeDelta = now.getTimeValue() - movingStartTime.getTimeValue();
        double timeDelta = now.getTimeAsDouble(TimeUnit.SECONDS) - movingStartTime.getTimeAsDouble(TimeUnit.SECONDS);

        //if the elapsed time is in the acceleration phase of the movement
        if (timeDelta < _accDuration) {
            length = KinematicsCalculations.getDistanceOfTimedAcceleration(timeDelta, _initialSpeed,
                KinematicsCalculations.getAcceleration(_initialSpeed, _maxSpeed,
                    new TimeSpan(_accDuration, TimeUnit.SECONDS)));
            //if the elapsed time is in the maximum speed phase
        } else if (timeDelta <= _accDuration + _maxSpeedDuration) {
            //length in the acceleration phase
            if (timeDelta == 0.0) {
                length = 0.0;
            } else if (_accDuration > 0) {
                length = KinematicsCalculations.getDistanceOfTimedAcceleration(_accDuration, _initialSpeed,
                    KinematicsCalculations.getAcceleration(_initialSpeed, _maxSpeed,
                        new TimeSpan(_accDuration, TimeUnit.SECONDS)));
            }
            //plus the moved distance in the maximum speed phase
            length += (timeDelta - _accDuration) * _maxSpeed;
            //if the elapsed time is in the deceleration phase of the movement
        } else if (timeDelta <= _accDuration + _maxSpeedDuration + _decDuration) {
            //store the values which we're going to use more often
            double initialSpeed = _initialSpeed;
            double accDuration = _accDuration;
            double maxSpeed = _maxSpeed;
            double maxSpeedDuration = _maxSpeedDuration;
            //the elapsed time in the deceleration phase
            double decelerationDuration = timeDelta - accDuration - maxSpeedDuration;
            //length in the acceleration phase
            length = KinematicsCalculations.getDistanceOfTimedAcceleration(accDuration, initialSpeed,
                KinematicsCalculations.getAcceleration(initialSpeed, maxSpeed,
                    new TimeSpan(accDuration, TimeUnit.SECONDS)));
            //plus the length in the maximum speed phase
            length += maxSpeedDuration * maxSpeed;
            //plus the moved distance in the deceleration phase
            length += KinematicsCalculations.getDistanceOfTimedAcceleration(decelerationDuration, maxSpeed,
                KinematicsCalculations.getAcceleration(maxSpeed, _endSpeed,
                    new TimeSpan(_decDuration, TimeUnit.SECONDS)));
            //if the elapsed time is greater than the total movement duration, an exception will be thrown
        } else {
            throw new IllegalArgumentException("The duration can't be greater than the total movement duration.");
        }
        return length;
    }

    /**
     * Gets the total duration which is the sum of all the 3 phases.
     *
     * @return The total duration.
     */
    public TimeSpan getTotalDuration() {
        return new TimeSpan(_accDuration + _maxSpeedDuration + _decDuration, TimeUnit.SECONDS);
    }

    /**
     * Gets the track object of this movement.
     *
     * @return the track object of this movement.
     */
    public Track getTrack() {
        return _track;
    }

    /**
     * Sets the track for the movement.
     *
     * @param track track to be set.
     */
    public void setTrack(Track track) {
        _track = track;
    }

    /**
     * Tests if the movement is valid and fully specified.
     *
     * @return true if: The Track object is instantiated and if neither the track length, the total duration and the sum
     *     of the speed in each phase is zero. If one of these condition is zero, then all of them should be zero.
     *     Otherwise false will be returned.
     */
    public boolean isValid() {
        if (_track == null) {
            return false;
        } else if (_track.getLength() == 0.0 || _accDuration + _maxSpeedDuration + _decDuration == 0.0 ||
            _initialSpeed + _maxSpeed + _endSpeed == 0.0) {
            return _track.getLength() == 0.0 && _accDuration + _maxSpeedDuration + _decDuration == 0.0 &&
                _initialSpeed + _maxSpeed + _endSpeed == 0.0;
        }
        return true;
    }

}
