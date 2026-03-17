package edu.kit.kastel.user;

import java.util.EnumSet;
import java.util.Set;

import edu.kit.kastel.area.Difficulty;
import edu.kit.kastel.area.Surface;
import edu.kit.kastel.area.Slope;

/**
 * Stores positive and negative skier preferences for slope properties.
 * <p>
 * Preferences are used as a tie-breaker when two candidate routes provide the
 * same utility. Each match with a liked property contributes {@code +1}, each
 * match with a disliked property contributes {@code -1}.
 *
 * @author usylb
 */
public class Preferences {

    private final Set<Difficulty> likedDifficulties = EnumSet.noneOf(Difficulty.class);
    private final Set<Difficulty> dislikedDifficulties = EnumSet.noneOf(Difficulty.class);
    private final Set<Surface> likedSurfaces = EnumSet.noneOf(Surface.class);
    private final Set<Surface> dislikedSurfaces = EnumSet.noneOf(Surface.class);

    /**
     * Marks the given difficulty as liked.
     *
     * @param difficulty the difficulty to like
     */
    public void likeDifficulty(Difficulty difficulty) {
        dislikedDifficulties.remove(difficulty);
        likedDifficulties.add(difficulty);
    }

    /**
     * Marks the given difficulty as disliked.
     *
     * @param difficulty the difficulty to dislike
     */
    public void dislikeDifficulty(Difficulty difficulty) {
        likedDifficulties.remove(difficulty);
        dislikedDifficulties.add(difficulty);
    }

    /**
     * Marks the given surface type as liked.
     *
     * @param surface the surface type to like
     */
    public void likeSurface(Surface surface) {
        dislikedSurfaces.remove(surface);
        likedSurfaces.add(surface);
    }

    /**
     * Marks the given surface type as disliked.
     *
     * @param surface the surface type to dislike
     */
    public void dislikeSurface(Surface surface) {
        likedSurfaces.remove(surface);
        dislikedSurfaces.add(surface);
    }

    /**
     * Clears all stored preferences.
     */
    public void reset() {
        likedDifficulties.clear();
        dislikedDifficulties.clear();
        likedSurfaces.clear();
        dislikedSurfaces.clear();
    }

    /**
     * Computes the preference score for the given slope.
     *
     * @param slope the slope to score
     * @return the preference score (\(+1\) per liked match, \(-1\) per disliked match)
     */
    public int score(Slope slope) {
        int result = 0;
        Difficulty difficulty = slope.getDifficulty();
        Surface surface = slope.getSurface();

        if (likedDifficulties.contains(difficulty)) {
            result++;
        }
        if (dislikedDifficulties.contains(difficulty)) {
            result--;
        }
        if (likedSurfaces.contains(surface)) {
            result++;
        }
        if (dislikedSurfaces.contains(surface)) {
            result--;
        }

        return result;
    }
}

