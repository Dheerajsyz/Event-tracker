package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EventListActivity extends AppCompatActivity
        implements EventAdapter.OnEventClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EventListActivity";

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private List<Event> allEvents;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private FloatingActionButton fabAddEvent;
    private NavigationView navigationView;
    private String currentUserRole = "user"; // default role
    private boolean isAdmin = false;

    // HashMaps for search filtering (keys are date or location, values are lists of events)
    private HashMap<String, List<Event>> dateMap = new HashMap<>();
    private HashMap<String, List<Event>> locationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Drawer and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        allEvents = new ArrayList<>();
        // Initially assume not admin. We'll update later after we fetch the role.
        eventAdapter = new EventAdapter(eventList, this, false);
        recyclerView.setAdapter(eventAdapter);

        // Setup Search Button (from activity_event_list.xml)
        ImageButton btnSearchEvents = findViewById(R.id.btnSearchEvents);
        btnSearchEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchDialog();
            }
        });

        // Setup FloatingActionButton for adding events
        fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEditEventActivity.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            fetchUserRole(mAuth.getCurrentUser().getUid());
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchUserRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            currentUserRole = role;
                        }
                        isAdmin = "admin".equalsIgnoreCase(currentUserRole);
                        Log.d(TAG, "Fetched user role: " + currentUserRole);

                        // Update adapter so it knows if user is admin
                        eventAdapter.setAdmin(isAdmin);

                        // Update nav menu: hide admin panel if not admin
                        Menu menu = navigationView.getMenu();
                        MenuItem adminItem = menu.findItem(R.id.nav_admin_panel);
                        if(adminItem != null) {
                            adminItem.setVisible(isAdmin);
                        }

                        // Now fetch events based on role
                        fetchEvents();
                    } else {
                        Log.d(TAG, "User document does not exist");
                        fetchEvents(); // fallback to normal user
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventListActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching user role", e);
                    fetchEvents(); // fallback
                });
    }

    private void fetchEvents() {
        if (isAdmin) {
            // Admin sees ALL events
            db.collection("events").addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Toast.makeText(EventListActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                    return;
                }
                eventList.clear();
                allEvents.clear();
                if (snapshots != null) {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        eventList.add(event);
                        allEvents.add(event);
                    }
                }
                populateHashMaps();
                eventAdapter.notifyDataSetChanged();
            });
        } else {
            // Normal user sees only APPROVED events
            db.collection("events")
                    .whereEqualTo("status", "approved")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(EventListActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eventList.clear();
                        allEvents.clear();
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Event event = doc.toObject(Event.class);
                                event.setEventId(doc.getId());
                                eventList.add(event);
                                allEvents.add(event);
                            }
                        }
                        populateHashMaps();
                        eventAdapter.notifyDataSetChanged();
                    });
        }
    }

    // Build hashmaps from the master list (allEvents) for date and location filtering
    private void populateHashMaps() {
        dateMap.clear();
        locationMap.clear();
        for (Event event : allEvents) {
            String date = event.getDate();
            String location = event.getLocation();

            if (!dateMap.containsKey(date)) {
                dateMap.put(date, new ArrayList<>());
            }
            dateMap.get(date).add(event);

            if (!locationMap.containsKey(location)) {
                locationMap.put(location, new ArrayList<>());
            }
            locationMap.get(location).add(event);
        }
    }

    // Displays a dialog with two input fields for date and location filtering
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventListActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_search, null);
        builder.setView(dialogView);

        final EditText etSearchDate = dialogView.findViewById(R.id.etSearchDate);
        final EditText etSearchLocation = dialogView.findViewById(R.id.etSearchLocation);

        // Set up DatePicker for the search date field
        etSearchDate.setFocusable(false);
        etSearchDate.setClickable(true);
        etSearchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(EventListActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        etSearchDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        builder.setTitle("Search Events");
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String searchDate = etSearchDate.getText().toString().trim();
                String searchLocation = etSearchLocation.getText().toString().trim();
                performSearch(searchDate, searchLocation);
            }
        });
        builder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Clear search: restore original list of events
                eventList.clear();
                eventList.addAll(allEvents);
                eventAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Filters events using the hashmaps based on provided search parameters
    private void performSearch(String searchDate, String searchLocation) {
        List<Event> filteredEvents = new ArrayList<>();

        if (!searchDate.isEmpty() && !searchLocation.isEmpty()) {
            List<Event> eventsByDate = dateMap.get(searchDate);
            List<Event> eventsByLocation = locationMap.get(searchLocation);
            if (eventsByDate != null && eventsByLocation != null) {
                // Find the common events (by matching eventId)
                for (Event event : eventsByDate) {
                    for (Event ev : eventsByLocation) {
                        if (event.getEventId().equals(ev.getEventId())) {
                            filteredEvents.add(event);
                            break;
                        }
                    }
                }
            }
        } else if (!searchDate.isEmpty()) {
            List<Event> eventsByDate = dateMap.get(searchDate);
            if (eventsByDate != null) {
                filteredEvents.addAll(eventsByDate);
            }
        } else if (!searchLocation.isEmpty()) {
            List<Event> eventsByLocation = locationMap.get(searchLocation);
            if (eventsByLocation != null) {
                filteredEvents.addAll(eventsByLocation);
            }
        } else {
            // If both fields are empty, show all events
            filteredEvents.addAll(allEvents);
        }

        eventList.clear();
        eventList.addAll(filteredEvents);
        eventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClick(Event event) {
        Intent intent = new Intent(EventListActivity.this, AddEditEventActivity.class);
        intent.putExtra("event_id", event.getEventId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Event event) {
        db.collection("events").document(event.getEventId())
                .delete()
                .addOnSuccessListener(aVoid -> fetchEvents())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error deleting event", e);
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_panel) {
            startActivity(new Intent(EventListActivity.this, AdminPanelActivity.class));
        } else if (id == R.id.nav_events) {
            // Already here, or you can refresh
        } else if (id == R.id.nav_sms_alerts) {
            startActivity(new Intent(EventListActivity.this, SmsPermissionActivity.class));
        } else if (id == R.id.nav_logout) {
            confirmLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(EventListActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
