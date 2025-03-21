package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.List;

public class EventListActivity extends AppCompatActivity
        implements EventAdapter.OnEventClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EventListActivity";

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private FloatingActionButton fabAddEvent;
    private NavigationView navigationView;
    private String currentUserRole = "user"; // default role
    private boolean isAdmin = false;

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
        // Initially assume not admin. We'll update later after we fetch the role.
        eventAdapter = new EventAdapter(eventList, this, false);
        recyclerView.setAdapter(eventAdapter);

        // Setup FloatingActionButton
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
                        adminItem.setVisible(isAdmin);

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
                if (snapshots != null) {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Event event = doc.toObject(Event.class);
                        event.setEventId(doc.getId());
                        eventList.add(event);
                    }
                }
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
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                Event event = doc.toObject(Event.class);
                                event.setEventId(doc.getId());
                                eventList.add(event);
                            }
                        }
                        eventAdapter.notifyDataSetChanged();
                    });
        }
    }

    @Override
    public void onEditClick(Event event) {
        // Only called if user sees an Edit button (i.e., if isAdmin is true
        // or you haven't hidden the button for normal users).
        Intent intent = new Intent(EventListActivity.this, AddEditEventActivity.class);
        intent.putExtra("event_id", event.getEventId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Event event) {
        // Only admins see the delete button
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
            // Admin only
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
