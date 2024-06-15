package com.example.geotracker.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.geotracker.storage.movements.MovementDao;

public class MovementContentProvider extends ContentProvider {

    // Define the authority and paths for the content provider
    private static final String AUTHORITY = "com.example.geotracker";
    private static final String PATH_MOVEMENT = "movement_table";

    // Define constants for the UriMatcher
    private static final int MOVEMENTS = 1;
    private static final int MOVEMENT_ID = 2;

    // Declare MovementDao and UriMatcher
    private MovementDao movementDao;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Initialise UriMatcher with the specified paths
    static {
        uriMatcher.addURI(AUTHORITY, PATH_MOVEMENT, MOVEMENTS);
        uriMatcher.addURI(AUTHORITY, PATH_MOVEMENT + "/#", MOVEMENT_ID);
    }

    @Override
    public boolean onCreate() {

        // Get the database
        Database database = Database.getDatabase(getContext());

        // Initialise the movementDao when the content provider is created
        movementDao = database.movementDao();

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Cursor variable to allow third party to access data
        Cursor cursor;

        // Check whether request is for all movements or a specific movement
        switch (uriMatcher.match(uri)) {

            case MOVEMENTS:
                // Query for all movements
                cursor = movementDao.getAllMovementsAsCursor();
                break;

            case MOVEMENT_ID:
                // Query for a specific movement by ID
                String movementId = uri.getLastPathSegment();

                // Check if the movementId is not null
                if (movementId != null) {
                    // Get the cursor to provide access to the specific movement
                    cursor = movementDao.getMovementByIdAsCursor(Integer.parseInt(movementId));
                } else {
                    // Handle the case where movementId is null
                    throw new IllegalArgumentException("Invalid URI: " + uri);
                }
                break;

            default:
                // Handle unknown URI
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Set notification URI to inform the ContentResolver about changes
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            // Handle the case where getContext() is null
            throw new IllegalStateException("Context is null");
        }
        return cursor;
    }

    // Below actions are not supported due to read-only access to the db

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Insert operation is not supported
        throw new UnsupportedOperationException("Insert operation is not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Update operation is not supported
        throw new UnsupportedOperationException("Update operation is not supported");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Delete operation is not supported
        throw new UnsupportedOperationException("Delete operation is not supported");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Get type operation is not supported
        throw new UnsupportedOperationException("Get type operation is not supported");
    }
}
