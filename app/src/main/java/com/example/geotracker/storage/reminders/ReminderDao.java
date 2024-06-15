package com.example.geotracker.storage.reminders;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
// Data Access Object for the reminder table in the database
public interface ReminderDao {

    @Insert
    // Insert method to add a reminder to the database
    long insert(ReminderRecord reminderRecord);

    @Delete
    // Delete method to remove a reminder from the database
    void delete(ReminderRecord reminderRecord);

    @Query("SELECT * FROM reminder_table")
    // Method to return a list of all the reminders in the database
    List<ReminderRecord> getAllReminders();

    @Query("SELECT * FROM reminder_table")
    // Method to return live data of all the reminders in the database
    LiveData<List<ReminderRecord>> observeAllReminders();
}