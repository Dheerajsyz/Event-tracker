package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminPanelActivity extends AppCompatActivity {

    private static final String TAG = "AdminPanelActivity";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminPanelAdapter adminPanelAdapter;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adminPanelAdapter = new AdminPanelAdapter(this);
        viewPager.setAdapter(adminPanelAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("User Management");
                    break;
                case 1:
                    tab.setText("Pending Events");
                    break;
                case 2:
                    tab.setText("Event Management");
                    break;
            }
        }).attach();

        Log.d(TAG, "AdminPanelActivity launched");
        Toast.makeText(this, "Admin Panel Opened", Toast.LENGTH_SHORT).show();
    }
}
