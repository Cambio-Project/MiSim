package cambio.simulator.parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import cambio.simulator.entities.patterns.Pattern;

/**
 * Annotation used to mark fields that will be injected when parsing Patterns.
 *
 * @see Pattern#initFields(Map)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FromJson {
}
