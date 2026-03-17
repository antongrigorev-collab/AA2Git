package edu.kit.kastel.app;

import edu.kit.kastel.area.Difficulty;
import edu.kit.kastel.area.Surface;
import edu.kit.kastel.planner.Goal;
import edu.kit.kastel.user.Skill;

/**
 * Implements commands that update the skier context (skill, goal, preferences).
 *
 * @author usylb
 */
class PreferenceCommands {

    private final CommandProcessor processor;

    /**
     * Creates a new helper for commands that update the skier context.
     *
     * @param processor the command processor that holds the global application state
     */
    PreferenceCommands(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Handles the {@code reset preferences} command.
     */
    void handleResetPreferences() {
        processor.getSkierContext().getPreferences().reset();
    }

    /**
     * Handles the {@code set skill} command.
     *
     * @param value the skill name to set
     */
    void handleSetSkill(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing skill");
            return;
        }
        Skill matchedSkill = null;
        for (Skill candidate : Skill.values()) {
            if (candidate.name().equals(value)) {
                matchedSkill = candidate;
                break;
            }
        }
        if (matchedSkill == null) {
            System.out.println("Error, invalid skill");
            return;
        }
        processor.getSkierContext().setSkill(matchedSkill);
    }

    /**
     * Handles the {@code set goal} command.
     *
     * @param value the goal name to set
     */
    void handleSetGoal(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing goal");
            return;
        }
        Goal matchedGoal = null;
        for (Goal candidate : Goal.values()) {
            if (candidate.name().equals(value)) {
                matchedGoal = candidate;
                break;
            }
        }
        if (matchedGoal == null) {
            System.out.println("Error, invalid goal");
            return;
        }
        processor.getSkierContext().setGoal(matchedGoal);
    }

    /**
     * Handles the {@code like} command for difficulty or surface preferences.
     *
     * @param value the preference value to like
     */
    void handleLike(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing preference");
            return;
        }
        if (isDifficulty(value)) {
            Difficulty difficulty = findDifficulty(value);
            if (difficulty == null) {
                System.out.println("Error, invalid preference");
                return;
            }
            processor.getSkierContext().getPreferences().likeDifficulty(difficulty);
            return;
        }
        if (isSurface(value)) {
            Surface surface = findSurface(value);
            if (surface == null) {
                System.out.println("Error, invalid preference");
                return;
            }
            processor.getSkierContext().getPreferences().likeSurface(surface);
            return;
        }
        System.out.println("Error, invalid preference");
    }

    /**
     * Handles the {@code dislike} command for difficulty or surface preferences.
     *
     * @param value the preference value to dislike
     */
    void handleDislike(String value) {
        if (value.isEmpty()) {
            System.out.println("Error, missing preference");
            return;
        }
        if (isDifficulty(value)) {
            Difficulty difficulty = findDifficulty(value);
            if (difficulty == null) {
                System.out.println("Error, invalid preference");
                return;
            }
            processor.getSkierContext().getPreferences().dislikeDifficulty(difficulty);
            return;
        }
        if (isSurface(value)) {
            Surface surface = findSurface(value);
            if (surface == null) {
                System.out.println("Error, invalid preference");
                return;
            }
            processor.getSkierContext().getPreferences().dislikeSurface(surface);
            return;
        }
        System.out.println("Error, invalid preference");
    }

    private Difficulty findDifficulty(String value) {
        for (Difficulty candidate : Difficulty.values()) {
            if (candidate.name().equals(value)) {
                return candidate;
            }
        }
        return null;
    }

    private Surface findSurface(String value) {
        for (Surface candidate : Surface.values()) {
            if (candidate.name().equals(value)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isDifficulty(String value) {
        return "BLUE".equals(value) || "RED".equals(value) || "BLACK".equals(value);
    }

    private boolean isSurface(String value) {
        return "REGULAR".equals(value) || "ICY".equals(value) || "BUMPY".equals(value);
    }
}

