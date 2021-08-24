package cambio.simulator.nparsing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;
import desmoj.core.simulator.Model;

/**
 * @param <T> Type of the pattern
 * @author Lion Wagner
 */
public class PatternCreator<T> implements InstanceCreator<T> {
    private final Model model;
    private final String name;
    private final Class<T> targetClass;

    private PatternCreator(Model model, String name,
                           Class<T> targetClass) {
        this.model = model;
        this.name = name;
        this.targetClass = targetClass;
    }

    public static <I> PatternCreator<I> getCreator(
        Model model, String name, Class<I> targetClass) {
        return new PatternCreator<>(model, name, targetClass);
    }

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
