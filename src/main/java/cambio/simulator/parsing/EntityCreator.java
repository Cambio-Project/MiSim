package cambio.simulator.parsing;

import java.lang.reflect.*;
import java.util.Objects;

import com.google.gson.InstanceCreator;
import desmoj.core.simulator.Model;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Extention of Gsons' {@link InstanceCreator} to create {@link desmoj.core.simulator.Entity} instances of Desmo-J
 * objects. Due to type erasure and type saftey the public factory {@link EntityCreator#getCreator(Model, String,
 * Class)} hast to be used to create instances of this class.
 *
 * <p>
 * Expects the target type to have a constructor with the parameters types {@code (Model.class, String.class,
 * boolean.class)}. Otherwise, no instance can be created.
 *
 * @param <T> Type of the pattern
 * @author Lion Wagner
 * @see EntityCreator#getCreator(Model, String, Class)
 */
public class EntityCreator<T> implements InstanceCreator<T> {
    private final Model model;
    private final String name;
    private final Class<T> targetClass;

    private EntityCreator(Model model, String name,
                          Class<T> targetClass) {
        this.model = model;
        this.name = name;
        this.targetClass = targetClass;
    }

    /**
     * Creates a new {@link EntityCreator} that will create an object of type {@code I}, if {@code I} has a constructor
     * that matches  {@code (Model.class, String.class, boolean.class)} arguments.
     *
     * @param model       model that is to be assigned
     * @param name        name that is to be assigned
     * @param targetClass concrete class instance of {@code I} for type safety
     * @param <I>         target type
     * @return a new {@link EntityCreator}, that potentially creates an object of type {@code I}.
     */
    @Contract("null,_,_ -> fail; _,_,null-> fail")
    public static <I> @NotNull EntityCreator<I> getCreator(
        Model model, String name, Class<I> targetClass) {
        Objects.requireNonNull(model);
        Objects.requireNonNull(targetClass);
        return new EntityCreator<>(model, name, targetClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T createInstance(Type type) {
        try {
            Constructor<T> c =
                targetClass.getDeclaredConstructor(Model.class, String.class, boolean.class);
            return c.newInstance(model, name, true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                String.format(
                    "Cannot create an instance of %s. Missing constructor of with parameters (%s, %s, %s)",
                    targetClass.getName(), Model.class, String.class, Boolean.class),
                e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(
                String.format(
                    "Cannot create an instance of %s. Executing the constructor failed",
                    targetClass.getName()), e);
        }
    }

}
