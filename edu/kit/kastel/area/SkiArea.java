package edu.kit.kastel.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a directed graph of area nodes (lifts and slopes) that form a ski
 * area.
 * <p>
 * Nodes can be added and connected by directed edges. The class provides
 * access to successors and convenience methods for listing lifts and slopes.
 *
 * @author usylb
 */
public class SkiArea {

    private final Map<String, AreaNode> nodes = new HashMap<>();
    private final Map<AreaNode, List<AreaNode>> adjacency = new HashMap<>();

    /**
     * Adds a node to the ski area.
     *
     * @param node the node to add
     */
    public void addNode(AreaNode node) {
        nodes.put(node.getId(), node);
        adjacency.put(node, new ArrayList<>());
    }

    /**
     * Looks up a node by its id.
     *
     * @param id the node id
     * @return the node, or {@code null} if no such node exists
     */
    public AreaNode getNode(String id) {
        return nodes.get(id);
    }

    /**
     * Adds a directed edge from one node to another.
     *
     * @param from the start node
     * @param to   the target node
     */
    public void addEdge(AreaNode from, AreaNode to) {
        List<AreaNode> list = adjacency.get(from);
        if (list != null) {
            list.add(to);
        }
    }

    /**
     * Returns all direct successor nodes of the given node.
     *
     * @param node the node whose successors should be returned
     * @return an unmodifiable list of successors (may be empty)
     */
    public List<AreaNode> getSuccessors(AreaNode node) {
        List<AreaNode> list = adjacency.get(node);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a list of all lifts contained in this ski area.
     *
     * @return list of all lifts
     */
    public List<Lift> getAllLifts() {
        List<Lift> result = new ArrayList<>();
        for (AreaNode node : nodes.values()) {
            if (node instanceof Lift) {
                result.add((Lift) node);
            }
        }
        return result;
    }

    /**
     * Returns a list of all slopes contained in this ski area.
     *
     * @return list of all slopes
     */
    public List<Slope> getAllSlopes() {
        List<Slope> result = new ArrayList<>();
        for (AreaNode node : nodes.values()) {
            if (node instanceof Slope) {
                result.add((Slope) node);
            }
        }
        return result;
    }

    /**
     * Returns an unmodifiable view of all nodes by id.
     *
     * @return unmodifiable map of node id to node
     */
    public Map<String, AreaNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }
}

