package cambio.simulator.entities.patterns;


/**
 * Interface to mark classes as acceptors of an {@link IStrategy}.
 *
 * @param <S> concrete type of the accepted {@link IStrategy}
 */
public interface IStrategyAcceptor<S extends IStrategy> {
    S getStrategy();

    void setStrategy(S strategy);
}
