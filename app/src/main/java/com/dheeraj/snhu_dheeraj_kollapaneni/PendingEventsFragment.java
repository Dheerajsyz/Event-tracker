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

public class PendingEventsFragment extends Fragment {

    private static final String TAG = "PendingEventsFragment";
    private TextView tvNoPending;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FirebaseFirestore db;
    private List<Event> pendingEvents;

    public PendingEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_events, container, false);

        tvNoPending = view.findViewById(R.id.tvNoPending);
        recyclerView = view.findViewById(R.id.recyclerViewPendingEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        pendingEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(pendingEvents, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEditClick(Event event) {
                // Admin might not edit pending events here, but you can handle if needed
            }

            @Override
            public void onDeleteClick(Event event) {
                // "Delete" is effectively "reject" in this scenario
                handleEventAction(event, "reject");
            }
        });
        recyclerView.setAdapter(eventAdapter);

        fetchPendingEvents();
        return view;
    }

    private void fetchPendingEvents() {
        db.collection("events")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    pendingEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        pendingEvents.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();

                    if (pendingEvents.isEmpty()) {
                        tvNoPending.setVisibility(View.VISIBLE);
                    } else {
                        tvNoPending.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Fetched " + pendingEvents.size() + " pending events");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch pending events", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching pending events", e);
                });
    }

    private void handleEventAction(Event event, String action) {
        if (action.equals("reject")) {
            db.collection("events").document(event.getEventId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Event rejected", Toast.LENGTH_SHORT).show();
                        fetchPendingEvents();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to reject event", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error rejecting event", e);
                    });
        }
        // You could add an "approve" path here if you like
    }
}
