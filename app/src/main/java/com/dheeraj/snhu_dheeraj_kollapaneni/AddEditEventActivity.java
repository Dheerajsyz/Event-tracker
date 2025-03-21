package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etEventName, etEventLocation, etEventDate, etEventTime;
    private Button btnSaveEvent;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Calendar calendar;

    private String eventId;         // Will be non-null if we are editing
    private boolean isEditMode;     // True if editing, false if creating new

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        etEventName = findViewById(R.id.etEventName);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        calendar = Calendar.getInstance();

        // Check if an event_id was passed in the Intent
        eventId = getIntent().getStringExtra("event_id");
        isEditMode = (eventId != null);

        if (isEditMode) {
            // If editing, load the existing event data from Firestore
            loadEventData(eventId);
        }

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void loadEventData(@NonNull String eventId) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        // Populate the fields
                        String name = documentSnapshot.getString("name");
                        String location = documentSnapshot.getString("location");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("time");

                        etEventName.setText(name);
                        etEventLocation.setText(location);
                        etEventDate.setText(date);
                        etEventTime.setText(time);
                    } else {
                        Toast.makeText(AddEditEventActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddEditEventActivity.this, "Failed to load event data", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etEventDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            etEventTime.setText(hourOfDay + ":" + minute);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void saveEvent() {
        String eventName = etEventName.getText().toString().trim();
        String eventLocation = etEventLocation.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();

        if (eventName.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Prepare the event data
        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("location", eventLocation);
        event.put("date", eventDate);
        event.put("time", eventTime);

        if (isEditMode) {
            // If editing, just update the existing document
            db.collection("events")
                    .document(eventId)
                    .update(event)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddEditEventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddEditEventActivity.this, "Failed to update event", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If creating new, also set status and userId
            event.put("userId", currentUser.getUid());
            event.put("status", "pending");  // newly created events are "pending" admin approval

            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddEditEventActivity.this, "Event submitted for approval", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddEditEventActivity.this, "Failed to submit event", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
