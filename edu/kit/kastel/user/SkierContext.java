package edu.kit.kastel.user;

import edu.kit.kastel.planner.Goal;

public class SkierContext {

    private Skill skill;
    private Goal goal;
    private final Preferences preferences = new Preferences();

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}

