package edu.kit.kastel.area;

/**
 * Represents a ski slope in a {@link SkiArea}.
 * <p>
 * A slope has a unique identifier, a difficulty level, a surface type, a length
 * in meters and a height difference in meters.
 *
 * @author usylb
 */
public class Slope implements AreaNode {

    private final String id;
    private final Difficulty difficulty;
    private final Surface surface;
    private final int lengthMeters;
    private final int heightDifferenceMeters;

    /**
     * Creates a new slope with the given properties.
     *
     * @param id                    the unique identifier of the slope
     * @param difficulty            the difficulty level of the slope
     * @param surface               the surface type of the slope
     * @param lengthMeters          the length of the slope in meters
     * @param heightDifferenceMeters the height difference of the slope in meters
     */
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

    /**
     * Returns the difficulty level of this slope.
     *
     * @return the difficulty level
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Returns the surface type of this slope.
     *
     * @return the surface type
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * Returns the length of this slope in meters.
     *
     * @return the length in meters
     */
    public int getLengthMeters() {
        return lengthMeters;
    }

    /**
     * Returns the height difference of this slope in meters.
     *
     * @return the height difference in meters
     */
    public int getHeightDifferenceMeters() {
        return heightDifferenceMeters;
    }
}

