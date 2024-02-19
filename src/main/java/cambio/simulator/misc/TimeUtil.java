package cambio.simulator.misc;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;

public class TimeUtil {
    public static TimeSpan fromTimeInstant(final TimeInstant timeInstant){
        return new TimeSpan(timeInstant.getTimeInEpsilon(), TimeOperations.getEpsilon());
    }

    public static TimeInstant add(final TimeInstant timeInstant1, final TimeInstant timeInstant2){
        return TimeOperations.add(timeInstant1, fromTimeInstant(timeInstant2));
    }

    public static TimeInstant subtract(final TimeInstant timeInstant1, final TimeInstant timeInstant2){
        return TimeOperations.subtract(timeInstant1, fromTimeInstant(timeInstant2));
    }
}
