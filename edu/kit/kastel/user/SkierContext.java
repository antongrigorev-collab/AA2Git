package edu.kit.kastel.user;

import edu.kit.kastel.planner.Goal;

/**
 * Holds the current configuration of the skier for route planning.
 * <p>
 * The context stores the selected skill level, the optimisation goal and the
 * mutable set of preferences.
 *
 * @author usylb
 */
public class SkierContext {

    private Skill skill;
    private Goal goal;
    private final Preferences preferences = new Preferences();

    /**
     * Returns the current skill level of the skier.
     *
     * @return the current skill level, or {@code null} if not set
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Updates the skill level of the skier.
     *
     * @param skill the new skill level to use
     */
    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    /**
     * Returns the current optimisation goal for route planning.
     *
     * @return the current goal, or {@code null} if not set
     */
    public Goal getGoal() {
        return goal;
    }

    /**
     * Updates the optimisation goal for route planning.
     *
     * @param goal the new goal to use
     */
    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    /**
     * Returns the mutable set of skier preferences.
     *
     * @return the preference container
     */
    public Preferences getPreferences() {
        return preferences;
    }
}

