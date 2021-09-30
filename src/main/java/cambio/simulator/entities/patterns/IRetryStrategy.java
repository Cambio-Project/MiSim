package cambio.simulator.entities.patterns;


/**
 * Interface that represnts a strategy of a {@link Retry}.
 *
 * @see LinearBackoffRetryStrategy
 * @see ExponentialBackoffRetryStrategy
 */
public interface IRetryStrategy extends IStrategy {

    double getNextDelay(int tries);

}
