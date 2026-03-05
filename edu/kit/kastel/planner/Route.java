package edu.kit.kastel.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.kit.kastel.area.AreaNode;

public class Route {

    private final List<AreaNode> nodes;
    private final List<Integer> times;
    private int currentIndex;

    public Route(List<AreaNode> nodes, List<Integer> times) {
        this.nodes = new ArrayList<>(nodes);
        this.times = new ArrayList<>(times);
        this.currentIndex = 0;
    }

    /**
     * Route with an initial index (e.g. to represent "already taken" prefix).
     */
    public Route(List<AreaNode> nodes, List<Integer> times, int startIndex) {
        this.nodes = new ArrayList<>(nodes);
        this.times = new ArrayList<>(times);
        this.currentIndex = Math.max(0, Math.min(startIndex, nodes.size()));
    }

    public boolean isFinished() {
        return currentIndex >= nodes.size();
    }

    public AreaNode getNextNode() {
        if (isFinished()) {
            return null;
        }
        return nodes.get(currentIndex);
    }

    public void advance() {
        if (!isFinished()) {
            currentIndex++;
        }
    }

    public List<AreaNode> getRemainingNodes() {
        if (isFinished()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(nodes.subList(currentIndex, nodes.size()));
    }

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

    public AreaNode getCurrentNode() {
        if (currentIndex == 0 || currentIndex > nodes.size()) {
            return null;
        }
        return nodes.get(currentIndex - 1);
    }

    /** Time when we arrived at the current node (for alternative planning start). */
    public int getTimeAtCurrentPosition() {
        if (currentIndex <= 0 || times.isEmpty()) {
            return 0;
        }
        if (currentIndex <= times.size()) {
            return times.get(currentIndex - 1);
        }
        return times.get(times.size() - 1);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<AreaNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Integer> getTimes() {
        return Collections.unmodifiableList(times);
    }
}

