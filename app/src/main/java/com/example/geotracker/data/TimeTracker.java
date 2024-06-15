package com.example.geotracker.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeTracker {
    private long startTime; // Time when the movement recording began
    private long finishTime; // Time when the movement recording ended

    public void startRecording() {
        // Record the start time when the movement recording begins
        startTime = System.currentTimeMillis();
    }

    public void stopRecording() {
        // Record the finish time when the movement recording stops
        finishTime = System.currentTimeMillis();
    }

    public String getFormattedStartTime() {
        // Format the start time as "HH:mm"
        return formatTime(startTime);
    }

    public String getFormattedFinishTime() {
        // Format the finish time as "HH:mm"
        return formatTime(finishTime);
    }

    private String formatTime(long timeInMillis) {
        // Create a SimpleDateFormat object for the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Convert the time in milliseconds to a Date object
        Date date = new Date(timeInMillis);

        // Format the Date object to a string
        return dateFormat.format(date);
    }

    public String getDurationFormatted() {
        // Calculate the duration of the movement recording in seconds
        long durationInSeconds = (finishTime - startTime) / 1000;

        // Calculate minutes and seconds
        long minutes = durationInSeconds / 60;
        long seconds = durationInSeconds % 60;

        // Format the result as "minute:seconds"
        return String.format(Locale.UK, "%02d:%02d", minutes, seconds);
    }

    public String calculateSpeed(int distanceInMeters, String duration) {
        // Split the duration string to get minutes and seconds
        String[] timeParts = duration.split(":");
        int minutes = Integer.parseInt(timeParts[0]);
        int seconds = Integer.parseInt(timeParts[1]);

        // Convert duration to seconds
        int totalSeconds = minutes * 60 + seconds;

        // Convert meters to kilometers
        double distanceInKilometers = distanceInMeters / 1000.0;

        // Calculate speed in km/h
        double speedKmph = (distanceInKilometers / totalSeconds) * 3600.0;

        // Return the speed to one decimal point
        return String.format(Locale.UK, "%.1f", speedKmph);
    }

    public String movementType(String speedKmph) {

        // Covert speed from String to double
        double speed = Double.parseDouble(speedKmph);

        // Using boundaries for average movement speeds, return the most likely movement
        if (speed < 6.0) {
            return "Walk";
        } else if (speed >= 6.0 && speed < 12.0) {
            return "Run";
        } else {
            return "Cycle";
        }
    }
}
