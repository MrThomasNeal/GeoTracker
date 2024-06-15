package com.example.geotracker.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

public class LocationBasedNotifications extends ContextWrapper {

    private static final String CHANNEL_ID = "location_reminder_channel"; // Channel ID for notifications
    private static final String CHANNEL_NAME = "Location Reminder Channel"; // Channel name for notifications
    private final NotificationManager notificationManager; // Notification manager object for notifications

    public LocationBasedNotifications(Context base) {
        // Constructor for class

        // Call superclass
        super(base);

        // Create the notification channel to send notifications
        createNotificationChannel();

        // Get the notification service to allow use of notifications on android device
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showLocationReminder(String reminderMessage) {
        // Display a notification when the user has reached a location based reminder

        // Hash the reminderMessage to be used for the notificationId
        int hashTitle = reminderMessage.hashCode();
        int notificationId = Math.abs(hashTitle);

        // Check that notification hasn't been displayed yet
        String title = "Location Reminder";
        String message = "Reminder: " + reminderMessage;

        // Build the notification and post the notification to the device
        Notification notification = buildNotification(title, message);
        notificationManager.notify(notificationId, notification);
    }

    private Notification buildNotification(String title, String message) {
        // Builds and returns a notification using the provided title and message

        // Initialise a notification builder with the specified channel ID
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);

        // Set the title, content text, small icon, and auto-cancel behaviour of the notification
        builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true); // Dismiss the notification when tapped

        // Build and return the built Notification object
        return builder.build();
    }

    private void createNotificationChannel() {
        // Creates the notification channel required for displaying notifications on the device

        // Define the properties of the notification channel
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );

        // Enable vibration and set a unique and distinct vibration pattern for the channel so the
        // user knows its a location based reminder and not a usual reminder
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});


        // Get the system's NotificationManager service
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        // Check if the service is available
        if (notificationManager != null) {
            // Create the notification channel using the predefined properties
            notificationManager.createNotificationChannel(channel);
        }

    }
}
