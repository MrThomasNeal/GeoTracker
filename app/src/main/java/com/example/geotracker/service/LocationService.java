package com.example.geotracker.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.geotracker.view.MapView;

public class LocationService extends Service {

    private LocationManager locationManager; // LocationManager object for location updates
    private LocationListener locationListener; // LocationListener object for location updates
    private static final int NOTIFICATION_ID = 100; // Unique notification id for foregrounding the service
    private static final String CHANNEL_ID = "LocationServiceChannel"; // Channel ID for the service notification
    private final IBinder binder = new LocationServiceBinder(); // New binder object for this service
    private boolean recordingStatus = false; // Variable holding value of whether movement is being removed

    public class LocationServiceBinder extends Binder {
        // Class and getter to support binding for the service
        public LocationService getService() { return LocationService.this; }
    }

    @Override
    public void onCreate() {
        // Called when service is created

        // Call super class
        super.onCreate();

        // Create the channel to display the service running notification
        createNotificationChannel();

        // Begin listening for location updates
        setupLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // Called when service is started

        // Build the notification to show the service is running
        Notification notification = buildNotification();

        // Set the service to run in the foreground
        startForeground(NOTIFICATION_ID, notification);

        // Check if received intent in not null and has the action "STOP_ACTION"
        if (intent != null && "STOP_ACTION".equals(intent.getAction())) {

            // Stop the service from running in the foreground
            stopForeground(true);

            // Stop the service
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    // When service is bound, return the binder
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void setupLocationUpdates() {
        // Begin listening for location updates and broadcast them

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // Get the system's location service
        locationListener = new LocationListener() { // Initialise a location listener to receive location updates
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Called when the device's location is changed this method

                // Create an intent to broadcast a location update
                Intent intent = new Intent(Constants.LOCATION_UPDATE_ACTION);

                // Add the location object into the intent
                intent.putExtra("location", location);

                // Broadcast the location update intent
                getApplicationContext().sendBroadcast(intent);
            }
        };

        try {
            if (locationManager != null) {
                if (recordingStatus) {
                    // If recording, request updates more frequently
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000,  // Update interval = 1 second
                            10,
                            locationListener
                    );
                } else {
                    // If not recording, request updates less frequently to save battery
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000,  // Update interval = 5 seconds
                            10,
                            locationListener
                    );
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void setRecordingStatus(boolean status) {
        recordingStatus = status;

        // Restart the location updater with new frequency
        locationManager.removeUpdates(locationListener);
        setupLocationUpdates();
    }

    @Override
    public void onDestroy() {
        // Called when the service is destroyed

        // Call the super class .onDestroy() method
        super.onDestroy();

        // Stop the service from running in the foreground
        stopForeground(true);

        // Check if the locationManager and locationListener is not null
        if (locationManager != null && locationListener != null) {

            // Stop receiving updates to the manager from the listener
            locationManager.removeUpdates(locationListener);
        }
    }

    private void createNotificationChannel() {
        // Create the notification channel to display the service notification

        // Create a NotificationChannel with the specific ID, name and importance
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        // Get the system's NotificationManager service
        NotificationManager manager = getSystemService(NotificationManager.class);

        // If the manager is not null
        if (manager != null) {

            // Register the NotificationChannel with the NotificationManager
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {

        // Create a notification with an associated PendingIntent which launches the MapView the pressed
        Intent notificationIntent = new Intent(this, MapView.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Creates an intent for the stop button on the notification to stop the service
        Intent stopIntent = new Intent(this, LocationService.class);
        stopIntent.setAction("STOP_ACTION");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Creates a stop action for the notification
        NotificationCompat.Action stopAction =
                new NotificationCompat.Action.Builder(
                        android.R.drawable.ic_delete,
                        "Stop Service",
                        stopPendingIntent)
                        .build();

        // Build and return the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Service is running in the foreground")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Replace with your app's notification icon
                .setContentIntent(pendingIntent)
                .addAction(stopAction)
                .build();
    }
}