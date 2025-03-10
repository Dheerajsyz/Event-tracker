package com.dheeraj.snhu_dheeraj_kollapaneni;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class SmsPermissionActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 123;

    private Button btnEnableSms;
    private TextView tvStatusMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Set up the default back button in the action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);  // Enable the up button

        // Initialize views
        btnEnableSms = findViewById(R.id.btnEnableSms);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);

        // Initialize button state based on permission
        updateButtonState();

        // Set button click listener
        btnEnableSms.setOnClickListener(v -> handleEnableSms());
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button press in the toolbar
        onBackPressed();  // Navigate back to the previous activity
        return true;
    }

    private boolean isSmsPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("SetTextI18n")
    private void updateButtonState() {
        if (isSmsPermissionGranted()) {
            btnEnableSms.setEnabled(false);  // Disable the button
            btnEnableSms.setText("SMS Notifications Enabled");  // Change button text
            tvStatusMessage.setText("SMS notifications are already enabled.");
        } else {
            btnEnableSms.setEnabled(true);  // Enable the button
            btnEnableSms.setText("Enable SMS Notifications");
            tvStatusMessage.setText("");
        }
    }

    private void handleEnableSms() {
        if (!isSmsPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
            updateButtonState();  // Refresh the button state after permission result
        }
    }
}
