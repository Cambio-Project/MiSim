package cambio.simulator.parsing;

/**
 * Exception class to symbolize the absence of an {@link cambio.simulator.entities.patterns.IStrategy} class.
 *
 * @author Lion Wagner
 */
public class StrategyNotFoundException extends ClassNotFoundException {
    public StrategyNotFoundException() {
    }

    public StrategyNotFoundException(String s) {
        super(s);
    }

    public StrategyNotFoundException(String s, Throwable ex) {
        super(s, ex);
    }
}
