package cambio.simulator.entities.patterns;

public interface IStrategyAcceptor<S extends IStrategy> {
    S getStrategy();

    void setStrategy(S strategy);
}
