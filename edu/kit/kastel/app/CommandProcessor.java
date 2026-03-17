package edu.kit.kastel.app;

import edu.kit.kastel.area.SkiArea;
import edu.kit.kastel.planner.Route;
import edu.kit.kastel.user.SkierContext;

/**
 * Processes user commands and manages the global application state.
 * <p>
 * This class parses textual commands, updates the skier context and the
 * currently loaded ski area and delegates route planning to the planner.
 *
 * @author usylb
 */
public class CommandProcessor {

    private boolean running = true;
    private SkiArea currentArea;
    private final SkierContext skierContext = new SkierContext();
    private Route currentRoute;
    private String pendingNextId;
    private Integer planEndTime;

    private final AreaCommands areaCommands = new AreaCommands(this);
    private final PlanningCommands planningCommands = new PlanningCommands(this);
    private final RouteCommands routeCommands = new RouteCommands(this);
    private final PreferenceCommands preferenceCommands = new PreferenceCommands(this);

    /**
     * Processes a single input line and executes the corresponding command.
     *
     * @param line the raw input line, may be {@code null}
     * @return {@code true} if the command loop should continue,
     *         {@code false} if the application should terminate
     */
    public boolean processLine(String line) {
        if (line == null) {
            return running;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return running;
        }

        if (handleSimpleCommand(trimmed)) {
            return running;
        }
        if (handleParameterizedCommand(trimmed)) {
            return running;
        }

        System.out.println("Error, unsupported command");
        return running;
    }

    private boolean handleSimpleCommand(String trimmed) {
        if ("quit".equals(trimmed)) {
            running = false;
            return true;
        }
        if ("next".equals(trimmed)) {
            routeCommands.handleNext();
            return true;
        }
        if ("take".equals(trimmed)) {
            routeCommands.handleTake();
            return true;
        }
        if ("show route".equals(trimmed)) {
            routeCommands.handleShowRoute();
            return true;
        }
        if ("abort".equals(trimmed)) {
            routeCommands.handleAbort();
            return true;
        }
        if ("alternative".equals(trimmed)) {
            routeCommands.handleAlternative();
            return true;
        }
        if ("reset preferences".equals(trimmed)) {
            preferenceCommands.handleResetPreferences();
            return true;
        }
        return false;
    }

    private boolean handleParameterizedCommand(String trimmed) {
        if (trimmed.startsWith("load area ")) {
            areaCommands.handleLoadArea(trimmed.substring("load area ".length()).trim());
            return true;
        }
        if (trimmed.startsWith("plan ")) {
            planningCommands.handlePlan(trimmed.substring("plan ".length()).trim());
            return true;
        }
        if (trimmed.startsWith("set skill ")) {
            preferenceCommands.handleSetSkill(trimmed.substring("set skill ".length()).trim());
            return true;
        }
        if (trimmed.startsWith("set goal ")) {
            preferenceCommands.handleSetGoal(trimmed.substring("set goal ".length()).trim());
            return true;
        }
        if (trimmed.startsWith("like ")) {
            preferenceCommands.handleLike(trimmed.substring("like ".length()).trim());
            return true;
        }
        if (trimmed.startsWith("dislike ")) {
            preferenceCommands.handleDislike(trimmed.substring("dislike ".length()).trim());
            return true;
        }
        return false;
    }

    /**
     * Returns the currently loaded ski area.
     *
     * @return the current ski area, or {@code null} if none is loaded
     */
    SkiArea getCurrentArea() {
        return currentArea;
    }

    /**
     * Sets the currently loaded ski area.
     *
     * @param currentArea the ski area to set (may be {@code null})
     */
    void setCurrentArea(SkiArea currentArea) {
        this.currentArea = currentArea;
    }

    /**
     * Returns the current skier context.
     *
     * @return the skier context instance
     */
    SkierContext getSkierContext() {
        return skierContext;
    }

    /**
     * Returns the currently planned or running route.
     *
     * @return the current route, or {@code null} if none is set
     */
    Route getCurrentRoute() {
        return currentRoute;
    }

    /**
     * Sets the current route.
     *
     * @param currentRoute the route to set (may be {@code null})
     */
    void setCurrentRoute(Route currentRoute) {
        this.currentRoute = currentRoute;
    }

    /**
     * Returns the id of the pending next route step selected by {@code next}.
     *
     * @return the pending next node id, or {@code null} if none is pending
     */
    String getPendingNextId() {
        return pendingNextId;
    }

    /**
     * Sets the pending next route step id.
     *
     * @param pendingNextId the pending next node id (may be {@code null})
     */
    void setPendingNextId(String pendingNextId) {
        this.pendingNextId = pendingNextId;
    }

    /**
     * Returns the end time of the most recently planned route window.
     *
     * @return the plan end time in minutes, or {@code null} if not available
     */
    Integer getPlanEndTime() {
        return planEndTime;
    }

    /**
     * Sets the end time of the current route planning window.
     *
     * @param planEndTime the plan end time in minutes (may be {@code null})
     */
    void setPlanEndTime(Integer planEndTime) {
        this.planEndTime = planEndTime;
    }
}

