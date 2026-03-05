package edu.kit.kastel.area;

public class Slope implements AreaNode {

    private final String id;
    private final Difficulty difficulty;
    private final Surface surface;
    private final int lengthMeters;
    private final int heightDifferenceMeters;

    public Slope(String id, Difficulty difficulty, Surface surface,
                 int lengthMeters, int heightDifferenceMeters) {
        this.id = id;
        this.difficulty = difficulty;
        this.surface = surface;
        this.lengthMeters = lengthMeters;
        this.heightDifferenceMeters = heightDifferenceMeters;
    }

    @Override
    public String getId() {
        return id;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public Surface getSurface() {
        return surface;
    }

    public int getLengthMeters() {
        return lengthMeters;
    }

    public int getHeightDifferenceMeters() {
        return heightDifferenceMeters;
    }
}

