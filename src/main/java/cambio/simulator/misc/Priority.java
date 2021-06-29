package cambio.simulator.misc;

/**
 * Contains Constants to make prioritized scheduling easier to read. (Also good for maintainability, e.g. when using
 * 'Find Usage...')
 *
 * @author Lion Wagner
 */
public final class Priority {
    public static final int IMMEDIATELY_ON_TARGETED_TIME = Integer.MAX_VALUE;
    public static final int VERY_HIGH = Integer.MAX_VALUE / 2;
    public static final int HIGH = Integer.MAX_VALUE / 4;
    public static final int NORMAL = 0;
    public static final int LOW = -Integer.MAX_VALUE / 4;
    public static final int Very_LOW = -Integer.MAX_VALUE / 2;
}
