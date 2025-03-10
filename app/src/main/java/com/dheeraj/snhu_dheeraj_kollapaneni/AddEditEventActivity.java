package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        String eventName = etEventName.getText().toString().trim();
        String eventLocation = etEventLocation.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String eventTime = etEventTime.getText().toString().trim();

        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(eventLocation) || TextUtils.isEmpty(eventDate) || TextUtils.isEmpty(eventTime)) {
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
