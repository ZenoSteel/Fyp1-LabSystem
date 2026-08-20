package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A splash/loading screen that appears between Login and the Dashboard.
 * Simulates system initialization.
 */
public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        TextView tvStatus = findViewById(R.id.tvLoadingStatus);

        // Capture data from MainActivity
        String username = getIntent().getStringExtra("USER_NAME");
        String role = getIntent().getStringExtra("USER_ROLE");

        // First Phase: AI Initialization
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (tvStatus != null) {
                tvStatus.setText(getString(R.string.msg_loading_sync));
            }
        }, 1200);

        // Second Phase: Transition to appropriate Dashboard based on Role
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (getString(R.string.role_admin).equals(role)) {
                intent = new Intent(LoadingActivity.this, AdminActivity.class);
            } else {
                intent = new Intent(LoadingActivity.this, SecondPageActivity.class);
            }
            
            intent.putExtra("USER_NAME", username);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
            finish(); // Remove from back stack
        }, 2500);
    }
}