package edu.kit.kastel.planner;

import java.util.List;
import java.util.Set;

import edu.kit.kastel.area.AreaNode;
import edu.kit.kastel.area.Slope;

/**
 * Bundles mutable search state for the depth-first route exploration.
 * <p>
 * The context holds the time limit, the currently explored path and a shared
 * container for the best route found so far.
 *
 * @author usylb
 */
class SearchContext {

    final int endTime;
    final List<AreaNode> nodesSoFar;
    final List<Integer> timesSoFar;
    final List<Slope> slopesSoFar;
    final Set<Slope> uniqueSlopes;
    final BestRoute best;

    /**
     * Creates a new search context instance.
     *
     * @param endTime       the end time (in minutes) that must not be exceeded
     * @param nodesSoFar    the nodes of the current partial route
     * @param timesSoFar    the arrival times (in minutes) for {@code nodesSoFar}
     * @param slopesSoFar   the slopes that have been taken so far (in order)
     * @param uniqueSlopes  the set of slopes that have been taken at least once
     * @param best          shared container holding the best route found so far
     */
    SearchContext(int endTime,
                  List<AreaNode> nodesSoFar,
                  List<Integer> timesSoFar,
                  List<Slope> slopesSoFar,
                  Set<Slope> uniqueSlopes,
                  BestRoute best) {
        this.endTime = endTime;
        this.nodesSoFar = nodesSoFar;
        this.timesSoFar = timesSoFar;
        this.slopesSoFar = slopesSoFar;
        this.uniqueSlopes = uniqueSlopes;
        this.best = best;
    }
}

