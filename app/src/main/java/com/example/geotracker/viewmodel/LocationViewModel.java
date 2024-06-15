package com.example.geotracker.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.geotracker.notifications.LocationBasedNotifications;
import com.example.geotracker.data.MovementData;
import com.example.geotracker.data.TimeTracker;
import com.example.geotracker.storage.Repository;
import com.example.geotracker.storage.locations.LocationRecord;
import com.example.geotracker.storage.reminders.ReminderRecord;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LocationViewModel extends ViewModel {

    private final MutableLiveData<Location> locationLiveData = new MutableLiveData<>(); // Device's location live data
    private LiveData<List<ReminderRecord>> remindersLiveData; // Live data list of the reminder table in database
    private Observer<List<ReminderRecord>> remindersObserver; // Observes and handles changed in the reminder live data
    private final List<Marker> addedMarkers = new ArrayList<>(); // List of all the markers on the map
    private String recordingDatetime = ""; // Recording datetime value
    private boolean isRecording = false; // Shows whether movement is currently being recorded or not
    private Repository repository; // Repository object to interact with the database
    private TimeTracker timeTracker = null; // Time tracker object to assist with data gathering from the movement

    // Executor and handler for background threading activities
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Map object
    private GoogleMap googleMap;
    @SuppressLint("StaticFieldLeak")
    // Properly handled in onClear(), needed for creating notification object
    private Context mapViewContext;


    private LocationViewModel() {
        // Private constructor to prevent instantiation
    }

    public void setMapViewContext(Context context) {
        // Set the map view activity context
        mapViewContext = context;
    }

    public void setLocation(Location location) {

        // Set the location of the location live data for device location tracking
        locationLiveData.setValue(location);

        // If a movement is currently being recorded
        if(isRecording) {
            // Create a location record and set the values - used to implement overall distance tracking
            LocationRecord locationRecord = new LocationRecord();
            locationRecord.setLatitude(location.getLatitude());
            locationRecord.setLongitude(location.getLongitude());
            locationRecord.setRecordingDatetime(recordingDatetime);
            repository.insertLocation(locationRecord);
        }
    }

    @Override
    protected void onCleared() {

        // Set map view context to null to avoid memory leaks
        mapViewContext = null;

        // If the reminders live data is not null, remove the observer for it
        if(remindersLiveData != null) {
            remindersLiveData.removeObserver(remindersObserver);
        }

        // Call super class
        super.onCleared();
    }

    public void setMap(GoogleMap map) {
        // Set the map to the variable googleMap
        googleMap = map;
    }

    public LiveData<Location> getLocationLiveData() {
        // Return the location live data
        return locationLiveData;
    }
    public void saveReminder(ReminderRecord reminderRecord) {
        // Add the reminder record to the reminder table in the database
        repository.insertReminder(reminderRecord);
    }

    public void startRecording() {
        // Method called when movement recording begins

        // Get the current date and format it
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yy_HH_mm", Locale.UK);
        Date currentDate = new Date();
        recordingDatetime = dateFormat.format(currentDate);

        // Initialise a new time tracker to assist with data gathering of the movement, and start recording
        timeTracker = new TimeTracker();
        timeTracker.startRecording();

        // Variable to check if a movement is currently being recorded
        isRecording = true;
    }

    public void stopRecording() {
        // Method called when the movement recording has ended

        // Variable to check if a movement is currently being recorded
        isRecording = false;

        // Stop the recording on the time tracker object
        timeTracker.stopRecording();
    }

    public MovementData returnMovementData(Polyline polyline) {

        // Get distance in meters from the polyline
        double distance = calculatePolylineLength(polyline);
        int distanceMeters = (int) distance;

        // Get date and place it in a local variable to allow clearing of the class variable
        String tempRecordingDatetime = recordingDatetime;
        recordingDatetime = "";

        // Get startTime, endTime + duration from the time tracker
        String formattedStartTime = timeTracker.getFormattedStartTime();
        String formattedFinishTime = timeTracker.getFormattedFinishTime();
        String formattedDuration = timeTracker.getDurationFormatted();

        // Get the speed of the movement from the time tracker
        String speed = timeTracker.calculateSpeed(distanceMeters, formattedDuration);

        // Get the movement type from the time tracker
        String movementType = timeTracker.movementType(speed);

        // Create and return a new movement data object with all the gathered data from the movement
        return new MovementData(tempRecordingDatetime, distanceMeters,
                formattedStartTime, formattedFinishTime, formattedDuration, speed, movementType);
    }

    public double calculatePolylineLength(Polyline polyline) {

        // Local variable to hold the total length of the polyline
        double totalLength = 0.0;

        // Get a list of the points within the polyline
        List<LatLng> points = polyline.getPoints();

        // If the polyline is one point long, its length is 0
        if (points.size() < 2) {
            return totalLength;
        }

        // Iterate through the points list and calculate the total length
        for (int i = 1; i < points.size(); i++) {

            LatLng previousLatLng = points.get(i - 1);
            LatLng currentLatLng = points.get(i);

            Location prevLocation = new Location("");
            prevLocation.setLatitude(previousLatLng.latitude);
            prevLocation.setLongitude(previousLatLng.longitude);

            Location currentLocation = new Location("");
            currentLocation.setLatitude(currentLatLng.latitude);
            currentLocation.setLongitude(currentLatLng.longitude);

            totalLength += prevLocation.distanceTo(currentLocation);
        }

        return totalLength;
    }

    public boolean recordingStatus() {
        // Return the status of the recording, whether its recording or not
        return isRecording;
    }

    public void setRepository(Repository repository) {

        // Set the repository in the class variable
        this.repository = repository;

        // Get the live data list of reminder records from the repository
        remindersLiveData = repository.observeAllReminders();

        // Initialise an observer to observe changes in the data within the reminder table in the database
        remindersObserver = new Observer<List<ReminderRecord>>() {
            @Override
            public void onChanged(List<ReminderRecord> reminderRecords) {
                // When data changes, update the markers on the map
                updateMarkers();
            }
        };
        // Observe the LiveData with the initialised observer
        remindersLiveData.observeForever(remindersObserver);
    }

    public String readableDatetime(String inputDatetime) {
        // Method which takes in an datetime and returns it as readable format

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd_MM_yy_HH_mm", Locale.UK);
        Date date;

        try {
            date = inputFormat.parse(inputDatetime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        // Format the date in "dd/MM/yy HH:mm" format
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK);
        if (date != null) {
            return outputFormat.format(date);
        } else {
            return null;
        }
    }

    public void checkProximityToMarkers(final Location currentLocation) {
        // Method which takes in the device's current location and checks proximity to reminder markers

        executor.execute(() -> {
            // Get all the reminder records from the database in a background thread
            List<ReminderRecord> reminderRecords = repository.getAllReminders();

            if(reminderRecords != null) {
                // Update the UI on the main thread
                handler.post(() -> {
                    // Iterate through each row in the reminder records
                    for (ReminderRecord reminder : reminderRecords) {

                        // Get the latitude and longitude of the reminder
                        double markerLat = reminder.getLatitude();
                        double markerLng = reminder.getLongitude();

                        // Calculate the distance between the updated location, and the reminder marker
                        float[] distance = new float[1];
                        Location.distanceBetween(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                markerLat, markerLng, distance
                        );

                        // Check if the distance is within 100 meters
                        if (distance[0] < 100) {

                            // Get the reminder message associated with the set reminder
                            String reminderMessage = reminder.getTitle();

                            // Initialise a new LocationBasedNotification object
                            LocationBasedNotifications notifications = new LocationBasedNotifications(mapViewContext);

                            // Show the location based reminder with the associated message to remind the user
                            notifications.showLocationReminder(reminderMessage);

                            // Remove the reminder from the database as the user has now been notified
                            repository.removeReminder(reminder);
                        }
                    }
                });
            }
        });
    }

    public void updateMarkers() {
        // Method is called when the reminder table in the database is changed

        executor.execute(() -> {

            // Get all the reminders from the reminder table in the database
            List<ReminderRecord> reminderRecords = repository.getAllReminders();

            // Update the UI on the main thread
            handler.post(() -> {

                // Remove all the current markers on the map
                removeMarkers();

                // Iterate through each row in the reminders table in the database
                for (ReminderRecord reminderRecord : reminderRecords) {

                    // Get the latitude and longitude of the reminder record
                    double latitude = reminderRecord.getLatitude();
                    double longitude = reminderRecord.getLongitude();

                    // Initialise a new LatLng object
                    LatLng markerLatLng = new LatLng(latitude, longitude);

                    // Hold the reminders title
                    String title = reminderRecord.getTitle();

                    // Add a marker to the map for each reminder in the table
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(markerLatLng)
                            .title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    // Add the marker to the class marker list to update them next time the method is called
                    addedMarkers.add(googleMap.addMarker(markerOptions));

                }
            });

        });
    }

    private void removeMarkers() {

        // Iterate through each marker in the marker list
        for (Marker marker : addedMarkers) {
            // Remove it from the map
            marker.remove();
        }

        // Clear the list of added markers
        addedMarkers.clear();
    }
}
