package edu.kit.kastel.area;

/**
 * Represents a lift node in the ski area graph.
 * <p>
 * Lifts have operating times, waiting time and ride duration and may be marked
 * as transit lifts (valley stations) that can serve as route start/end points.
 *
 * @author usylb
 */
public class Lift implements AreaNode {

    private final String id;
    private final LiftType type;
    private final int startTimeMinutes;
    private final int endTimeMinutes;
    private final int rideDurationMinutes;
    private final int waitingTimeMinutes;
    private final boolean transit;

    /**
     * Creates a new lift instance.
     *
     * @param id                unique identifier of the lift
     * @param type              the lift type
     * @param startTimeMinutes  opening time in minutes
     * @param endTimeMinutes    closing time in minutes
     * @param rideDurationMinutes ride duration in minutes
     * @param waitingTimeMinutes  waiting time in minutes
     * @param transit           whether this lift is a transit (valley) lift
     */
    public Lift(String id, LiftType type, int startTimeMinutes, int endTimeMinutes,
                int rideDurationMinutes, int waitingTimeMinutes, boolean transit) {
        this.id = id;
        this.type = type;
        this.startTimeMinutes = startTimeMinutes;
        this.endTimeMinutes = endTimeMinutes;
        this.rideDurationMinutes = rideDurationMinutes;
        this.waitingTimeMinutes = waitingTimeMinutes;
        this.transit = transit;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the type of this lift.
     *
     * @return the lift type
     */
    public LiftType getType() {
        return type;
    }

    /**
     * Returns the opening time in minutes.
     *
     * @return opening time in minutes
     */
    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    /**
     * Returns the closing time in minutes.
     *
     * @return closing time in minutes
     */
    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    /**
     * Returns the ride duration in minutes.
     *
     * @return ride duration in minutes
     */
    public int getRideDurationMinutes() {
        return rideDurationMinutes;
    }

    /**
     * Returns the waiting time in minutes.
     *
     * @return waiting time in minutes
     */
    public int getWaitingTimeMinutes() {
        return waitingTimeMinutes;
    }

    /**
     * Indicates whether this lift is a transit lift (valley station).
     *
     * @return {@code true} if this lift is a transit lift, {@code false} otherwise
     */
    public boolean isTransit() {
        return transit;
    }

    /**
     * Checks whether this lift can be used when arriving at the current time.
     * <p>
     * A lift is usable if the arrival time including waiting time lies within
     * the operating window of the lift.
     *
     * @param currentTimeMinutes current time in minutes since midnight
     * @return {@code true} if the lift is usable at the given time,
     *         {@code false} otherwise
     */
    public boolean isUsableAt(int currentTimeMinutes) {
        int arrival = currentTimeMinutes + this.waitingTimeMinutes;
        return arrival >= this.startTimeMinutes && arrival <= this.endTimeMinutes;
    }

    /**
     * Returns the total duration of using this lift in minutes.
     * <p>
     * The duration is the sum of waiting time and ride duration.
     *
     * @return the total duration of a lift step in minutes
     */
    public int getStepDurationMinutes() {
        return this.waitingTimeMinutes + this.rideDurationMinutes;
    }
}

