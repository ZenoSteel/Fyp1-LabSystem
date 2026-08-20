package com.example.fyp1;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that displays the user's profile information.
 */
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvRole = findViewById(R.id.tvProfileRole);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);

        // Get data from Intent
        String username = getIntent().getStringExtra("USER_NAME");
        String role = getIntent().getStringExtra("USER_ROLE");

        if (username != null) {
            tvName.setText(username.toUpperCase());
            tvEmail.setText(getString(R.string.placeholder_email, username.toLowerCase()));
        }

        if (role != null) {
            tvRole.setText(role);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}