package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class EventManagementFragment extends Fragment {

    private static final String TAG = "EventManagementFragment";
    private TextView tvNoEvents;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private List<Event> allEvents;

    public EventManagementFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);

        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        recyclerView = view.findViewById(R.id.recyclerViewAllEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        allEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(allEvents, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEditClick(Event event) {
                // Admin can edit an event. If you want to reuse AddEditEventActivity, do so:
                // Example:
                // Intent intent = new Intent(getContext(), AddEditEventActivity.class);
                // intent.putExtra("event_id", event.getEventId());
                // startActivity(intent);
            }

            @Override
            public void onDeleteClick(Event event) {
                deleteEvent(event);
            }
        });
        recyclerView.setAdapter(eventAdapter);

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

                    if (allEvents.isEmpty()) {
                        tvNoEvents.setVisibility(View.VISIBLE);
                    } else {
                        tvNoEvents.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Fetched " + allEvents.size() + " total events");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch events", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching events", e);
                });
    }

    private void deleteEvent(Event event) {
        db.collection("events").document(event.getEventId())
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
}
