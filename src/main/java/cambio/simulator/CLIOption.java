package cambio.simulator;

import java.lang.annotation.*;

/**
 * Annotation to mark and configure a CLI option.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CLIOption {
    /**
     * Short option name. e.g. -h, -a, ...
     */
    String opt() default "";

    /**
     * Long option name. e.g. --help, --arch_desc, ...
     */
    String longOpt() default "";

    /**
     * The option description that is displayed from the help menu.
     */
    String description() default "";

    /**
     * Defines whether the CLI option is required.
     */
    boolean required() default false;


    /**
     * Defines whether the CLI option has an argument.
     */
    boolean hasArg() default false;


    /**
     * Name of the option-group this option belongs to.
     */
    String optionGroup() default "";

    /**
     * Whether the option-group is required. This is seen from the perspective of this option. If you are using the
     * default {@link CLI} class for parsing an option-group it is considered required if any of the options that
     * belongs to the group define the group as required.
     */
    boolean optionGroupRequired() default false;
}
