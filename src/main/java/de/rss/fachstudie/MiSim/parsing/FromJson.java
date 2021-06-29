package de.rss.fachstudie.MiSim.parsing;

import java.lang.annotation.*;
import java.util.Map;

/**
 * Annotation used to mark fields that will be injected when parsing Patterns
 *
 * @see de.rss.fachstudie.MiSim.entities.patterns.Pattern#initFields(Map)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FromJson {
}
