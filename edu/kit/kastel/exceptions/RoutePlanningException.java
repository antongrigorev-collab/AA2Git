package edu.kit.kastel.exceptions;

/**
 * Signals problems that occur while planning a route in a ski area.
 * <p>
 * Typical reasons include invalid input data or the absence of any feasible
 * route within the given time window.
 *
 * @author usylb
 */
public class RoutePlanningException extends Exception {

    /**
     * Creates a new route planning exception with the given message.
     *
     * @param message the detail message describing the failure
     */
    public RoutePlanningException(String message) {
        super(message);
    }
}

