package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays ALL events in the system.
 * Admin can edit or delete them.
 */
public class EventManagementFragment extends Fragment {

    private static final String TAG = "EventManagementFragment";

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private List<Event> allEvents;

    public EventManagementFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAllEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        allEvents = new ArrayList<>();

        // Pass true for isAdmin so the adapter shows the delete/edit buttons
        eventAdapter = new EventAdapter(allEvents, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEditClick(Event event) {
                // Launch AddEditEventActivity so admin can edit
                Intent intent = new Intent(getContext(), AddEditEventActivity.class);
                intent.putExtra("event_id", event.getEventId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Event event) {
                // Delete the event from Firestore
                db.collection("events")
                        .document(event.getEventId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                            fetchAllEvents();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error deleting event", e);
                        });
            }
        }, true); // true = admin mode

        recyclerView.setAdapter(eventAdapter);

        // Fetch all events in Firestore
        fetchAllEvents();

        return view;
    }

    private void fetchAllEvents() {
        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        allEvents.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Fetched " + allEvents.size() + " total events.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch events", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching events", e);
                });
    }
}
