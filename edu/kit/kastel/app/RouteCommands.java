package edu.kit.kastel.app;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.area.AreaNode;
import edu.kit.kastel.exceptions.RoutePlanningException;
import edu.kit.kastel.planner.Route;
import edu.kit.kastel.planner.RoutePlanner;

/**
 * Implements commands that operate on the currently planned or running route.
 *
 * @author usylb
 */
class RouteCommands {

    private final CommandProcessor processor;

    /**
     * Creates a new helper for route-related commands.
     *
     * @param processor the command processor that holds the global application state
     */
    RouteCommands(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Handles the {@code next} command and prints the next node id or a finished message.
     */
    void handleNext() {
        Route currentRoute = processor.getCurrentRoute();
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        if (currentRoute.isFinished()) {
            System.out.println("route finished!");
            processor.setPendingNextId(null);
            processor.setCurrentRoute(null);
            return;
        }
        AreaNode next = currentRoute.getNextNode();
        if (next == null) {
            System.out.println("route finished!");
            processor.setPendingNextId(null);
            processor.setCurrentRoute(null);
            return;
        }
        processor.setPendingNextId(next.getId());
        System.out.println(next.getId());
    }

    /**
     * Handles the {@code take} command and advances the current route by one step.
     */
    void handleTake() {
        Route currentRoute = processor.getCurrentRoute();
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        String pendingNextId = processor.getPendingNextId();
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
        processor.setPendingNextId(null);
    }

    /**
     * Handles the {@code show route} command and prints the remaining route node ids.
     */
    void handleShowRoute() {
        Route currentRoute = processor.getCurrentRoute();
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        List<AreaNode> remaining = currentRoute.getRemainingNodes();
        if (remaining.isEmpty() || currentRoute.isFinished()) {
            System.out.println("Error, no route");
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

    /**
     * Handles the {@code abort} command and clears the current route state.
     */
    void handleAbort() {
        if (processor.getCurrentRoute() == null) {
            System.out.println("Error, no route");
            return;
        }
        processor.setCurrentRoute(null);
        processor.setPendingNextId(null);
        processor.setPlanEndTime(null);
        System.out.println("route aborted");
    }

    /**
     * Handles the {@code alternative} command by replanning while avoiding the pending next step.
     */
    void handleAlternative() {
        Route currentRoute = processor.getCurrentRoute();
        if (currentRoute == null) {
            System.out.println("Error, no route");
            return;
        }
        String pendingNextId = processor.getPendingNextId();
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
        Integer planEndTime = processor.getPlanEndTime();
        if (planEndTime == null) {
            System.out.println("no alternative found");
            return;
        }

        int currentTime = currentRoute.getTimeAtCurrentPosition();
        RoutePlanner planner = new RoutePlanner(processor.getCurrentArea(), processor.getSkierContext());
        Route altRoute;
        try {
            altRoute = planner.planAlternativeRoute(cur.getId(), currentTime, planEndTime, pendingNextId);
        } catch (RoutePlanningException exception) {
            altRoute = null;
        }

        if (altRoute == null) {
            System.out.println("no alternative found");
            return;
        }

        int idx = currentRoute.getCurrentIndex();
        List<AreaNode> prefixNodes = new ArrayList<>(currentRoute.getNodes().subList(0, idx));
        List<Integer> prefixTimes = new ArrayList<>(currentRoute.getTimes().subList(0, idx));
        List<AreaNode> altNodes = new ArrayList<>(altRoute.getNodes());
        List<Integer> altTimes = new ArrayList<>(altRoute.getTimes());
        prefixNodes.addAll(altNodes);
        prefixTimes.addAll(altTimes);
        processor.setCurrentRoute(new Route(prefixNodes, prefixTimes, idx));
        System.out.println("avoided " + pendingNextId);
    }
}

