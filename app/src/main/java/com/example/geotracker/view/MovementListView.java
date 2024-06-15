package com.example.geotracker.view;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.example.geotracker.R;
import com.example.geotracker.storage.movements.MovementRecord;
import com.example.geotracker.viewmodel.MovementListViewModel;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MovementListView extends AppCompatActivity {

    private MovementListViewModel movementListViewModel; // View model for movement list

    // Executor and handler to handle background threaded operations
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_list_view);

        // Initialise the view model for this view
        movementListViewModel = new ViewModelProvider(this).get(MovementListViewModel.class);

        // Set the context for this view in the view model
        movementListViewModel.setContext(this);

        // Get the "NO DATA AVAILABLE" text and set it in invisible on the view
        TextView noDataAvailableText = findViewById(R.id.noDataAvailableText);
        noDataAvailableText.setVisibility(View.INVISIBLE);

        // Set the recycler view
        RecyclerView movementList = findViewById(R.id.movementList);
        movementList.setLayoutManager(new LinearLayoutManager(this));

        executor.execute(() -> {
            // Get the live data of movement recording in the database
            LiveData<List<MovementRecord>> liveData = movementListViewModel.getMovementListLiveData();
            handler.post(() -> {
                if(liveData != null) {
                    // Observe the live data for changes
                    liveData.observe(this, movements -> {
                        if(movements.isEmpty()) {
                            // Display "NO DATA AVAILABLE" if thats the case
                            noDataAvailableText.setVisibility(View.VISIBLE);
                        }
                        // Initialise and set the adapter for the recycler view
                        MovementListAdapter movementListAdapter = new MovementListAdapter(liveData, movementListViewModel, this);
                        movementList.setAdapter(movementListAdapter);
                    });
                } else {
                    // If the live data fetch was unsuccessful, recreate the window to try again
                    recreate();
                }
            });
        });

        // Handle device back button presses
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    // Handle the back button within the UI
    public void onBackButton(View v) {
        finish();
    }
}