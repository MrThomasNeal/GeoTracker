package com.example.geotracker.data;

public class MovementData {
    private final int distanceMeters; // Distance in meters
    private final String formattedStartTime; // Movement start time, formatted for readability
    private final String formattedFinishTime; // Movement finish time, formatted for readability
    private final String formattedDuration; // Duration of movement, formatted for readability
    private final String speed; // Average speed of movement
    private final String movementType; // Type of movement (walk/run/cycle)
    private final String recordingDatetime; // Datetime of start of movement recording

    public MovementData(String recordingDatetime, int distanceMeters, String formattedStartTime, String formattedFinishTime,
                         String formattedDuration, String speed, String movementType) {
        // Constructor to set all the required values
        this.recordingDatetime = recordingDatetime;
        this.distanceMeters = distanceMeters;
        this.formattedStartTime = formattedStartTime;
        this.formattedFinishTime = formattedFinishTime;
        this.formattedDuration = formattedDuration;
        this.speed = speed;
        this.movementType = movementType;
    }

    // Getters for the saved variables

    public String getRecordingDatetime() {
        return recordingDatetime;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public String getFormattedStartTime() {
        return formattedStartTime;
    }

    public String getFormattedFinishTime() {
        return formattedFinishTime;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }

    public String getSpeed() {
        return speed;
    }

    public String getMovementType() {
        return movementType;
    }
}

