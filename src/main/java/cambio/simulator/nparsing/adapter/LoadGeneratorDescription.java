package cambio.simulator.nparsing.adapter;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * @author Lion Wagner
 */
public abstract class LoadGeneratorDescription {
    public TimeSpan repetitionSkip;
    public TimeInstant startTime;
    private boolean repeating;

}
