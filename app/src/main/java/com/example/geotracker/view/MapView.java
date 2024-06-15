package com.example.geotracker.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geotracker.service.Constants;
import com.example.geotracker.service.LocationReceiver;
import com.example.geotracker.data.MovementData;
import com.example.geotracker.service.LocationService;
import com.example.geotracker.R;
import com.example.geotracker.storage.reminders.ReminderRecord;
import com.example.geotracker.viewmodel.LocationViewModel;
import com.example.geotracker.storage.Repository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MapView extends AppCompatActivity implements OnMapReadyCallback {

    // Variables for the map view
    private GoogleMap googleMap;
    private com.google.android.gms.maps.MapView map;

    private boolean isBound = false; // Show whether service is bound or not
    private Button recordingButton; // Button to start recording on the UI
    private Repository repository; // Repository to interact with the database
    private Polyline currentPolyline; // Current polyline being displayed on the map
    private Marker currentMarker = null; // Current marker displaying where the user is
    private LocationViewModel locationViewModel; // View model for this view
    private Marker currentReminderMarker = null; // Temporary marker to set notifications

    // Executor and handler to carry out background threading activities
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    LocationService locationService; // Location service

    private static final int REQUEST_CODE_PERMISSIONS = 1; // Request code for location permissions
    private static final int BACKGROUND_REQUEST_CODE_PERMISSIONS = 2; // Request code for always allow location permission
    private static final String[] REQUIRED_PERMISSIONS = { // Required permissions for app to work to be requested from user
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private LocationReceiver locationReceiver; // Receiver for location updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialise view model
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        // Observe changes in the location data
        locationViewModel.getLocationLiveData().observe(this, this::updateMapWithLocation);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Initialise map
        map = findViewById(R.id.mapView);
        map.onCreate(savedInstanceState);
        map.getMapAsync(this);

        // Check and request permissions
        if(checkPermissions()) {
            // If app has required permissions, run the rest of the app
            runActivity();
        } else {
            // If app doesn't have required permissions, request them from the user
            requestPermissions();
        }

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void runActivity() {

        // Initialise a LocationReceiver to handle location updates
        locationReceiver = new LocationReceiver(locationViewModel);
        IntentFilter filter = new IntentFilter(Constants.LOCATION_UPDATE_ACTION);

        // Register the receiver
        registerReceiver(locationReceiver, filter, 0);

        // Initialise repository in background thread
        executor.execute(() -> {
            repository = new Repository(this);
            handler.post(() -> {
                locationViewModel.setRepository(repository);
            });
        });

        // Save the recording button in a class variable
        recordingButton = findViewById(R.id.movementButton);

        // Start the LocationService
        Intent locationServiceIntent = new Intent(this, LocationService.class);
        startService(locationServiceIntent);

        // Bind to LocationService
        bindService(locationServiceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE);

        // Set the context from this activity for the LocationViewModel
        locationViewModel.setMapViewContext(this);
    }

    // Service connection to bind to LocationService
    private final ServiceConnection locationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
            locationService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // Method to update the map with the latest location
    private void updateMapWithLocation(Location location) {
        if (googleMap != null) {

            // Add a marker for the new location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (currentMarker != null) {
                // Move the position of the device's current location
                currentMarker.setPosition(latLng);
            } else {
                // If the currentMarker doesn't exist, create it and place it at device's location
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
                currentMarker = googleMap.addMarker(markerOptions);
            }

            // Move the camera to the new location
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            // Set the zoom level for the camera
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));


            if(locationViewModel.recordingStatus()) {
                // If the movement is being recorded call the polyline function
                createOrUpdatePolyline(latLng);
            }

        }
    }

    private void showSetReminderDialog(final LatLng markerLatLng) {

        // Create a builder for the set reminder dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the necessary options and define the behaviour of each option
        builder.setMessage("Do you want to set a reminder here?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Launch create reminder dialog
                        createOrUpdateMarker(markerLatLng);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User chose not to set a reminder, remove the temporary marker
                        if (currentReminderMarker != null) {
                            currentReminderMarker.remove();
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // This is called when the dialog is dismissed
                        if (currentReminderMarker != null) {
                            // Remove the temporary marker
                            currentReminderMarker.remove();
                        }
                    }
                })
                .show();
    }

    private void createOrUpdateMarker(final LatLng markerLatLng) {

        // Create a builder for a dialog window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Reminder Title");

        // Set up the input box for the reminder title
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Get the set title for the reminder
                String title = input.getText().toString().trim();

                // Check if the title is empty
                if (!title.isEmpty()) {
                    // Save the reminder to the database
                    saveReminder(title, markerLatLng);
                    // Remove the temporary marker
                    if (currentReminderMarker != null) {
                        currentReminderMarker.remove();
                    }
                } else {
                    // Handle empty title, prompt user to tell them title cannot be empty
                    Toast.makeText(MapView.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If the cancel button is clicked
                if (currentReminderMarker != null) {
                    // Remove the temporary set reminder marker
                    currentReminderMarker.remove();
                }
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // If the dialog box is dismissed without performed actions
                if (currentReminderMarker != null) {
                    // Remove the temporary set reminder marker
                    currentReminderMarker.remove();
                }
            }
        });
        builder.show();
    }

    private void saveReminder(String title, LatLng markerLatLng) {
        // Method to save the reminder set by the user to the database

        // Get the latitude and longitude from the placed temporary reminder marker
        double latitude = markerLatLng.latitude;
        double longitude = markerLatLng.longitude;

        // Initialise a reminder record and set the values
        ReminderRecord reminderRecord = new ReminderRecord();
        reminderRecord.setTitle(title);
        reminderRecord.setLatitude(latitude);
        reminderRecord.setLongitude(longitude);

        // Call the save reminder method to save the reminder to the database
        locationViewModel.saveReminder(reminderRecord);
    }

    // Method to update the markers on the map when needed
    public void updateMarkers() {
        locationViewModel.updateMarkers();
    }

    private void createOrUpdatePolyline(LatLng newLatLng) {
        // Method to create or update the polyline shown when recording a movement

        if (currentPolyline == null) {

            // Create a new polyline and set its attributes
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLUE);
            polylineOptions.width(10);

            // Add the polyline to the map
            currentPolyline = googleMap.addPolyline(polylineOptions);
            currentPolyline.setVisible(true);
        }

        // Add the new location point to the polyline
        List<LatLng> points = currentPolyline.getPoints();
        points.add(newLatLng);
        currentPolyline.setPoints(points);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {

        // Set the map where needed
        googleMap = map;
        locationViewModel.setMap(googleMap);

        // Place zoom control tool on the map
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Set a listener to allow the user to click on the map to set reminders
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Add a temporary marker at the clicked location
                currentReminderMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                // Show a dialog to ask if the user wants to set a reminder
                showSetReminderDialog(latLng);
            }
        });
        // Listen for repository set changes
        checkRepositoryAndRunUpdate();
    }

    private void checkRepositoryAndRunUpdate() {
        executor.execute(() -> {
            // Check if the repository is initialised
            if (repository != null) {
                // If initialised, post message to main thread to update the markers
                handler.post(() -> updateMarkers());
            } else {
                // Retry after a delay of 1 second
                handler.postDelayed(() -> checkRepositoryAndRunUpdate(), 1000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();

        // Unregister to location receiver for location updates
        unregisterReceiver(locationReceiver);

        // Stop the location service
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);

        // Nullify location service
        if(locationService != null) {
            locationService = null;
        }

        // Unbind the location service
        if(isBound) {
            unbindService(locationServiceConnection);
            isBound = false;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    public void onRecordButtonClick(View v) {

        // When stop button pressed
        if(locationViewModel.recordingStatus()) {

            // Stop recording and display visual change to show this
            recordingButton.setText(R.string.start);
            locationViewModel.stopRecording();
            locationService.setRecordingStatus(false);

            if(currentPolyline != null) {

                // Temporary variable to hold polyline for later use in the method
                Polyline tempPolyline = currentPolyline;

                // Remove the polyline shown on the map
                currentPolyline.remove();
                currentPolyline = null;

                // Get all the data from the recording
                MovementData movementData = locationViewModel.returnMovementData(tempPolyline);

                // Create an intent for the annotation activity
                Intent annotateIntent = new Intent(MapView.this, AnnotationView.class);

                // Add all the necessary values to the intent
                annotateIntent.putExtra("RECORDING_DATETIME", movementData.getRecordingDatetime());
                annotateIntent.putExtra("DISTANCE", movementData.getDistanceMeters());
                annotateIntent.putExtra("START_TIME", movementData.getFormattedStartTime());
                annotateIntent.putExtra("FINISH_TIME", movementData.getFormattedFinishTime());
                annotateIntent.putExtra("DURATION", movementData.getFormattedDuration());
                annotateIntent.putExtra("SPEED", movementData.getSpeed());
                annotateIntent.putExtra("MOVEMENT_TYPE", movementData.getMovementType());
                annotateIntent.putExtra("CALLER", "MAPVIEW");

                // Start the annotation activity
                startActivity(annotateIntent);
            }
        } else {
            // When start button pressed, start the recording and visually show the user this
            recordingButton.setText(R.string.stop);
            locationViewModel.startRecording();
            locationService.setRecordingStatus(true);
        }
    }

    public void onDataButton(View v) {
        // When the data button is clicked, create an intent for the MovementList activity
        Intent movementListIntent = new Intent(this, MovementListView.class);
        // Start the activity
        startActivity(movementListIntent);
    }


    // Methods for permissions below

    private boolean checkPermissions() {
        // Check if the required permissions are granted for the app to work
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        // Check for background location permission only if primary location permissions are granted
        if (arePermissionsGranted(new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_GRANTED})) {
            // If background location permission is needed, request it
            if (needsBackgroundLocationPermission()) {
                requestBackgroundLocationPermission();
                return false;
            }
            return true;
        }
        return false;
    }

    private void requestBackgroundLocationPermission() {
        // Request the background location permission
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                BACKGROUND_REQUEST_CODE_PERMISSIONS
        );
    }

    private void requestPermissions() {
        // Request required permissions from the user so that the app can function
        ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
        );
    }

    private boolean needsBackgroundLocationPermission() {
        // Check if ACCESS_BACKGROUND_LOCATION permission is needed
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Check if all permissions have been granted
            if (arePermissionsGranted(grantResults)) {
                // If all permissions granted, check if background location permission is needed
                if (needsBackgroundLocationPermission()) {
                    // Request ACCESS_BACKGROUND_LOCATION permission
                    requestBackgroundLocationPermission();
                } else {
                    // If no additional permissions needed, run the program
                    runActivity();
                }
            } else {
                // If not all permissions granted, tell the user the permissions are needed
                Toast.makeText(this, "Permissions are needed", Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == BACKGROUND_REQUEST_CODE_PERMISSIONS) {
            // Check if the background location permission is granted
            if (arePermissionsGranted(grantResults)) {
                runActivity();
            } else {
                // Permission denied, inform the user and close the app
                Toast.makeText(this, "Background location permission is needed", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private boolean arePermissionsGranted(int[] grantResults) {
        // Check if all permissions have been granted
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}