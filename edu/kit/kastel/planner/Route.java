package edu.kit.kastel.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.kit.kastel.area.AreaNode;

/**
 * Represents a planned route through the ski area with timing information.
 * <p>
 * A route consists of an ordered list of {@link AreaNode} instances and the
 * corresponding times at which the skier reaches each node, together with an
 * index that marks the current position in the route.
 *
 * @author usylb
 */
public class Route {

    private final List<AreaNode> nodes;
    private final List<Integer> times;
    private int currentIndex;

    /**
     * Creates a new route starting at the first node.
     *
     * @param nodes the nodes that make up the route in execution order
     * @param times the times associated with the nodes, typically arrival times
     */
    public Route(List<AreaNode> nodes, List<Integer> times) {
        this.nodes = new ArrayList<>(nodes);
        this.times = new ArrayList<>(times);
        this.currentIndex = 0;
    }

    /**
     * Creates a route with an initial index (for example to represent an
     * already executed prefix of the route).
     *
     * @param nodes      the nodes that make up the route in execution order
     * @param times      the times associated with the nodes, typically
     *                   arrival times
     * @param startIndex the initial index in the route that should be
     *                   considered as the current position
     */
    public Route(List<AreaNode> nodes, List<Integer> times, int startIndex) {
        this.nodes = new ArrayList<>(nodes);
        this.times = new ArrayList<>(times);
        this.currentIndex = Math.max(0, Math.min(startIndex, nodes.size()));
    }

    /**
     * Checks whether all nodes of the route have already been processed.
     *
     * @return {@code true} if there is no next node, {@code false} otherwise
     */
    public boolean isFinished() {
        return currentIndex >= nodes.size();
    }

    /**
     * Returns the next node in the route relative to the current index.
     *
     * @return the next {@link AreaNode} or {@code null} if the route is
     *         already finished
     */
    public AreaNode getNextNode() {
        if (isFinished()) {
            return null;
        }
        return nodes.get(currentIndex);
    }

    /**
     * Advances the current index by one step if the route is not finished.
     */
    public void advance() {
        if (!isFinished()) {
            currentIndex++;
        }
    }

    /**
     * Returns an unmodifiable view of all remaining nodes of the route
     * starting at the current index.
     *
     * @return unmodifiable list of remaining nodes, or an empty list if the
     *         route is finished
     */
    public List<AreaNode> getRemainingNodes() {
        if (isFinished()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(nodes.subList(currentIndex, nodes.size()));
    }

    /**
     * Returns the time that corresponds to the current index.
     *
     * @return the current time in minutes, or a best-effort fallback if the
     *         index is out of range
     */
    public int getCurrentTime() {
        if (isFinished()) {
            if (times.isEmpty()) {
                return 0;
            }
            return times.get(times.size() - 1);
        }
        if (currentIndex < times.size()) {
            return times.get(currentIndex);
        }
        return times.isEmpty() ? 0 : times.get(times.size() - 1);
    }

    /**
     * Returns the node that has most recently been reached.
     *
     * @return the current {@link AreaNode}, or {@code null} if the route
     *         has not started yet or the index is out of range
     */
    public AreaNode getCurrentNode() {
        if (currentIndex == 0 || currentIndex > nodes.size()) {
            return null;
        }
        return nodes.get(currentIndex - 1);
    }

    /**
     * Returns the time when the current node was reached.
     * <p>
     * This value is used as the start time for alternative route planning.
     *
     * @return the arrival time at the current node in minutes, or a
     *         best-effort fallback if no such time is available
     */
    public int getTimeAtCurrentPosition() {
        if (currentIndex <= 0 || times.isEmpty()) {
            return 0;
        }
        if (currentIndex <= times.size()) {
            return times.get(currentIndex - 1);
        }
        return times.get(times.size() - 1);
    }

    /**
     * Returns the current index within the route.
     *
     * @return the current index, starting at {@code 0}
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Returns an unmodifiable view of all nodes in the route.
     *
     * @return unmodifiable list of all route nodes
     */
    public List<AreaNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Returns an unmodifiable view of all times associated with the route.
     *
     * @return unmodifiable list of all times in minutes
     */
    public List<Integer> getTimes() {
        return Collections.unmodifiableList(times);
    }
}

