package com.example.geotracker.view;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.geotracker.R;
import com.example.geotracker.storage.Repository;
import com.example.geotracker.storage.movements.MovementRecord;
import com.example.geotracker.viewmodel.LocationViewModel;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnnotationView extends AppCompatActivity {

    // Fields for storing movement record details
    private String currentWeatherChoice;
    private long id;
    private String recordingDatetime;
    private int distance;
    private String startTime;
    private String finishTime;
    private String duration;
    private String speed;
    private String movementType;
    private String notes;
    private String caller;

    // Repository for interacting with the database
    private final Repository repository = new Repository(this);

    // Executor for background thread execution
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation_view);

        // Initialise the view model
        LocationViewModel locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        // Get the intent
        Intent intent = getIntent();

        // Retrieve the data from the intent
        id = intent.getLongExtra("ID", -1);
        recordingDatetime = intent.getStringExtra("RECORDING_DATETIME");
        distance = intent.getIntExtra("DISTANCE", 0);
        startTime = intent.getStringExtra("START_TIME");
        finishTime = intent.getStringExtra("FINISH_TIME");
        duration = intent.getStringExtra("DURATION");
        speed = intent.getStringExtra("SPEED");
        movementType = intent.getStringExtra("MOVEMENT_TYPE");
        notes = intent.getStringExtra("NOTES");
        caller = intent.getStringExtra("CALLER");

        // Update the save/update button based on where the activity was launched form
        if(Objects.equals(caller, "MOVEMENT_LIST")) {
            // If the activity was launched from the MovementListView:

            // Change the "save" button to display "update"
            Button saveButton = findViewById(R.id.saveButton);
            saveButton.setText(R.string.update);

            // Display the saved notes in the edit text field
            EditText notesText = findViewById(R.id.notesInput);
            notesText.setText(notes);
        }

        // Handle the back button press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                // Check if the launcher of this activity was MapView or MovementListView
                if(Objects.equals(caller, "MAPVIEW")) {
                    // Show the "save or discard" dialog
                    showSaveDiscardDialog();
                } else if(Objects.equals(caller, "MOVEMENT_LIST")) {
                    // Close the activity
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Populate the display recording datetime field
        TextView displayRecordingDatetime = findViewById(R.id.displayRecordingDatetime);
        String readableDatetime= locationViewModel.readableDatetime(recordingDatetime);
        displayRecordingDatetime.setText(readableDatetime);

        // Populate the display distance field
        TextView displayDistance = findViewById(R.id.displayDistance);
        String distanceString;
        if (distance < 1000) {
            // Display the distance in m
            distanceString = String.format(Locale.UK, "%dm", distance);
        } else {
            // Display the distance in km
            double distanceInKilometers = distance / 1000.0;
            distanceString = String.format(Locale.UK, "%.2fkm", distanceInKilometers);
        }
        displayDistance.setText(distanceString);

        // Populate the display start time field
        TextView displayStartTime = findViewById(R.id.displayStartTime);
        displayStartTime.setText(startTime);

        // Populate the display finish time field
        TextView displayFinishTime = findViewById(R.id.displayFinishTime);
        displayFinishTime.setText(finishTime);

        // Populate the display duration field
        TextView displayDuration = findViewById(R.id.displayDuration);
        String[] timeParts = duration.split(":");
        // Calculate what format to display the duration in
        int minutes = Integer.parseInt(timeParts[0]);
        int seconds = Integer.parseInt(timeParts[1]);
        String durationString;
        if (minutes >= 60) {
            int hours = minutes / 60;
            minutes %= 60;
            durationString = String.format(Locale.UK, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            durationString = String.format(Locale.UK, "%02d:%02d", minutes, seconds);
        }
        displayDuration.setText(durationString);

        // Populate the display speed field
        TextView displaySpeed = findViewById(R.id.displaySpeed);
        String speedString = String.format(Locale.UK, speed + "kmph");
        displaySpeed.setText(speedString);

        // Populate the display movement type field
        TextView displayMovementType = findViewById(R.id.displayMovementType);
        displayMovementType.setText(movementType);

    }

    // Show a dialog prompting the user to save or discard the movement
    private void showSaveDiscardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Movement?");
        builder.setMessage("Do you want to save your movement?");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If user clicks the save section of the dialog, call the saveMovement() method
                saveMovement();
            }
        });
        builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If the user clicks the discard section of the dialog, end the activity
                finish();
            }
        });
        builder.setNeutralButton("Cancel", null);

        // Create the dialog and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Method to handle the weather set buttons
    public void onSunnyButton(View v) {
        currentWeatherChoice = "Sunny";
    }

    public void onCloudyButton(View v) {
        currentWeatherChoice = "Cloudy";
    }

    public void onRainyButton(View v) {
        currentWeatherChoice = "Rainy";
    }

    // Method to handle when the save button is pressed
    public void onSaveButton(View v) {

        // Get and set the notes from the text box
        EditText notesEditText = findViewById(R.id.notesInput);
        notes = notesEditText.getText().toString();

        // Perform actions based on caller
        if(Objects.equals(caller, "MAPVIEW")) {
            saveMovement();
        } else if(Objects.equals(caller, "MOVEMENT_LIST")) {
            updateMovement();
        }

        // Close the activity
        finish();
    }

    // Method to handle when the delete button clicked
    public void onDeleteButton(View v) {
       if(Objects.equals(caller, "MOVEMENT_LIST")) {
            deleteMovement();
        }
       finish();
    }

    // Method to handle when the update button is clicked
    private void updateMovement() {
        // Update the new values in the database through the repository
        repository.updateNotesAndWeather(id, notes, currentWeatherChoice);
    }

    private void deleteMovement() {
        // Delete the movement specified in the id from the database through the repository
        repository.deleteMovementById(id);
    }

    private void saveMovement() {
        // Create a movement record and set the values
        MovementRecord movementRecord = new MovementRecord();
        movementRecord.setRecordingDatetime(recordingDatetime);
        movementRecord.setMovementType(movementType);
        movementRecord.setDistance(distance);
        movementRecord.setWeather(currentWeatherChoice);
        movementRecord.setStartTime(startTime);
        movementRecord.setFinishTime(finishTime);
        movementRecord.setDuration(duration);
        movementRecord.setSpeed(speed);
        movementRecord.setNotes(notes);

        executor.execute(() -> {
            // Insert the movement into the database through the repository in a background thread
            repository.insertMovement(movementRecord);
        });
    }
}