package de.rss.fachstudie.MiSim.parsing;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FromJson {
}
