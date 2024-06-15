package com.example.geotracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.example.geotracker.viewmodel.LocationViewModel;

public class LocationReceiver extends BroadcastReceiver {

    private final LocationViewModel viewModel; // LocationViewModel object for interacting with main view

    public LocationReceiver(LocationViewModel setViewModel) {
        // Constructor for class

        // Set the passed in view model
        viewModel = setViewModel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // When a broadcast is received, this method is called

        // Check if a getAction() exists and its the location update action
        if (intent.getAction() != null && intent.getAction().equals(Constants.LOCATION_UPDATE_ACTION)) {

            // Extract the Location object from the broadcast
            Location receivedLocation = intent.getParcelableExtra("location");

            // Check if the received location is null
            if (receivedLocation != null) {

                // Set the location in the passed in view model
                viewModel.setLocation(receivedLocation);

                // Check if the new location is within distance of any location based reminders
                viewModel.checkProximityToMarkers(receivedLocation);
            }
        }
    }
};

