package edu.kit.kastel.area;

public class Lift implements AreaNode {

    private final String id;
    private final LiftType type;
    private final int startTimeMinutes;
    private final int endTimeMinutes;
    private final int rideDurationMinutes;
    private final int waitingTimeMinutes;
    private final boolean transit;

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

    public LiftType getType() {
        return type;
    }

    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public int getRideDurationMinutes() {
        return rideDurationMinutes;
    }

    public int getWaitingTimeMinutes() {
        return waitingTimeMinutes;
    }

    public boolean isTransit() {
        return transit;
    }
}

