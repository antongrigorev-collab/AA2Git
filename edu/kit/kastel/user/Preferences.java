package edu.kit.kastel.user;

import java.util.EnumSet;
import java.util.Set;

import edu.kit.kastel.area.Difficulty;
import edu.kit.kastel.area.Surface;
import edu.kit.kastel.area.Slope;

public class Preferences {

    private final Set<Difficulty> likedDifficulties = EnumSet.noneOf(Difficulty.class);
    private final Set<Difficulty> dislikedDifficulties = EnumSet.noneOf(Difficulty.class);
    private final Set<Surface> likedSurfaces = EnumSet.noneOf(Surface.class);
    private final Set<Surface> dislikedSurfaces = EnumSet.noneOf(Surface.class);

    public void likeDifficulty(Difficulty difficulty) {
        dislikedDifficulties.remove(difficulty);
        likedDifficulties.add(difficulty);
    }

    public void dislikeDifficulty(Difficulty difficulty) {
        likedDifficulties.remove(difficulty);
        dislikedDifficulties.add(difficulty);
    }

    public void likeSurface(Surface surface) {
        dislikedSurfaces.remove(surface);
        likedSurfaces.add(surface);
    }

    public void dislikeSurface(Surface surface) {
        likedSurfaces.remove(surface);
        dislikedSurfaces.add(surface);
    }

    public void reset() {
        likedDifficulties.clear();
        dislikedDifficulties.clear();
        likedSurfaces.clear();
        dislikedSurfaces.clear();
    }

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

