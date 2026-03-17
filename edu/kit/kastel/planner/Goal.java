package edu.kit.kastel.planner;

/**
 * Represents the optimization goal for a planned ski route.
 * <p>
 * Each goal defines which utility measure should be maximized when searching
 * for a route in the ski area.
 *
 * @author usylb
 */
public enum Goal {

    /**
     * Maximizes the total altitude difference of all slopes in the route.
     */
    ALTITUDE,

    /**
     * Maximizes the total distance (length) of all slopes in the route.
     */
    DISTANCE,

    /**
     * Maximizes the total number of slope rides, counting repetitions.
     */
    NUMBER,

    /**
     * Maximizes the number of distinct slopes used in the route.
     */
    UNIQUE
}

