package desmoj.extensions.space3D;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * This class is a utility class for the calculations of time-spatial-relations of object movements.
 *
 * @author Fred Sun
 */
public final class KinematicsCalculations {


    //The static singleton KinematicsCalculations.
    //	private static KinematicsCalculations _kineCalc = null;

    //private KinematicsCalculations(){}

    /**
     * The static get method for the KinematicsCalculations.
     * @return The singleton KinematicsCalculations
     */
    //	public static KinematicsCalculations getKinematicsCalulations() {
    //		if(_kineCalc == null){
    //			_kineCalc = new KinematicsCalculations();
    //		}
    //		return _kineCalc;
    //	}

    /**
     * Configures the Movement object based on the track length, the start speed, the maximum speed, the acceleration,
     * the deceleration and a given total duration.
     *
     * @param movement         The movement to be configured.
     * @param totalDistance    The total length for the movement.
     * @param initialSpeed     The initial speed.
     * @param maxSpeed         The maximum speed.
     * @param acc              The acceleration.
     * @param dec              The deceleration.
     * @param totalSimDuration The given duration in SimTime.
     * @return The Movement object whose total distance and duration matches to the given distance and duration.
     */
    public static Movement configureMovement(Movement movement, double totalDistance, double initialSpeed,
                                             double maxSpeed,
                                             double acc, double dec, TimeSpan totalSimDuration) {
        if (totalDistance <= 0) {
            throw new IllegalArgumentException("The length of the movement must be greater than zero.");
        }
        if (totalSimDuration == null) {
            throw new IllegalArgumentException("A duration in SimTime must be given.");
        }
        //		assert(initialSpeed<=maxSpeed):"The start speed can't be greater than the maximum speed.";

        //the given total duration
        double totalDuration = totalSimDuration.getTimeAsDouble(TimeUnit.SECONDS);

        //gets a movement object which has the fastest movement to the destiny
        Movement fastestPossibleMovement = configureMovement(movement, totalDistance, initialSpeed, maxSpeed, acc, dec);


        //check whether it's possible to reach the destination in time.
        //If possible, start to test how to adjust the initial speed so the object can reach the destination in the exact given time
        //we just keep the initial and see where the movement will be ended
        if (fastestPossibleMovement.getTotalDuration().getTimeAsDouble(TimeUnit.SECONDS) < totalDuration) {
            //test whether the initialSpeed should be speeded down or speeded up.
            //the duration from initialSpeed to zero
            double breakingFromInitialSpeedToZeroDuration =
                getDurationOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec);
            //the duration when the movement is going on with the initial speed
            double initialSpeedDuration = totalDuration - breakingFromInitialSpeedToZeroDuration;
            //the distance which consist of the constant initialSpeed movement and break to zero with the given duration.
            double initialSpeedToDestinyDistance =
                (initialSpeed * initialSpeedDuration) + getDistanceOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec);


            //if we need to speed up
            if (initialSpeedToDestinyDistance < totalDistance) {
                double squared = (acc * acc * totalDuration * totalDuration * dec * dec)
                    + (2 * acc * dec * dec * initialSpeed * totalDuration)
                    + (acc * dec * initialSpeed * initialSpeed)
                    - (2 * acc * dec * dec * totalDistance)
                    + (2 * acc * acc * totalDistance * dec);

                if (squared < 0) {
                    throw new IllegalArgumentException(
                        "The squareroot of a negative value should be calculated. Please check the input");
                }

                double accToConstantSpeedDuration =
                    ((-acc * initialSpeed) - (acc * totalDuration * dec) - Math.sqrt(squared)) / (acc * (acc - dec));

                if (accToConstantSpeedDuration < 0) {
                    throw new IllegalArgumentException("The calculated acceleration time is negative. Check input.");
                }

                double breakFromConstantSpeedToZeroDuration =
                    (-(acc * accToConstantSpeedDuration + initialSpeed)) / dec;
                movement = new Movement(movement.getTrack(), accToConstantSpeedDuration,
                    totalDuration - accToConstantSpeedDuration - breakFromConstantSpeedToZeroDuration,
                    breakFromConstantSpeedToZeroDuration, initialSpeed, acc * accToConstantSpeedDuration + initialSpeed,
                    0.0d);
                //if we need to speed down
            } else if (initialSpeedToDestinyDistance > totalDistance) {
                double decToConstantSpeedDuration =
                    -((2 * initialSpeed * totalDuration * dec) + (initialSpeed * initialSpeed) -
                        (2 * totalDistance * dec)) / (2 * dec * (totalDuration * dec + initialSpeed));
                if (decToConstantSpeedDuration < 0) {
                    throw new IllegalArgumentException("The calculated acceleration time is negative. Check input.");
                }
                double breakFromConstantSpeedToZeroDuration =
                    (-(dec * decToConstantSpeedDuration + initialSpeed)) / dec;
                movement = new Movement(movement.getTrack(), decToConstantSpeedDuration,
                    totalDuration - decToConstantSpeedDuration - breakFromConstantSpeedToZeroDuration,
                    breakFromConstantSpeedToZeroDuration, initialSpeed, dec * decToConstantSpeedDuration + initialSpeed,
                    0.0d);
                //just for the case that we don't have to adjust the initial speed at all
            } else {
                movement = new Movement(movement.getTrack(), 0.0d, initialSpeedDuration,
                    breakingFromInitialSpeedToZeroDuration, initialSpeed, initialSpeed, 0.0d);
            }

            //check whether it's possible to reach the destination in time. If not, an exception will be thrown.
        } else if (fastestPossibleMovement.getTotalDuration().getTimeAsDouble(TimeUnit.SECONDS) > totalDuration) {
            throw new IllegalArgumentException("The given duration is too short to reach the destination in time.");
            //just for the case that the fastest possible movement is exactly what we want
        } else {
            movement = fastestPossibleMovement;
        }

        return movement;
    }

    /**
     * Configures the Movement object based on the track length, the start speed, the acceleration, the deceleration and
     * a given total duration. There's no maximum speed limitation.
     *
     * @param movement         The movement to be configured.
     * @param totalDistance    The total length for the movement.
     * @param initialSpeed     The initial speed.
     * @param acc              The acceleration.
     * @param dec              The deceleration.
     * @param totalSimDuration The given duration in SimTime.
     * @return The Movement object whose total distance and duration matches to the given distance and duration.
     */
    public static Movement configureMovement(Movement movement, double totalDistance, double initialSpeed, double acc,
                                             double dec, TimeSpan totalSimDuration) {

        if (totalDistance <= 0) {
            throw new IllegalArgumentException("The length of the movement must be greater than zero.");
        }
        if (totalSimDuration == null) {
            throw new IllegalArgumentException("A duration in SimTime must be given.");
        }

        //The distance needed to break from initialSpeed to zero.
        double breakToZeroDistance = getDistanceOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec);

        //If there is still room for more moving.
        if (breakToZeroDistance <= totalDistance) {

            //the given total duration
            double totalDuration = totalSimDuration.getTimeAsDouble(TimeUnit.SECONDS);

            //test whether the initialSpeed should be speeded down or speeded up.
            //the duration from initialSpeed to zero
            double breakingFromInitialSpeedToZeroDuration =
                getDurationOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec);
            //the duration when the movement is going on with the initial speed
            double initialSpeedDuration = totalDuration - breakingFromInitialSpeedToZeroDuration;
            //the distance which consist of the constant initialSpeed movement and break to zero with the given duration.
            double initialSpeedToDestinyDistance = (initialSpeed * initialSpeedDuration) + breakToZeroDistance;


            //if we need to speed up
            if (initialSpeedToDestinyDistance < totalDistance) {
                double squared = (acc * acc * totalDuration * totalDuration * dec * dec)
                    + (2 * acc * dec * dec * initialSpeed * totalDuration)
                    + (acc * dec * initialSpeed * initialSpeed)
                    - (2 * acc * dec * dec * totalDistance)
                    + (2 * acc * acc * totalDistance * dec);

                if (squared < 0) {
                    throw new IllegalArgumentException(
                        "The squareroot of a negative value should be calculated. Please check the input");
                }

                double accToConstantSpeedDuration =
                    ((-acc * initialSpeed) - (acc * totalDuration * dec) - Math.sqrt(squared)) / (acc * (acc - dec));

                if (accToConstantSpeedDuration < 0) {
                    throw new IllegalArgumentException("The calculated acceleration time is negative. Check input.");
                }

                double breakFromConstantSpeedToZeroDuration =
                    (-(acc * accToConstantSpeedDuration + initialSpeed)) / dec;
                movement = new Movement(movement.getTrack(), accToConstantSpeedDuration,
                    totalDuration - accToConstantSpeedDuration - breakFromConstantSpeedToZeroDuration,
                    breakFromConstantSpeedToZeroDuration, initialSpeed, acc * accToConstantSpeedDuration + initialSpeed,
                    0.0d);
                //if we need to speed down
            } else if (initialSpeedToDestinyDistance > totalDistance) {
                double decToConstantSpeedDuration =
                    -((2 * initialSpeed * totalDuration * dec) + (initialSpeed * initialSpeed) -
                        (2 * totalDistance * dec)) / (2 * dec * (totalDuration * dec + initialSpeed));
                if (decToConstantSpeedDuration < 0) {
                    throw new IllegalArgumentException("The calculated acceleration time is negative. Check input.");
                }
                double breakFromConstantSpeedToZeroDuration =
                    (-(dec * decToConstantSpeedDuration + initialSpeed)) / dec;
                movement = new Movement(movement.getTrack(), decToConstantSpeedDuration,
                    totalDuration - decToConstantSpeedDuration - breakFromConstantSpeedToZeroDuration,
                    breakFromConstantSpeedToZeroDuration, initialSpeed, dec * decToConstantSpeedDuration + initialSpeed,
                    0.0d);
                //just for the case that we don't have to adjust the initial speed at all
            } else {
                movement = new Movement(movement.getTrack(), 0.0d, initialSpeedDuration,
                    breakingFromInitialSpeedToZeroDuration, initialSpeed, initialSpeed, 0.0d);
            }
            //If the breaking distance longer than the distance to the destination an exception will be thrown.
        } else {
            throw new IllegalArgumentException(
                "The initial speed is too high. It's not possible to break to zero within the given distance.");
        }
        return movement;
    }

    /**
     * Configures the Movement object of an object's movement based on the track, the initial speed, the maximum speed,
     * the acceleration and the deceleration of the object. Its duration represents the shortest duration to the
     * destination possible (with the maximum speed as limitation).
     *
     * @param movement      The movement to be configured.
     * @param totalDistance The total length of the movement.
     * @param initialSpeed  The initial Speed.
     * @param maxSpeed      The maximum speed of the moving object.
     * @param acc           The acceleration of the moving object.
     * @param dec           The deceleration of the moving object. It's normally a negative value.
     * @return The Movement object whose total distance matches the given distance.
     */
    public static Movement configureMovement(Movement movement, double totalDistance, double initialSpeed,
                                             double maxSpeed, double acc, double dec) {

        if (totalDistance <= 0) {
            throw new IllegalArgumentException("The length of the movement must be greater than zero.");
        }

        //The distance needed to break from initialSpeed to zero.
        double breakToZeroDistance = getDistanceOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec);


        //If the breaking distance longer than the distance to the destination. There is still room for more moving.
        if (breakToZeroDistance < totalDistance) {


            //If the initialSpeed lies under the maximum speed, means an acceleration is needed at the beginning.
            if (initialSpeed < maxSpeed) {

                //get the maximum speed if the acceleration phase is just followed by the deceleration phase
                double maxAcceleratedSpeed = getMaxAcceleratedSpeed(totalDistance, initialSpeed, 0.0d, acc, dec);

                //if there's a phase where the speed is constant at the maximum speed
                //then moving duration is the sum of the duration of the acceleration to the maximum speed
                //and the duration with constant maximum speed
                //and the breaking duration from the maximum speed to 0.
                if (maxAcceleratedSpeed > maxSpeed) {
                    double accDistance = getDistanceOfSpeedBoundedAcceleration(initialSpeed, maxSpeed, acc);
                    double breakingDistance = getDistanceOfSpeedBoundedAcceleration(maxSpeed, 0.0d, dec);
                    double distanceWithMaxSpeed = totalDistance - accDistance - breakingDistance;

                    double accDuration = getDurationOfSpeedBoundedAcceleration(initialSpeed, maxSpeed, acc);
                    double breakingDuration = getDurationOfSpeedBoundedAcceleration(maxSpeed, 0.0d, dec);
                    double durationWithMaxSpeed = distanceWithMaxSpeed / maxSpeed;

                    movement = new Movement(movement.getTrack(), accDuration, durationWithMaxSpeed, breakingDuration,
                        initialSpeed, maxSpeed, 0.0d);

                    //if the acceleration phase is just followed by the deceleration phase after the maximum speed is reached
                    //then the moving duration is the sum of the duration of the acceleration to the maximum speed
                    //and the breaking duration from the maximum speed to 0.
                } else if (maxAcceleratedSpeed == maxSpeed) {
                    double accDuration = getDurationOfSpeedBoundedAcceleration(initialSpeed, maxSpeed, acc);
                    double breakingDuration = getDurationOfSpeedBoundedAcceleration(maxSpeed, 0.0d, dec);

                    movement =
                        new Movement(movement.getTrack(), accDuration, 0.0d, breakingDuration, initialSpeed, maxSpeed,
                            0.0d);

                    //if the distance isn't enough for the maximum speed to be reached
                    //then the moving duration is the sum of the duration of the acceleration to the reachable speed
                    //and the breaking duration from the reachable speed to 0.
                } else {
                    double accDuration = getDurationOfSpeedBoundedAcceleration(initialSpeed, maxAcceleratedSpeed, acc);
                    double breakingDuration = getDurationOfSpeedBoundedAcceleration(maxAcceleratedSpeed, 0.0d, dec);

                    movement = new Movement(movement.getTrack(), accDuration, 0.0d, breakingDuration, initialSpeed,
                        maxAcceleratedSpeed, 0.0d);
                }
                //If the initialSpeed lies over the maximum speed, means a break to the new maxSpeed is necessary at the beginning.
            } else {
                double distanceWithMaxSpeed = totalDistance - breakToZeroDistance;
                double durationWithMaxSpeed = distanceWithMaxSpeed / maxSpeed;
                movement = new Movement(movement.getTrack(),
                    getDurationOfSpeedBoundedAcceleration(initialSpeed, maxSpeed, dec), durationWithMaxSpeed,
                    getDurationOfSpeedBoundedAcceleration(maxSpeed, 0, dec), initialSpeed, maxSpeed, 0.0d);
            }
            //If the breaking distance longer than the distance to the destination an exception will be thrown.
        } else if (breakToZeroDistance > totalDistance) {
            throw new IllegalArgumentException(
                "The initial speed is too high. It's not possible to break to zero within the given distance.");
            //If the breaking distance is exactly the distance to the destination, it'll break to zero immediately.
        } else {
            movement = new Movement(movement.getTrack(), 0.0d, 0.0d,
                getDurationOfSpeedBoundedAcceleration(initialSpeed, 0.0d, dec), initialSpeed, 0.0d, 0.0d);
        }

        return movement;
    }


    /**
     * Gets the acceleration value based on the initial, the end speed and the acceleration duration.
     *
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @param duration     The duration of the acceleration.
     * @return The acceleration.
     */
    public static double getAcceleration(double initialSpeed, double endSpeed, TimeSpan duration) {
        double durationValue = duration.getTimeAsDouble(TimeUnit.SECONDS);
        if (durationValue < 0) {
            throw new IllegalArgumentException("The duration must be greater than 0.");
        }
        if (durationValue == 0) {
            throw new IllegalArgumentException("The duration can't be 0.");
        }
        return (endSpeed - initialSpeed) / durationValue;
    }

    /**
     * Gets the current speed based on the elapsed time and the movement object.
     *
     * @param movement        The movement within  the current speed should be calculated.
     * @param movingStartTime The SimTime, when the movement has started.
     * @param now             The current SimTime.
     * @return The current speed of the movement.
     */
    public static double getCurrentSpeed(Movement movement, TimeInstant movingStartTime, TimeInstant now) {

        //the duration how long the object has already moved
        double timeDelta = now.getTimeAsDouble(TimeUnit.SECONDS) - movingStartTime.getTimeAsDouble(TimeUnit.SECONDS);

        //if the elapsed time is in the acceleration phase of the movement
        if (timeDelta < movement.getAccDuration().getTimeAsDouble(TimeUnit.SECONDS)) {
            return getSpeedOfTimedAcceleration(timeDelta, movement.getInitialSpeed(),
                getAcceleration(movement.getInitialSpeed(), movement.getMaxSpeed(), movement.getAccDuration()));
            //if the elapsed time is in the maximum speed phase
        } else if (timeDelta <= (movement.getAccDuration().getTimeAsDouble(TimeUnit.SECONDS) +
            movement.getMaxSpeedDuration().getTimeAsDouble(TimeUnit.SECONDS))) {
            return movement.getMaxSpeed();
            //if the elapsed time is in the deceleration phase of the movement
        } else if (timeDelta < movement.getTotalDuration().getTimeAsDouble(TimeUnit.SECONDS)) {
            //the elapsed time in the deceleration phase
            double decelerationDuration = timeDelta - (movement.getAccDuration().getTimeAsDouble(TimeUnit.SECONDS) +
                movement.getMaxSpeedDuration().getTimeAsDouble(TimeUnit.SECONDS));
            return getSpeedOfTimedAcceleration(decelerationDuration, movement.getMaxSpeed(),
                getAcceleration(movement.getMaxSpeed(), movement.getEndSpeed(), movement.getDecDuration()));
            //if the moved time is exactly at the end of the movement
        } else if (timeDelta == movement.getTotalDuration().getTimeAsDouble(TimeUnit.SECONDS)) {
            return movement.getEndSpeed();
            //if the elapsed time is greater than the total movement duration, an exception will be thrown
        } else {
            throw new IllegalArgumentException("The duration can't be greater than the total movement duration.");
        }
    }

    /**
     * Gets the distance of the acceleration or deceleration from an initial speed to an end speed with a given
     * acceleration or deceleration value.
     *
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @param acc          The acceleration or deceleration.
     * @return The distance needed.
     */
    public static double getDistanceOfSpeedBoundedAcceleration(double initialSpeed, double endSpeed, double acc) {
        if ((endSpeed < initialSpeed && acc > 0.0d) || (endSpeed > initialSpeed && acc < 0.0d)) {
            throw new IllegalArgumentException(
                "The sign of the start and end speed don't match to the sign of the acceleration or the deceleration. The duration can't be calculated.");
        }

        if (acc == 0.0d) {
            throw new IllegalArgumentException("The acceleration can't be zero. The distance can't be calculated");
        } else {
            return (endSpeed * endSpeed - initialSpeed * initialSpeed) / (2 * acc);
        }

    }

    /**
     * Gets the distance moved based on the acceleration and the duration.
     *
     * @param duration     The duration of the movement.
     * @param initialSpeed The speed offset.
     * @param acc          The acceleration.
     * @return The distance moved with the specified duration after the duration.
     */
    public static double getDistanceOfTimedAcceleration(double duration, double initialSpeed, double acc) {
        if (initialSpeed < 0) {
            throw new IllegalArgumentException("The initial speed can't be negative.");
        }
        if (acc >= 0) {
            return (acc * duration * 0.5 * duration) + (initialSpeed * duration);
        } else {
            return Math.max(0.0d, (acc * duration * 0.5 * duration) + (initialSpeed * duration));
        }
    }

    /**
     * Gets the duration of a simple movement based of its moving distance, initial speed and the end speed.
     *
     * @param distance     The distance of the movement.
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @return The duration.
     */
    public static double getDurationOfDistanceBoundedMovement(double distance, double initialSpeed, double endSpeed) {
        if (distance < 0.0 || initialSpeed < 0.0 || endSpeed < 0.0) {
            throw new IllegalArgumentException("All the parameters have to be positive.");
        }
        if (initialSpeed + endSpeed == 0) {
            throw new IllegalArgumentException("The initial speed and the end speed can't be both zero.");
        }
        return (distance * 2) / (initialSpeed + endSpeed);
    }

    /**
     * Gets the duration of the acceleration or the deceleration from an initial speed to a end speed with a given
     * acceleration or deceleration value.
     *
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @param acc          the acceleration or deceleration value.
     * @return The duration needed.
     */
    public static double getDurationOfSpeedBoundedAcceleration(double initialSpeed, double endSpeed, double acc) {

        if ((endSpeed < initialSpeed && acc > 0.0d) || (endSpeed > initialSpeed && acc < 0.0d)) {
            throw new IllegalArgumentException(
                "The sign of the start and end speed don't match to the sign of the acceleration or the deceleration. The duration can't be calculated.");
        }

        if (acc == 0.0) {
            throw new IllegalArgumentException("The acceleration can't be zero. The duration can't be calculated");
        }

        if (initialSpeed == endSpeed) {
            return 0;
        } else {
            return (endSpeed - initialSpeed) / acc;
        }
    }

    /**
     * Gets the maximum speed if the movement only consist of an acceleration from a start speed followed by a
     * deceleration to the end speed for a certain distance.
     *
     * @param distance     The total distance of the movement.
     * @param initialSpeed The start speed.
     * @param endSpeed     The end speed.
     * @param acc          The acceleration.
     * @param dec          The deceleration. Normally a negative value.
     * @return The maximum speed can be reached.
     */
    public static double getMaxAcceleratedSpeed(double distance, double initialSpeed, double endSpeed, double acc,
                                                double dec) {

        if (acc == dec) {
            throw new IllegalArgumentException(
                "The acceleration can't have the same value as the deceleration. Normally they have different sign.");
        }
        double squaredSpeed =
            ((2 * acc * dec * distance) + (dec * initialSpeed * initialSpeed) - (acc * endSpeed * endSpeed)) /
                (dec - acc);
        if (squaredSpeed < 0) {
            throw new IllegalArgumentException(
                "The squareroot of a negative value should be calculated. Please check the input");
        } else {
            return Math.sqrt(squaredSpeed);
        }

    }

    /**
     * Gets the speed based on the acceleration, initial speed and the moved distance.
     *
     * @param distance     The distance which the movement shouldn't exceed.
     * @param initialSpeed The initial speed.
     * @param acc          The acceleration
     * @return The speed after the movement.
     */
    public static double getSpeedOfDistanceBoundedAcceleration(double distance, double initialSpeed, double acc) {
        if (distance < 0) {
            throw new IllegalArgumentException("The acceleration distance can't be negative.");
        }
        if (initialSpeed < 0) {
            throw new IllegalArgumentException("The initial speed can't be negative.");
        }
        double speedSqr = (initialSpeed * initialSpeed) + (2 * acc * distance);
        if (speedSqr < 0) {
            throw new IllegalArgumentException(
                "Invalid input. The squareroot of a negative value is tried to be calculated.");
        }
        return Math.sqrt(speedSqr);
    }

    /**
     * Gets the speed based on the acceleration and the duration.
     *
     * @param duration     The duration of the movement.
     * @param initialSpeed The speed offset.
     * @param acc          The acceleration.
     * @return The speed of the movement with the acceleration after the duration.
     */
    public static double getSpeedOfTimedAcceleration(double duration, double initialSpeed, double acc) {
        if (initialSpeed < 0) {
            throw new IllegalArgumentException("The initial speed can't be negative.");
        }
        return Math.max(0.0d, acc * duration + initialSpeed);
    }

}
