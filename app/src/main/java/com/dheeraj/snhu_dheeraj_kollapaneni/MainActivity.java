package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserRole = "user"; // Default role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the layout that includes the full navigation menu (activity_event_list.xml)
        setContentView(R.layout.activity_event_list);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserRole(user.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        // Force the Admin Panel menu item visible for debugging
        Menu menu = navigationView.getMenu();
        MenuItem adminItem = menu.findItem(R.id.nav_admin_panel);
        adminItem.setVisible(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_admin_panel) {
                    Log.d(TAG, "Admin Panel menu item clicked, currentUserRole = " + currentUserRole);
                    if ("admin".equalsIgnoreCase(currentUserRole)) {
                        Toast.makeText(MainActivity.this, "Opening Admin Panel", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, AdminPanelActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Error: You don't have admin permissions!", Toast.LENGTH_SHORT).show();
                    }
                    drawerLayout.closeDrawers();
                    return true;
                } else if (id == R.id.nav_events) {
                    startActivity(new Intent(MainActivity.this, EventListActivity.class));
                    drawerLayout.closeDrawers();
                    return true;
                } else if (id == R.id.nav_logout) {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    private void fetchUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if (role != null) {
                            currentUserRole = role;
                        }
                        Log.d(TAG, "Fetched user role: " + currentUserRole);
                        Toast.makeText(MainActivity.this, "User role: " + currentUserRole, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "User document does not exist");
                        Toast.makeText(MainActivity.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user role", e);
                    Toast.makeText(MainActivity.this, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
                });
    }
}
