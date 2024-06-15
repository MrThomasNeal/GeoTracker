package com.example.geotracker.storage.reminders;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminder_table")
// Room entity for reminder records for the reminder table
public class ReminderRecord {

    @PrimaryKey(autoGenerate = true)
    private long id; // Auto-generated primary key
    private String title; // Title of the reminder
    private double latitude; // Latitude position where the reminder is placed
    private double longitude; // Longitude position where the reminder is placed

    // Getters and setters for the values

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
