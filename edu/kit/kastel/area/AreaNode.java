package edu.kit.kastel.area;

/**
 * Common abstraction for all nodes in a {@link SkiArea}.
 * <p>
 * Each node has a unique identifier that is used to reference it in the area
 * definition and during route planning.
 *
 * @author usylb
 */
public interface AreaNode {

    /**
     * Returns the unique identifier of this node.
     *
     * @return the node identifier
     */
    String getId();
}

