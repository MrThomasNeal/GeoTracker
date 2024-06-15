package com.example.geotracker.storage.locations;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
// Data Access Object for the location table in the database
public interface LocationDao {

    @Insert
    // Insert method to add a location record to the database
    void insert(LocationRecord location);
}
