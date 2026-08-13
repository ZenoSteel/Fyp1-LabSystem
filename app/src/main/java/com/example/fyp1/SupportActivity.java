package com.example.fyp1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * SupportActivity provides users with ways to contact support and view office information.
 * It includes email and phone intents for direct communication.
 */
public class SupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Initialize components
        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardEmail = findViewById(R.id.cardEmail);
        CardView cardCall = findViewById(R.id.cardCall);

        // Handle back button click
        btnBack.setOnClickListener(v -> finish());

        // Email Intent Implementation
        cardEmail.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@labsystem.edu.my"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
            startActivity(Intent.createChooser(emailIntent, getString(R.string.support_chooser_title)));
        });

        // Phone Intent Implementation
        cardCall.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:+603-12345678"));
            startActivity(callIntent);
        });
    }
}
