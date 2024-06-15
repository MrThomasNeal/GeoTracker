package com.example.geotracker.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.geotracker.storage.Repository;
import com.example.geotracker.storage.movements.MovementRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MovementListViewModel extends ViewModel {

    private LiveData<List<MovementRecord>> movementListLiveData; // Live data for the movement table in the database
    private Repository repository; // Repository object to interact with the database

    public MovementListViewModel() {
    }

    public void setContext(Context context) {

        // Initialise a new repository to interact with the database
        repository = new Repository(context);

        // Create a new executor for background operations
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Get the live data from the movement table to observe any changes
            movementListLiveData = repository.getAllMovementsLiveData();
        });
    }

    public LiveData<List<MovementRecord>> getMovementListLiveData() {
        // Return the live data list of movements in the movement table within the database
        return movementListLiveData;
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

    public String formatDistance(int distance) {
        // Method to format the movement distance for readability

        String distanceString;

        // If the distance is < 1000m, display the distance in meters
        if (distance < 1000) {
            distanceString = String.format(Locale.UK, "%dm", distance);
        } else {
            // If the distance is >= 1000m, display the distance in kilometers
            double distanceInKilometers = distance / 1000.0;
            distanceString = String.format(Locale.UK, "%.2fkm", distanceInKilometers);
        }
        return distanceString;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
