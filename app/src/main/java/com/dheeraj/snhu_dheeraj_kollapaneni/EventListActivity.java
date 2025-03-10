package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener, NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private FloatingActionButton fabAddEvent;

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // FIXED: Uses navi.xml for Navigation Drawer

        auth = FirebaseAuth.getInstance();

        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Enable Hamburger Menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(eventAdapter);

        // FIXED: Ensure FloatingActionButton opens AddEditEventActivity
        fabAddEvent = findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEditEventActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        fetchEvents();
    }

    private void fetchEvents() {
        eventsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(EventListActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots == null || snapshots.isEmpty()) {
                eventList.clear();
                eventAdapter.notifyDataSetChanged();
                return;
            }

            eventList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Event event = doc.toObject(Event.class);
                event.setEventId(doc.getId());
                eventList.add(event);
            }
            eventAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onEditClick(Event event) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("event_id", event.getEventId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Event event) {
        eventsRef.document(event.getEventId()).delete()
                .addOnSuccessListener(aVoid -> fetchEvents())
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_logout) {
            auth.signOut();
            startActivity(new Intent(EventListActivity.this, LoginActivity.class));
            finish();
        } else if (item.getItemId() == R.id.nav_sms_alerts) {
            startActivity(new Intent(EventListActivity.this, SmsPermissionActivity.class));
        } else if (item.getItemId() == R.id.nav_view_events) {
            startActivity(new Intent(EventListActivity.this, EventListActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
