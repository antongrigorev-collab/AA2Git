package edu.kit.kastel.area;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.exceptions.SkiAreaParseException;

public class SkiAreaParser {

    public SkiArea parse(String path) throws IOException, SkiAreaParseException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line == null || !line.trim().startsWith("graph")) {
                throw new SkiAreaParseException("Error, invalid area file");
            }

            Map<String, AreaNode> nodesById = new HashMap<>();
            List<String[]> edges = new ArrayList<>();

            String current;
            while ((current = reader.readLine()) != null) {
                String trimmed = current.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.contains("-->")) {
                    String[] parts = trimmed.split("-->");
                    if (parts.length != 2) {
                        throw new SkiAreaParseException("Error, invalid edge");
                    }
                    String fromId = parts[0].trim();
                    String toId = parts[1].trim();
                    edges.add(new String[] {fromId, toId});
                } else {
                    AreaNode node = parseNodeDefinition(trimmed);
                    if (nodesById.containsKey(node.getId())) {
                        throw new SkiAreaParseException("Error, duplicate id");
                    }
                    nodesById.put(node.getId(), node);
                }
            }

            SkiArea area = new SkiArea();
            for (AreaNode node : nodesById.values()) {
                area.addNode(node);
            }
            for (String[] edge : edges) {
                AreaNode from = nodesById.get(edge[0]);
                AreaNode to = nodesById.get(edge[1]);
                if (from == null || to == null) {
                    throw new SkiAreaParseException("Error, unknown id in edge");
                }
                area.addEdge(from, to);
            }

            validateArea(area);

            return area;
        }
    }

    private AreaNode parseNodeDefinition(String line) throws SkiAreaParseException {
        int idxBracket = line.indexOf('[');
        if (idxBracket < 0) {
            throw new SkiAreaParseException("Error, invalid node");
        }
        int idxParen = line.indexOf('(');
        int idEnd = idxBracket;
        if (idxParen >= 0 && idxParen < idEnd) {
            idEnd = idxParen;
        }
        String id = line.substring(0, idEnd).trim();
        if (line.contains("[[")) {
            return parseLift(line, id, true);
        }
        if (line.contains("(")) {
            return parseSlope(line, id);
        }
        return parseLift(line, id, false);
    }

    private Lift parseLift(String line, String id, boolean transit) throws SkiAreaParseException {
        int firstBracket = line.indexOf('[');
        int lastBracket = line.lastIndexOf(']');
        if (firstBracket < 0 || lastBracket < 0 || lastBracket <= firstBracket) {
            throw new SkiAreaParseException("Error, invalid lift");
        }
        String inner = line.substring(firstBracket + 1, lastBracket);
        if (transit && inner.startsWith("[") && inner.endsWith("]")) {
            inner = inner.substring(1, inner.length() - 1);
        }
        int brIndex = inner.indexOf("<br/>");
        if (brIndex < 0) {
            throw new SkiAreaParseException("Error, invalid lift content");
        }
        String data = inner.substring(brIndex + "<br/>".length());
        String[] parts = data.split(";");
        if (parts.length != 5) {
            throw new SkiAreaParseException("Error, invalid lift data");
        }
        LiftType type = LiftType.valueOf(parts[0].trim());
        int start = parseTime(parts[1].trim());
        int end = parseTime(parts[2].trim());
        int rideDuration = parsePositiveInt(parts[3].trim());
        int waitingTime = parsePositiveInt(parts[4].trim());
        return new Lift(id, type, start, end, rideDuration, waitingTime, transit);
    }

    private Slope parseSlope(String line, String id) throws SkiAreaParseException {
        int firstParen = line.indexOf('(');
        int lastParen = line.lastIndexOf(')');
        if (firstParen < 0 || lastParen < 0 || lastParen <= firstParen) {
            throw new SkiAreaParseException("Error, invalid slope");
        }
        String inner = line.substring(firstParen + 1, lastParen);
        if (inner.startsWith("[") && inner.endsWith("]")) {
            inner = inner.substring(1, inner.length() - 1);
        }
        int brIndex = inner.indexOf("<br/>");
        if (brIndex < 0) {
            throw new SkiAreaParseException("Error, invalid slope content");
        }
        String data = inner.substring(brIndex + "<br/>".length());
        String[] parts = data.split(";");
        if (parts.length != 4) {
            throw new SkiAreaParseException("Error, invalid slope data");
        }
        Difficulty difficulty = Difficulty.valueOf(parts[0].trim());
        Surface surface = Surface.valueOf(parts[1].trim());
        int length = parsePositiveInt(parts[2].trim());
        int height = parsePositiveInt(parts[3].trim());
        return new Slope(id, difficulty, surface, length, height);
    }

    private int parseTime(String value) throws SkiAreaParseException {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new SkiAreaParseException("Error, invalid time");
        }
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new SkiAreaParseException("Error, invalid time");
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new SkiAreaParseException("Error, invalid time");
        }
        return hour * 60 + minute;
    }

    private int parsePositiveInt(String value) throws SkiAreaParseException {
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                throw new SkiAreaParseException("Error, invalid integer");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new SkiAreaParseException("Error, invalid integer");
        }
    }

    private void validateArea(SkiArea area) throws SkiAreaParseException {
        if (area.getNodes().isEmpty()) {
            throw new SkiAreaParseException("Error, empty area");
        }

        boolean hasTransit = false;
        boolean hasSlope = false;
        for (AreaNode node : area.getNodes().values()) {
            if (node instanceof Lift && ((Lift) node).isTransit()) {
                hasTransit = true;
            }
            if (node instanceof Slope) {
                hasSlope = true;
            }
        }
        if (!hasTransit || !hasSlope) {
            throw new SkiAreaParseException("Error, invalid area structure");
        }

        ensureConnected(area);
    }

    private void ensureConnected(SkiArea area) throws SkiAreaParseException {
        Map<String, AreaNode> nodes = area.getNodes();
        if (nodes.isEmpty()) {
            return;
        }
        AreaNode start = nodes.values().iterator().next();
        Set<AreaNode> visited = new HashSet<>();
        List<AreaNode> stack = new ArrayList<>();
        stack.add(start);
        while (!stack.isEmpty()) {
            AreaNode current = stack.remove(stack.size() - 1);
            if (!visited.add(current)) {
                continue;
            }
            for (AreaNode successor : area.getSuccessors(current)) {
                if (!visited.contains(successor)) {
                    stack.add(successor);
                }
            }
        }
        if (visited.size() != nodes.size()) {
            throw new SkiAreaParseException("Error, area not connected");
        }
    }
}

