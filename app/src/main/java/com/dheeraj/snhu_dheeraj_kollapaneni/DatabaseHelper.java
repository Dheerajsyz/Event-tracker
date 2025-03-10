package com.dheeraj.snhu_dheeraj_kollapaneni;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public DatabaseHelper() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Add a new user to Firestore
     */
    public Task<Void> addUser(String username, String email, String phone, String role) { // FIXED: Changed isAdmin (boolean) to role (String)
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("username", username);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", role); // FIXED: Now directly stores "user" or "admin"

        return firestore.collection("users").document(userId).set(user);
    }

    /**
     * Check if a user already exists (by username or email)
     */
    public Task<QuerySnapshot> checkUserExists(String username, String email) {
        return firestore.collection("users")
                .whereEqualTo("username", username)
                .whereEqualTo("email", email)
                .get();
    }

    /**
     * Add a new event (with location)
     */
    public Task<DocumentReference> addEvent(String eventName, String eventDate, String eventTime, String location) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        Map<String, Object> event = new HashMap<>();
        event.put("userId", userId);
        event.put("name", eventName);
        event.put("date", eventDate);
        event.put("time", eventTime);
        event.put("location", location);

        return firestore.collection("events").add(event);
    }

    /**
     * Update an existing event
     */
    public Task<Void> updateEvent(String eventId, String eventName, String eventDate, String eventTime, String location) {
        Map<String, Object> event = new HashMap<>();
        event.put("name", eventName);
        event.put("date", eventDate);
        event.put("time", eventTime);
        event.put("location", location);

        return firestore.collection("events").document(eventId).update(event);
    }

    /**
     * Delete an event from Firestore
     */
    public Task<Void> deleteEvent(String eventId) {
        return firestore.collection("events").document(eventId).delete();
    }
}
