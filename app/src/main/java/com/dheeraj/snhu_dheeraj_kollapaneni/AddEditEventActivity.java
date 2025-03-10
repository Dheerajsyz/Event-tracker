package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etEventName, etEventLocation, etEventDate, etEventTime;
    private Button btnSaveEvent;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String eventId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etEventName = findViewById(R.id.etEventName);
        etEventLocation = findViewById(R.id.etEventLocation);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        eventId = getIntent().getStringExtra("event_id");

        // ✅ Fix: Use proper input focus + hint hiding
        setupHintHandling(etEventName, "Enter Event Name");
        setupHintHandling(etEventLocation, "Enter Event Location");

        // ✅ Fix: Date and Time Pickers
        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    // ✅ Fix: Proper hint management
    private void setupHintHandling(EditText editText, String originalHint) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editText.setHint(""); // Remove hint when typing
                } else {
                    editText.setHint(originalHint); // Restore if empty
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ✅ Fix: Show Date Picker
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            etEventDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
        }, year, month, day);
        datePickerDialog.show();
    }

    // ✅ Fix: Show Time Picker
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            etEventTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    // ✅ Fix: Proper field validation
    private void saveEvent() {
        String eventName = etEventName.getText().toString().trim();
        String eventLocation = etEventLocation.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();

        // **Check for actual content instead of hint!**
        if (eventName.isEmpty() || eventLocation.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("userId", userId);
        eventMap.put("name", eventName);
        eventMap.put("location", eventLocation);
        eventMap.put("date", eventDate);
        eventMap.put("time", eventTime);

        if (eventId == null) {
            firestore.collection("events").add(eventMap)
                    .addOnSuccessListener(documentReference -> finish())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error adding event", Toast.LENGTH_SHORT).show());
        } else {
            firestore.collection("events").document(eventId)
                    .update(eventMap)
                    .addOnSuccessListener(aVoid -> finish())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating event", Toast.LENGTH_SHORT).show());
        }
    }
}
