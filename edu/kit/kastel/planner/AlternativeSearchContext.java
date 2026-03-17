package edu.kit.kastel.planner;

import edu.kit.kastel.area.AreaNode;

/**
 * Search context variant for alternative route planning.
 * <p>
 * In addition to the base search context, this context can suppress a specific
 * immediate successor from the start position to ensure the next step differs
 * from the originally suggested one.
 *
 * @author usylb
 */
final class AlternativeSearchContext extends SearchContext {

    private final String startId;
    private final String avoidNextId;

    /**
     * Creates a new alternative search context based on an existing base context.
     *
     * @param startId       id of the node where the alternative search starts
     * @param avoidNextId   id of the immediate next node that must be avoided
     * @param baseContext   the base context that provides the mutable search state
     */
    AlternativeSearchContext(String startId,
                             String avoidNextId,
                             SearchContext baseContext) {
        super(baseContext.endTime,
                baseContext.nodesSoFar,
                baseContext.timesSoFar,
                baseContext.slopesSoFar,
                baseContext.uniqueSlopes,
                baseContext.best);
        this.startId = startId;
        this.avoidNextId = avoidNextId;
    }

    /**
     * Checks whether the given successor must be skipped as the immediate next step.
     *
     * @param node       the currently expanded node
     * @param successor  the candidate successor node
     * @return {@code true} if {@code successor} must be avoided as the first step, otherwise {@code false}
     */
    boolean shouldAvoidImmediateNext(AreaNode node, AreaNode successor) {
        return node.getId().equals(startId) && nodesSoFar.isEmpty() && successor.getId().equals(avoidNextId);
    }
}

