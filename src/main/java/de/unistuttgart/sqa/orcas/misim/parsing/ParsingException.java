package de.unistuttgart.sqa.orcas.misim.parsing;

/**
 * Exception thrown when the parsers encounter a problem.
 *
 * @author Lion Wagner
 */
public class ParsingException extends RuntimeException {
    public ParsingException() {
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }

    public ParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
