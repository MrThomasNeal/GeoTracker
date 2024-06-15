package com.example.geotracker.storage.movements;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movement_table")
// Room entity for movement records for the movement table
public class MovementRecord {
    @PrimaryKey(autoGenerate = true)
    private long id; // Auto-generated primary key
    private String recordingDatetime; // Datetime of the recording
    private String movementType; // Type of movement recorded (walk/run/cycle)
    private int distance; // Distance of the movement
    private String weather; // Weather recorded during movement
    private String startTime; // Time the recording started
    private String finishTime; // Time the recording ended
    private String duration; // Duration of the movement
    private String speed; // Speed of the movement
    private String notes; // Notes recorded in the annotation view after the recording stops

    // Getters and setters for the fields

    public String getRecordingDatetime() {
        return recordingDatetime;
    }

    public void setRecordingDatetime(String recordingDatetime) {
        this.recordingDatetime = recordingDatetime;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getDuration() { return duration; }

    public void setDuration(String duration) { this.duration = duration; }

    public String getSpeed() { return speed; }

    public void setSpeed(String speed) { this.speed = speed; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }
}