package edu.kit.kastel.app;

import edu.kit.kastel.area.AreaNode;
import edu.kit.kastel.area.Difficulty;
import edu.kit.kastel.area.SkiArea;
import edu.kit.kastel.area.SkiAreaParser;
import edu.kit.kastel.area.Surface;
import edu.kit.kastel.exceptions.RoutePlanningException;
import edu.kit.kastel.exceptions.SkiAreaParseException;
import edu.kit.kastel.planner.Goal;
import edu.kit.kastel.planner.Route;
import edu.kit.kastel.planner.RoutePlanner;
import edu.kit.kastel.user.Skill;
import edu.kit.kastel.user.SkierContext;

public class CommandProcessor {

    private boolean running = true;
    private SkiArea currentArea;
    private final SkierContext skierContext = new SkierContext();
    private Route currentRoute;
    private String pendingNextId;
    private Integer planEndTime;

    public boolean processLine(String line) {
        if (line == null) {
            return running;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return running;
        }

        if ("quit".equals(trimmed)) {
            running = false;
            return false;
        }

        if (trimmed.startsWith("load area ")) {
            handleLoadArea(trimmed.substring("load area ".length()).trim());
            return running;
        }

        if (trimmed.startsWith("plan ")) {
            handlePlan(trimmed.substring("plan ".length()).trim());
            return running;
        }

        if ("next".equals(trimmed)) {
            handleNext();
            return running;
        }

        if ("take".equals(trimmed)) {
            handleTake();
            return running;
        }

        if ("show route".equals(trimmed)) {
            handleShowRoute();
            return running;
        }

        if ("abort".equals(trimmed)) {
            handleAbort();
            return running;
        }

        if ("alternative".equals(trimmed)) {
            handleAlternative();
            return running;
        }

        if (trimmed.startsWith("set skill ")) {
            handleSetSkill(trimmed.substring("set skill ".length()).trim());
            return running;
        }

        if (trimmed.startsWith("set goal ")) {
            handleSetGoal(trimmed.substring("set goal ".length()).trim());
            return running;
        }

        if (trimmed.startsWith("like ")) {
            handleLike(trimmed.substring("like ".length()).trim());
            return running;
        }

        if (trimmed.startsWith("dislike ")) {
            handleDislike(trimmed.substring("dislike ".length()).trim());
            return running;
        }

        if ("reset preferences".equals(trimmed)) {
            skierContext.getPreferences().reset();
            return running;
        }

        System.out.println("Error, unsupported command");
        return running;
    }

    private void handleLoadArea(String path) {
        if (path.isEmpty()) {
            System.out.println("Error, missing path");
            return;
        }
        currentRoute = null;
        pendingNextId = null;
        planEndTime = null;
        try {
            java.io.BufferedReader reader =
                    new java.io.BufferedReader(new java.io.FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
        } catch (java.io.IOException e) {
            System.out.println("Error, cannot read file");
            return;
        }

        SkiAreaParser parser = new SkiAreaParser();
        try {
            SkiArea parsed = parser.parse(path);
            currentArea = parsed;
        } catch (java.io.IOException e) {
            System.out.println("Error, cannot read file");
        } catch (SkiAreaParseException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handlePlan(String args) {
        if (currentArea == null) {
            System.out.println("Error, no area loaded");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length != 3) {
            System.out.println("Error, invalid plan arguments");
            return;
        }
        String startId = parts[0];
        String startTimeString = parts[1];
        String endTimeString = parts[2];
        int startMinutes;
        int endMinutes;
        try {
            startMinutes = parseTime(startTimeString);
            endMinutes = parseTime(endTimeString);
        } catch (IllegalArgumentException e) {
            System.out.println("Error, invalid time");
            return;
        }
        RoutePlanner planner = new RoutePlanner(currentArea, skierContext);
        try {
            currentRoute = planner.planRoute(startId, startMinutes, endMinutes);
            pendingNextId = null;
            planEndTime = endMinutes;
            System.out.println("route planned");
        } catch (RoutePlanningException e) {
            System.out.println(e.getMessage());
        }
    }

    private int parseTime(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("invalid time");
        }
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("invalid time");
        }
        return hour * 60 + minute;
    }

    private void handleNext() {
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        if (currentRoute.isFinished()) {
            System.out.println("route finished!");
            pendingNextId = null;
            return;
        }
        AreaNode next = currentRoute.getNextNode();
        if (next == null) {
            System.out.println("route finished!");
            pendingNextId = null;
            return;
        }
        pendingNextId = next.getId();
        System.out.println(pendingNextId);
    }

    private void handleTake() {
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        if (pendingNextId == null) {
            System.out.println("Error, no pending step");
            return;
        }
        AreaNode next = currentRoute.getNextNode();
        if (next == null || !pendingNextId.equals(next.getId())) {
            System.out.println("Error, inconsistent route state");
            return;
        }
        currentRoute.advance();
        pendingNextId = null;
    }

    private void handleShowRoute() {
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        java.util.List<AreaNode> remaining = currentRoute.getRemainingNodes();
        if (remaining.isEmpty()) {
            System.out.println();
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < remaining.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(remaining.get(i).getId());
        }
        System.out.println(builder.toString());
    }

    private void handleAbort() {
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        currentRoute = null;
        pendingNextId = null;
        planEndTime = null;
        System.out.println("route aborted");
    }

    private void handleAlternative() {
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        if (pendingNextId == null) {
            System.out.println("Error, no pending step");
            return;
        }
        AreaNode next = currentRoute.getNextNode();
        if (next == null || !pendingNextId.equals(next.getId())) {
            System.out.println("Error, inconsistent route state");
            return;
        }
        AreaNode cur = currentRoute.getCurrentNode();
        if (cur == null) {
            System.out.println("Error, no pending step");
            return;
        }
        if (planEndTime == null) {
            System.out.println("no alternative found");
            return;
        }
        int currentTime = currentRoute.getTimeAtCurrentPosition();
        RoutePlanner planner = new RoutePlanner(currentArea, skierContext);
        Route altRoute;
        try {
            altRoute = planner.planAlternativeRoute(cur.getId(), currentTime, planEndTime, pendingNextId);
        } catch (RoutePlanningException e) {
            altRoute = null;
        }
        if (altRoute != null) {
            int idx = currentRoute.getCurrentIndex();
            java.util.List<AreaNode> prefixNodes = new java.util.ArrayList<>(currentRoute.getNodes().subList(0, idx));
            java.util.List<Integer> prefixTimes = new java.util.ArrayList<>(currentRoute.getTimes().subList(0, idx));
            java.util.List<AreaNode> altNodes = new java.util.ArrayList<>(altRoute.getNodes());
            java.util.List<Integer> altTimes = new java.util.ArrayList<>(altRoute.getTimes());
            prefixNodes.addAll(altNodes);
            prefixTimes.addAll(altTimes);
            currentRoute = new Route(prefixNodes, prefixTimes, idx);
            System.out.println("avoided " + pendingNextId);
        } else {
            System.out.println("no alternative found");
        }
    }

    private void handleSetSkill(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing skill");
            return;
        }
        try {
            Skill skill = Skill.valueOf(value);
            skierContext.setSkill(skill);
        } catch (IllegalArgumentException e) {
            System.out.println("Error, invalid skill");
        }
    }

    private void handleSetGoal(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing goal");
            return;
        }
        try {
            Goal goal = Goal.valueOf(value);
            skierContext.setGoal(goal);
        } catch (IllegalArgumentException e) {
            System.out.println("Error, invalid goal");
        }
    }

    private void handleLike(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing preference");
            return;
        }
        if (isDifficulty(value)) {
            try {
                Difficulty difficulty = Difficulty.valueOf(value);
                skierContext.getPreferences().likeDifficulty(difficulty);
            } catch (IllegalArgumentException e) {
                System.out.println("Error, invalid preference");
            }
            return;
        }
        if (isSurface(value)) {
            try {
                Surface surface = Surface.valueOf(value);
                skierContext.getPreferences().likeSurface(surface);
            } catch (IllegalArgumentException e) {
                System.out.println("Error, invalid preference");
            }
            return;
        }
        System.out.println("Error, invalid preference");
    }

    private void handleDislike(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing preference");
            return;
        }
        if (isDifficulty(value)) {
            try {
                Difficulty difficulty = Difficulty.valueOf(value);
                skierContext.getPreferences().dislikeDifficulty(difficulty);
            } catch (IllegalArgumentException e) {
                System.out.println("Error, invalid preference");
            }
            return;
        }
        if (isSurface(value)) {
            try {
                Surface surface = Surface.valueOf(value);
                skierContext.getPreferences().dislikeSurface(surface);
            } catch (IllegalArgumentException e) {
                System.out.println("Error, invalid preference");
            }
            return;
        }
        System.out.println("Error, invalid preference");
    }

    private boolean isDifficulty(String value) {
        return "BLUE".equals(value) || "RED".equals(value) || "BLACK".equals(value);
    }

    private boolean isSurface(String value) {
        return "REGULAR".equals(value) || "ICY".equals(value) || "BUMPY".equals(value);
    }
}

