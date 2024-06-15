package com.example.geotracker.storage;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.geotracker.storage.locations.LocationDao;
import com.example.geotracker.storage.locations.LocationRecord;
import com.example.geotracker.storage.movements.MovementDao;
import com.example.geotracker.storage.movements.MovementRecord;
import com.example.geotracker.storage.reminders.ReminderDao;
import com.example.geotracker.storage.reminders.ReminderRecord;

// Define the Room database with the specified tables, version and export schema
@androidx.room.Database(entities = {LocationRecord.class, MovementRecord.class, ReminderRecord.class}, version = 1, exportSchema = false)
public abstract class Database extends RoomDatabase {

    // Define abstract methods to get the Data Access Object for each entity
    public abstract LocationDao locationDao();
    public abstract MovementDao movementDao();
    public abstract ReminderDao reminderDao();

    // Declare a volatile instance variable for the database
    private static volatile Database INSTANCE;

    // Create method to get a singleton instance of the database
    public static Database getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (Database.class) {
                if (INSTANCE == null) {
                    // Build the Room database with the application context, class and database name
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            Database.class,
                            "location_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}