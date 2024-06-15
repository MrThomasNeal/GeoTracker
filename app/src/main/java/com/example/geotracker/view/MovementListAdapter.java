package com.example.geotracker.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geotracker.R;
import com.example.geotracker.storage.movements.MovementRecord;
import com.example.geotracker.viewmodel.MovementListViewModel;

import java.util.List;

public class MovementListAdapter extends RecyclerView.Adapter<MovementListAdapter.MovementViewHolder> {

    private List<MovementRecord> movementList; // List of movement records
    public MovementListViewModel movementListViewModel; // View model for this movement list

    @SuppressLint("NotifyDataSetChanged")
    public MovementListAdapter(LiveData<List<MovementRecord>> liveData, MovementListViewModel viewModel, LifecycleOwner lifecycleOwner) {

        // Set the view model passed in
        this.movementListViewModel = viewModel;

        // Observe the movement records live data, if changed observed, notify dataset changed
        liveData.observe(lifecycleOwner, movements -> {
            if (movements != null) {
                movementList = movements;
                notifyDataSetChanged(); // Notify the adapter when the list changes
            }
        });
    }

    @NonNull
    @Override
    public MovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movement_item, parent, false);
        return new MovementViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovementViewHolder holder, int position) {

        // Get the movement at the specified position from the list of movement records
        MovementRecord movement = movementList.get(position);

        // Bind the movement to the view holder
        holder.bind(movement, movementListViewModel);

        // Set an OnClickListener on the item
        holder.itemView.setOnClickListener(v -> {
            // If a movement from the list is clicked, launch the annotation activity with relevant values
            Intent annotateIntent = new Intent(holder.itemView.getContext(), AnnotationView.class);
            annotateIntent.putExtra("ID", movement.getId());
            annotateIntent.putExtra("RECORDING_DATETIME", movement.getRecordingDatetime());
            annotateIntent.putExtra("DISTANCE", movement.getDistance());
            annotateIntent.putExtra("START_TIME", movement.getStartTime());
            annotateIntent.putExtra("FINISH_TIME", movement.getFinishTime());
            annotateIntent.putExtra("DURATION", movement.getDuration());
            annotateIntent.putExtra("SPEED", movement.getSpeed());
            annotateIntent.putExtra("MOVEMENT_TYPE", movement.getMovementType());
            annotateIntent.putExtra("NOTES", movement.getNotes());
            annotateIntent.putExtra("CALLER", "MOVEMENT_LIST");
            (holder.itemView.getContext()).startActivity(annotateIntent);
        });
    }

    @Override
    // Get the item count in the movement list list
    public int getItemCount() {
        return movementList.size();
    }

    static class MovementViewHolder extends RecyclerView.ViewHolder {

        // TextViews to display values on the recycler view items
        private final TextView recordingDateTimeTextView;
        private final TextView movementTypeTextView;
        private final TextView distanceTextView;

        public MovementViewHolder(@NonNull View itemView) {

            super(itemView);

            // Get the text views from the xml file
            recordingDateTimeTextView = itemView.findViewById(R.id.recordingDateTimeTextView);
            movementTypeTextView = itemView.findViewById(R.id.movementTypeTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
        }

        public void bind(MovementRecord movement, MovementListViewModel viewModel) {

            String displayMovementType;

            // Display movement type on the recycler view item
            String weather = movement.getWeather();
            // Format the movement type to include the weather set during the recording if it was set
            if(weather != null) {
                displayMovementType = movement.getWeather() + " " + movement.getMovementType();
            } else {
                displayMovementType = movement.getMovementType();
            }
            movementTypeTextView.setText(displayMovementType);

            // Display date on the recycler view item
            String formattedDatetime = "Date: " + viewModel.readableDatetime(movement.getRecordingDatetime());
            recordingDateTimeTextView.setText(formattedDatetime);

            // Display distance on the recycler view item
            String displayDistance = viewModel.formatDistance(movement.getDistance());
            distanceTextView.setText(displayDistance);
        }
    }
}
