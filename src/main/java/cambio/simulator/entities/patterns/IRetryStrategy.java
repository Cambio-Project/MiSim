package cambio.simulator.entities.patterns;


public interface IRetryStrategy extends IStrategy {

    double getNextDelay(int tries);

}
