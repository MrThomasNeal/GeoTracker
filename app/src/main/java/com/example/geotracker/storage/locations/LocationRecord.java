package com.example.geotracker.storage.locations;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location_table")
// Room entity for location records for the location table
public class LocationRecord {
    @PrimaryKey(autoGenerate = true)
    private long id; // Auto-generated primary key
    private double latitude; // Latitude of the location
    private double longitude; // Longitude of the location
    private String recordingDatetime; // Recording datetime of the location

    // Setter methods to set values

    public void setId(long id) {
        this.id = id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRecordingDatetime(String recordingId) {
        this.recordingDatetime = recordingId;
    }

    // Getters to retrieve values

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getRecordingDatetime() {
        return recordingDatetime;
    }
}
