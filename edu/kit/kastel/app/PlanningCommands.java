package edu.kit.kastel.app;

import edu.kit.kastel.exceptions.RoutePlanningException;
import edu.kit.kastel.planner.RoutePlanner;

/**
 * Implements commands related to planning routes and parsing time windows.
 *
 * @author usylb
 */
class PlanningCommands {

    private final CommandProcessor processor;

    /**
     * Creates a new helper for planning-related commands.
     *
     * @param processor the command processor that holds the global application state
     */
    PlanningCommands(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Handles the {@code plan} command and updates the current route in the processor.
     *
     * @param args the raw argument string after the {@code plan} keyword
     */
    void handlePlan(String args) {
        if (processor.getCurrentArea() == null) {
            System.out.println("Error, no area loaded");
            return;
        }
        String[] parts = args.split(" ");
        if (parts.length != 3) {
            System.out.println("Error, invalid plan arguments");
            return;
        }

        String startId = parts[0];
        int startMinutes = parseTime(parts[1]);
        int endMinutes = parseTime(parts[2]);
        if (startMinutes < 0 || endMinutes < 0) {
            System.out.println("Error, invalid time");
            return;
        }

        RoutePlanner planner = new RoutePlanner(processor.getCurrentArea(), processor.getSkierContext());
        try {
            processor.setCurrentRoute(planner.planRoute(startId, startMinutes, endMinutes));
            processor.setPendingNextId(null);
            processor.setPlanEndTime(endMinutes);
            System.out.println("route planned");
        } catch (RoutePlanningException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private int parseTime(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return -1;
        }
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return -1;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }
}

