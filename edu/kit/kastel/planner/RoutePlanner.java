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

/**
 * Computes optimal routes through a {@link SkiArea} for a given skier context.
 * <p>
 * Routes are evaluated according to the currently configured {@link Goal} and
 * the preference tie-breaker rules.
 *
 * @author usylb
 */
public class RoutePlanner {

    private final SkiArea area;
    private final SkierContext context;

    /**
     * Creates a new planner for the given ski area and skier context.
     *
     * @param area    the ski area graph to plan routes in
     * @param context the current skier configuration (skill, goal, preferences)
     */
    public RoutePlanner(SkiArea area, SkierContext context) {
        this.area = area;
        this.context = context;
    }

    /**
     * Plans the best route starting from a transit lift within a time window.
     *
     * @param startId   id of the start node (must be a transit lift)
     * @param startTime start time in minutes
     * @param endTime   end time in minutes (exclusive upper bound for finishing)
     * @return the planned route
     * @throws RoutePlanningException if planning is not possible or no route exists
     */
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

        SearchContext ctx = new SearchContext(endTime, currentNodes, currentTimes, currentSlopes, uniqueSlopes, best);
        search(start, startTime, ctx);

        if (best.nodes == null) {
            throw new RoutePlanningException("Error, no route possible in given time window");
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
     * Plans an alternative route from the current position that avoids a
     * specific immediate next step.
     *
     * @param startId      id of the current position
     * @param startTime    current time in minutes
     * @param endTime      end time in minutes
     * @param avoidNextId  id of the step that must not be used as immediate next step
     * @return the planned alternative route, or {@code null} if none exists
     * @throws RoutePlanningException if input data is invalid
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

        SearchContext baseContext = new SearchContext(endTime, currentNodes, currentTimes,
                currentSlopes, uniqueSlopes, best);
        AlternativeSearchContext ctx = new AlternativeSearchContext(startId, avoidNextId, baseContext);
        searchAlternative(start, startTime, ctx);

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

    private void searchAlternative(AreaNode node, int currentTime, AlternativeSearchContext ctx) {
        considerAsBestIfTransit(node, currentTime, ctx);

        for (AreaNode successor : area.getSuccessors(node)) {
            if (ctx.shouldAvoidImmediateNext(node, successor)) {
                continue;
            }
            if (successor instanceof Lift) {
                expandLiftAlternative((Lift) successor, currentTime, ctx);
            } else if (successor instanceof Slope) {
                expandSlopeAlternative((Slope) successor, currentTime, ctx);
            }
        }
    }

    private void search(AreaNode node, int currentTime, SearchContext ctx) {
        considerAsBestIfTransit(node, currentTime, ctx);

        for (AreaNode successor : area.getSuccessors(node)) {
            if (successor instanceof Lift) {
                expandLift((Lift) successor, currentTime, ctx);
            } else if (successor instanceof Slope) {
                expandSlope((Slope) successor, currentTime, ctx);
            }
        }
    }

    private void considerAsBestIfTransit(AreaNode node, int currentTime, SearchContext ctx) {
        if (node instanceof Lift && ((Lift) node).isTransit() && currentTime <= ctx.endTime) {
            considerAsBest(ctx.nodesSoFar, ctx.timesSoFar, ctx.slopesSoFar, ctx.uniqueSlopes, ctx.best);
        }
    }

    private void considerAsBest(List<AreaNode> nodesSoFar,
                                List<Integer> timesSoFar,
                                List<Slope> slopesSoFar,
                                Set<Slope> uniqueSlopes,
                                BestRoute best) {
        int utility = computeUtility(slopesSoFar, uniqueSlopes);
        int preferenceScore = computePreferenceScore(slopesSoFar);
        String routeString = toRouteString(nodesSoFar);

        boolean takeAsBest = false;
        if (best.nodes == null) {
            takeAsBest = true;
        } else if (utility > best.utility) {
            takeAsBest = true;
        } else if (utility == best.utility) {
            if (preferenceScore > best.preferenceScore) {
                takeAsBest = true;
            } else if (preferenceScore == best.preferenceScore && routeString.compareTo(best.routeString) < 0) {
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

    private int computePreferenceScore(List<Slope> slopesSoFar) {
        int preferenceScore = 0;
        for (Slope slope : slopesSoFar) {
            preferenceScore += context.getPreferences().score(slope);
        }
        return preferenceScore;
    }

    private String toRouteString(List<AreaNode> nodesSoFar) {
        StringBuilder routeBuilder = new StringBuilder();
        for (int i = 0; i < nodesSoFar.size(); i++) {
            if (i > 0) {
                routeBuilder.append(' ');
            }
            routeBuilder.append(nodesSoFar.get(i).getId());
        }
        return routeBuilder.toString();
    }

    private void expandLift(Lift lift, int currentTime, SearchContext ctx) {
        if (!lift.isUsableAt(currentTime)) {
            return;
        }
        int finishTime = currentTime + lift.getStepDurationMinutes();
        if (finishTime > ctx.endTime) {
            return;
        }

        ctx.nodesSoFar.add(lift);
        ctx.timesSoFar.add(finishTime);
        search(lift, finishTime, ctx);
        ctx.nodesSoFar.remove(ctx.nodesSoFar.size() - 1);
        ctx.timesSoFar.remove(ctx.timesSoFar.size() - 1);
    }

    private void expandSlope(Slope slope, int currentTime, SearchContext ctx) {
        int finishTime = currentTime + computeSlopeMinutes(slope);
        if (finishTime > ctx.endTime) {
            return;
        }

        ctx.nodesSoFar.add(slope);
        ctx.timesSoFar.add(finishTime);
        ctx.slopesSoFar.add(slope);
        boolean addedUnique = ctx.uniqueSlopes.add(slope);
        search(slope, finishTime, ctx);
        if (addedUnique) {
            ctx.uniqueSlopes.remove(slope);
        }
        ctx.slopesSoFar.remove(ctx.slopesSoFar.size() - 1);
        ctx.nodesSoFar.remove(ctx.nodesSoFar.size() - 1);
        ctx.timesSoFar.remove(ctx.timesSoFar.size() - 1);
    }

    private void expandLiftAlternative(Lift lift, int currentTime, AlternativeSearchContext ctx) {
        if (!lift.isUsableAt(currentTime)) {
            return;
        }
        int finishTime = currentTime + lift.getStepDurationMinutes();
        if (finishTime > ctx.endTime) {
            return;
        }

        ctx.nodesSoFar.add(lift);
        ctx.timesSoFar.add(finishTime);
        searchAlternative(lift, finishTime, ctx);
        ctx.nodesSoFar.remove(ctx.nodesSoFar.size() - 1);
        ctx.timesSoFar.remove(ctx.timesSoFar.size() - 1);
    }

    private void expandSlopeAlternative(Slope slope, int currentTime, AlternativeSearchContext ctx) {
        int finishTime = currentTime + computeSlopeMinutes(slope);
        if (finishTime > ctx.endTime) {
            return;
        }

        ctx.nodesSoFar.add(slope);
        ctx.timesSoFar.add(finishTime);
        ctx.slopesSoFar.add(slope);
        boolean addedUnique = ctx.uniqueSlopes.add(slope);
        searchAlternative(slope, finishTime, ctx);
        if (addedUnique) {
            ctx.uniqueSlopes.remove(slope);
        }
        ctx.slopesSoFar.remove(ctx.slopesSoFar.size() - 1);
        ctx.nodesSoFar.remove(ctx.nodesSoFar.size() - 1);
        ctx.timesSoFar.remove(ctx.timesSoFar.size() - 1);
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

}

