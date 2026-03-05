package edu.kit.kastel.planner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.area.AreaNode;
import edu.kit.kastel.area.Difficulty;
import edu.kit.kastel.area.Lift;
import edu.kit.kastel.area.SkiArea;
import edu.kit.kastel.area.Slope;
import edu.kit.kastel.area.Surface;
import edu.kit.kastel.exceptions.RoutePlanningException;
import edu.kit.kastel.user.Skill;
import edu.kit.kastel.user.SkierContext;

public class RoutePlanner {

    private final SkiArea area;
    private final SkierContext context;

    public RoutePlanner(SkiArea area, SkierContext context) {
        this.area = area;
        this.context = context;
    }

    public Route planRoute(String startId, int startTime, int endTime) throws RoutePlanningException {
        AreaNode start = area.getNode(startId);
        if (!(start instanceof Lift) || !((Lift) start).isTransit()) {
            throw new RoutePlanningException("Error, invalid start");
        }
        if (context.getSkill() == null || context.getGoal() == null) {
            throw new RoutePlanningException("Error, missing skill or goal");
        }
        if (endTime <= startTime) {
            throw new RoutePlanningException("Error, invalid time window");
        }

        BestRoute best = new BestRoute();
        List<AreaNode> currentNodes = new ArrayList<>();
        List<Integer> currentTimes = new ArrayList<>();
        List<Slope> currentSlopes = new ArrayList<>();
        Set<Slope> uniqueSlopes = new HashSet<>();

        currentNodes.add(start);
        currentTimes.add(startTime);

        search(start, startTime, endTime, currentNodes, currentTimes, currentSlopes, uniqueSlopes, best);

        if (best.nodes == null) {
            throw new RoutePlanningException("Error, no route");
        }
        List<AreaNode> nodesForRoute = best.nodes;
        List<Integer> timesForRoute = best.times;
        if (!nodesForRoute.isEmpty()) {
            AreaNode lastNode = nodesForRoute.get(nodesForRoute.size() - 1);
            if (lastNode instanceof Lift && ((Lift) lastNode).isTransit()) {
                nodesForRoute = new ArrayList<>(nodesForRoute);
                timesForRoute = new ArrayList<>(timesForRoute);
                nodesForRoute.remove(nodesForRoute.size() - 1);
                if (!timesForRoute.isEmpty()) {
                    timesForRoute.remove(timesForRoute.size() - 1);
                }
            }
        }
        return new Route(nodesForRoute, timesForRoute);
    }

    /**
     * Plans an alternative route from current position that does not use avoidNextId as immediate next step.
     */
    public Route planAlternativeRoute(String startId, int startTime, int endTime, String avoidNextId)
            throws RoutePlanningException {
        AreaNode start = area.getNode(startId);
        if (start == null) {
            throw new RoutePlanningException("Error, invalid start");
        }
        if (context.getSkill() == null || context.getGoal() == null) {
            throw new RoutePlanningException("Error, missing skill or goal");
        }
        if (endTime <= startTime) {
            throw new RoutePlanningException("Error, invalid time window");
        }

        BestRoute best = new BestRoute();
        List<AreaNode> currentNodes = new ArrayList<>();
        List<Integer> currentTimes = new ArrayList<>();
        List<Slope> currentSlopes = new ArrayList<>();
        Set<Slope> uniqueSlopes = new HashSet<>();

        searchAlternative(start, startTime, endTime, startId, avoidNextId,
                currentNodes, currentTimes, currentSlopes, uniqueSlopes, best);

        if (best.nodes == null) {
            return null;
        }
        List<AreaNode> nodesForRoute = best.nodes;
        List<Integer> timesForRoute = best.times;
        if (!nodesForRoute.isEmpty()) {
            AreaNode lastNode = nodesForRoute.get(nodesForRoute.size() - 1);
            if (lastNode instanceof Lift && ((Lift) lastNode).isTransit()) {
                nodesForRoute = new ArrayList<>(nodesForRoute);
                timesForRoute = new ArrayList<>(timesForRoute);
                nodesForRoute.remove(nodesForRoute.size() - 1);
                if (!timesForRoute.isEmpty()) {
                    timesForRoute.remove(timesForRoute.size() - 1);
                }
            }
        }
        return new Route(nodesForRoute, timesForRoute);
    }

    private void searchAlternative(AreaNode node, int currentTime, int endTime,
                        String startId, String avoidNextId,
                        List<AreaNode> nodesSoFar,
                        List<Integer> timesSoFar,
                        List<Slope> slopesSoFar,
                        Set<Slope> uniqueSlopes,
                        BestRoute best) {
        if (node instanceof Lift && ((Lift) node).isTransit() && currentTime <= endTime) {
            int utility = computeUtility(slopesSoFar, uniqueSlopes);
            int preferenceScore = 0;
            for (Slope slope : slopesSoFar) {
                preferenceScore += context.getPreferences().score(slope);
            }
            StringBuilder routeBuilder = new StringBuilder();
            for (int i = 0; i < nodesSoFar.size(); i++) {
                if (i > 0) {
                    routeBuilder.append(' ');
                }
                routeBuilder.append(nodesSoFar.get(i).getId());
            }
            String routeString = routeBuilder.toString();
            boolean takeAsBest = false;
            if (best.nodes == null) {
                takeAsBest = true;
            } else if (utility > best.utility) {
                takeAsBest = true;
            } else if (utility == best.utility) {
                if (preferenceScore > best.preferenceScore) {
                    takeAsBest = true;
                } else if (preferenceScore == best.preferenceScore
                        && routeString.compareTo(best.routeString) < 0) {
                    takeAsBest = true;
                }
            }
            if (takeAsBest) {
                best.utility = utility;
                best.preferenceScore = preferenceScore;
                best.routeString = routeString;
                best.nodes = new ArrayList<>(nodesSoFar);
                best.times = new ArrayList<>(timesSoFar);
            }
        }

        for (AreaNode successor : area.getSuccessors(node)) {
            if (node.getId().equals(startId) && nodesSoFar.isEmpty() && successor.getId().equals(avoidNextId)) {
                continue;
            }
            if (successor instanceof Lift) {
                Lift lift = (Lift) successor;
                int arrival = currentTime + lift.getWaitingTimeMinutes();
                if (arrival < lift.getStartTimeMinutes()
                        || arrival > lift.getEndTimeMinutes()) {
                    continue;
                }
                int finishTime = arrival + lift.getRideDurationMinutes();
                if (finishTime > endTime) {
                    continue;
                }
                nodesSoFar.add(successor);
                timesSoFar.add(finishTime);
                searchAlternative(successor, finishTime, endTime, startId, avoidNextId,
                        nodesSoFar, timesSoFar, slopesSoFar, uniqueSlopes, best);
                nodesSoFar.remove(nodesSoFar.size() - 1);
                timesSoFar.remove(timesSoFar.size() - 1);
            } else if (successor instanceof Slope) {
                Slope slope = (Slope) successor;
                int duration = computeSlopeMinutes(slope);
                int finishTime = currentTime + duration;
                if (finishTime > endTime) {
                    continue;
                }
                nodesSoFar.add(successor);
                timesSoFar.add(finishTime);
                slopesSoFar.add(slope);
                boolean addedUnique = uniqueSlopes.add(slope);
                searchAlternative(successor, finishTime, endTime, startId, avoidNextId,
                        nodesSoFar, timesSoFar, slopesSoFar, uniqueSlopes, best);
                if (addedUnique) {
                    uniqueSlopes.remove(slope);
                }
                slopesSoFar.remove(slopesSoFar.size() - 1);
                nodesSoFar.remove(nodesSoFar.size() - 1);
                timesSoFar.remove(timesSoFar.size() - 1);
            }
        }
    }

    private void search(AreaNode node, int currentTime, int endTime,
                        List<AreaNode> nodesSoFar,
                        List<Integer> timesSoFar,
                        List<Slope> slopesSoFar,
                        Set<Slope> uniqueSlopes,
                        BestRoute best) {
        if (node instanceof Lift && ((Lift) node).isTransit() && currentTime <= endTime) {
            int utility = computeUtility(slopesSoFar, uniqueSlopes);
            int preferenceScore = 0;
            for (Slope slope : slopesSoFar) {
                preferenceScore += context.getPreferences().score(slope);
            }
            StringBuilder routeBuilder = new StringBuilder();
            for (int i = 0; i < nodesSoFar.size(); i++) {
                if (i > 0) {
                    routeBuilder.append(' ');
                }
                routeBuilder.append(nodesSoFar.get(i).getId());
            }
            String routeString = routeBuilder.toString();
            boolean takeAsBest = false;
            if (best.nodes == null) {
                takeAsBest = true;
            } else if (utility > best.utility) {
                takeAsBest = true;
            } else if (utility == best.utility) {
                if (preferenceScore > best.preferenceScore) {
                    takeAsBest = true;
                } else if (preferenceScore == best.preferenceScore
                        && routeString.compareTo(best.routeString) < 0) {
                    takeAsBest = true;
                }
            }
            if (takeAsBest) {
                best.utility = utility;
                best.preferenceScore = preferenceScore;
                best.routeString = routeString;
                best.nodes = new ArrayList<>(nodesSoFar);
                best.times = new ArrayList<>(timesSoFar);
            }
        }

        for (AreaNode successor : area.getSuccessors(node)) {
            if (successor instanceof Lift) {
                Lift lift = (Lift) successor;
                int arrival = currentTime + lift.getWaitingTimeMinutes();
                if (arrival < lift.getStartTimeMinutes()
                        || arrival > lift.getEndTimeMinutes()) {
                    continue;
                }
                int finishTime = arrival + lift.getRideDurationMinutes();
                if (finishTime > endTime) {
                    continue;
                }
                nodesSoFar.add(successor);
                timesSoFar.add(finishTime);
                search(successor, finishTime, endTime, nodesSoFar, timesSoFar,
                        slopesSoFar, uniqueSlopes, best);
                nodesSoFar.remove(nodesSoFar.size() - 1);
                timesSoFar.remove(timesSoFar.size() - 1);
            } else if (successor instanceof Slope) {
                Slope slope = (Slope) successor;
                int duration = computeSlopeMinutes(slope);
                int finishTime = currentTime + duration;
                if (finishTime > endTime) {
                    continue;
                }
                nodesSoFar.add(successor);
                timesSoFar.add(finishTime);
                slopesSoFar.add(slope);
                boolean addedUnique = uniqueSlopes.add(slope);
                search(successor, finishTime, endTime, nodesSoFar, timesSoFar,
                        slopesSoFar, uniqueSlopes, best);
                if (addedUnique) {
                    uniqueSlopes.remove(slope);
                }
                slopesSoFar.remove(slopesSoFar.size() - 1);
                nodesSoFar.remove(nodesSoFar.size() - 1);
                timesSoFar.remove(timesSoFar.size() - 1);
            }
        }
    }

    private int computeSlopeMinutes(Slope slope) {
        double length = slope.getLengthMeters();
        double deltaH = slope.getHeightDifferenceMeters();
        double r = 0.0;
        if (length > 0.0) {
            r = deltaH / length;
        }
        double mdifficulty;
        if (slope.getDifficulty() == Difficulty.BLUE) {
            mdifficulty = 1.0;
        } else if (slope.getDifficulty() == Difficulty.RED) {
            mdifficulty = 1.15;
        } else {
            mdifficulty = 1.35;
        }
        double msurface;
        if (slope.getSurface() == Surface.REGULAR) {
            msurface = 1.0;
        } else if (slope.getSurface() == Surface.BUMPY) {
            msurface = 1.2;
        } else {
            msurface = 1.3;
        }
        double mskill;
        if (context.getSkill() == Skill.BEGINNER) {
            mskill = 1.35;
        } else if (context.getSkill() == Skill.INTERMEDIATE) {
            mskill = 1.1;
        } else {
            mskill = 0.9;
        }
        double seconds = (length / 8.0) * mdifficulty * msurface * (1.0 + 2.0 * r) * mskill;
        int minutes = (int) Math.ceil(seconds / 60.0);
        if (minutes < 0) {
            minutes = 0;
        }
        return minutes;
    }

    private int computeUtility(List<Slope> slopes, Set<Slope> uniqueSlopes) {
        Goal goal = context.getGoal();
        if (goal == Goal.ALTITUDE) {
            int sum = 0;
            for (Slope slope : slopes) {
                sum += slope.getHeightDifferenceMeters();
            }
            return sum;
        }
        if (goal == Goal.DISTANCE) {
            int sum = 0;
            for (Slope slope : slopes) {
                sum += slope.getLengthMeters();
            }
            return sum;
        }
        if (goal == Goal.NUMBER) {
            return slopes.size();
        }
        if (goal == Goal.UNIQUE) {
            return uniqueSlopes.size();
        }
        return 0;
    }

    private static class BestRoute {
        List<AreaNode> nodes;
        List<Integer> times;
        int utility;
        int preferenceScore;
        String routeString;
    }
}

