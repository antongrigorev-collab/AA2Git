package edu.kit.kastel.planner;

import java.util.List;

import edu.kit.kastel.area.AreaNode;

/**
 * Stores the best route found during route search.
 * <p>
 * This is a simple mutable container that is updated whenever a better route is
 * discovered.
 *
 * @author usylb
 */
class BestRoute {

    List<AreaNode> nodes;
    List<Integer> times;
    int utility;
    int preferenceScore;
    String routeString;
}

