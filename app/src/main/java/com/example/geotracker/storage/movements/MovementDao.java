package com.example.geotracker.storage.movements;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.geotracker.storage.reminders.ReminderRecord;

import java.util.List;

@Dao
// Data Access Object for the movement table in the database
public interface MovementDao {
    @Insert
    // Insert method to add a movement to the database
    void insert(MovementRecord movementRecord);

    @Query("DELETE FROM movement_table WHERE id = :movementId")
    // Delete method to delete the movement with the given id from the database
    void deleteMovementById(long movementId);

    @Query("UPDATE movement_table SET notes = :notes, weather = :weather WHERE id = :movementId")
    // Update method to update a row in the database based off the ID with updates notes and weather info
    void updateNotesAndWeather(long movementId, String notes, String weather);

    @Query("SELECT * FROM movement_table")
    // Return a list of the movement records in the database
    List<MovementRecord> getAllMovements();

    @Query("SELECT * FROM movement_table")
    // Returns a livedata list of the movement records in the database
    LiveData<List<MovementRecord>> getAllMovementsLiveData();

    @Query("SELECT * FROM movement_table")
    // Get all movements from the database as a cursor for content provider
    Cursor getAllMovementsAsCursor();

    @Query("SELECT * FROM movement_table WHERE id = :movementId")
    // Get a specific movement as a cursor by its id for content provider
    Cursor getMovementByIdAsCursor(long movementId);
}
