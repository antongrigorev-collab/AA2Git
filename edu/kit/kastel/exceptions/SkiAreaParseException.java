package edu.kit.kastel.exceptions;

/**
 * Indicates that a ski area definition could not be parsed or validated
 * successfully.
 * <p>
 * This exception is thrown when the Mermaid-based input format or the
 * structural constraints of a {@link edu.kit.kastel.area.SkiArea} are violated.
 *
 * @author usylb
 */
public class SkiAreaParseException extends Exception {

    /**
     * Creates a new ski area parse exception with the given message.
     *
     * @param message the detail message describing the parse error
     */
    public SkiAreaParseException(String message) {
        super(message);
    }
}

