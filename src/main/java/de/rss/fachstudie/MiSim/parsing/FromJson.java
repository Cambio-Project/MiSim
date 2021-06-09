package de.rss.fachstudie.MiSim.parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * Annotation used to mark fields that will be injected when parsing Patterns.
 *
 * @see de.rss.fachstudie.MiSim.entities.patterns.Pattern#initFields(Map)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FromJson {
}
