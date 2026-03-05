package edu.kit.kastel.area;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkiArea {

    private final Map<String, AreaNode> nodes = new HashMap<>();
    private final Map<AreaNode, List<AreaNode>> adjacency = new HashMap<>();

    public void addNode(AreaNode node) {
        nodes.put(node.getId(), node);
        adjacency.put(node, new ArrayList<>());
    }

    public AreaNode getNode(String id) {
        return nodes.get(id);
    }

    public void addEdge(AreaNode from, AreaNode to) {
        List<AreaNode> list = adjacency.get(from);
        if (list != null) {
            list.add(to);
        }
    }

    public List<AreaNode> getSuccessors(AreaNode node) {
        List<AreaNode> list = adjacency.get(node);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public List<Lift> getAllLifts() {
        List<Lift> result = new ArrayList<>();
        for (AreaNode node : nodes.values()) {
            if (node instanceof Lift) {
                result.add((Lift) node);
            }
        }
        return result;
    }

    public List<Slope> getAllSlopes() {
        List<Slope> result = new ArrayList<>();
        for (AreaNode node : nodes.values()) {
            if (node instanceof Slope) {
                result.add((Slope) node);
            }
        }
        return result;
    }

    public Map<String, AreaNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }
}

