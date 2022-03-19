package cambio.simulator.parsing;

import java.lang.annotation.*;

/**
 * Annotation to give a type name to class that represents it in a MiSim architecture or experiment description.
 *
 * <p>
 * E.g.
 * <pre>
 *    {@literal @}JsonTypeName("retry")
 *    public class Retry extends ...
 * </pre>
 * names the class {@code Retry} "retry" so the parse will resolve the follwoing json to an object of the type {@code
 * Retry} and parses/injects the given config.
 *
 * <pre>
 *     {
 *         "type": "retry",
 *         "config:{
 *             ...
 *         }
 *     }
 * </pre>
 *
 * <p>
 * If two classes inherit from the same source they should have a distinct set of {@link JsonTypeName}s. Otherwise, one
 * will be ignored during parsing.
 *
 * @see JsonTypeNameResolver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonTypeName {

    /**
     * Normal type name of class that represents it in a MiSim architecture or experiment description.
     *
     * @return a string representing a type name of this class.
     */
    String value();


    /**
     * All alternative type names  of class that can represent it in a MiSim architecture or experiment description.
     *
     * @return all alternative type names of this class.
     */
    String[] alternativeNames() default {};
}
