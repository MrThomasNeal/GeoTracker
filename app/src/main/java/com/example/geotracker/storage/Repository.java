package com.example.geotracker.storage;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.geotracker.storage.locations.LocationDao;
import com.example.geotracker.storage.locations.LocationRecord;
import com.example.geotracker.storage.movements.MovementDao;
import com.example.geotracker.storage.movements.MovementRecord;
import com.example.geotracker.storage.reminders.ReminderDao;
import com.example.geotracker.storage.reminders.ReminderRecord;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Abstraction layer for accessing and managing the data within the database
public class Repository {

    // Data Access Objects for each table in the database
    private final LocationDao locationDao;
    private final MovementDao movementDao;
    private final ReminderDao reminderDao;

    // Creates an instance of a single-threaded executor
    private final Executor executor = Executors.newSingleThreadExecutor();

    public Repository(Context context) {
        // Constructor to get the database and required Data Access Objects

        Database db = Database.getDatabase(context);
        locationDao = db.locationDao();
        movementDao = db.movementDao();
        reminderDao = db.reminderDao();
    }

    public void insertLocation(LocationRecord location) {
        // Insert a location record into location table in the database
        executor.execute(() -> locationDao.insert(location));
    }

    public void insertMovement(MovementRecord movementRecord) {
        // Insert a movement into the movement table in the database
        executor.execute(() -> movementDao.insert(movementRecord));
    }

    public void deleteMovementById(long movementId) {
        // Delete a movement by its ID within the movement table in the database
        executor.execute(() -> movementDao.deleteMovementById(movementId));
    }

    public void updateNotesAndWeather(long movementId, String notes, String weather) {
        // Update the notes and weather for a specific movement record in the movement table in the database
        executor.execute(() -> movementDao.updateNotesAndWeather(movementId, notes, weather));
    }

    public LiveData<List<MovementRecord>> getAllMovementsLiveData() {
        // Get a live data list of all the movements in the movement table within the database
        return movementDao.getAllMovementsLiveData();
    }

    public void insertReminder(ReminderRecord reminderRecord) {
        // Insert a reminder record into the reminder table in the database
        executor.execute(() -> reminderDao.insert(reminderRecord));
    }

    public void removeReminder(ReminderRecord reminderRecord) {
        // Remove a reminder record from the reminder table in the database
        executor.execute(() -> reminderDao.delete(reminderRecord));
    }

    public List<ReminderRecord> getAllReminders() {
        // Return a list of all the reminders in the reminder table in the database
        return reminderDao.getAllReminders();
    }

    public LiveData<List<ReminderRecord>> observeAllReminders() {
        // Return a live data list of all the reminder records in the reminder table in the database
        return reminderDao.observeAllReminders();
    }

}
