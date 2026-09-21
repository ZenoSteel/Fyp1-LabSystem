package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * The main login screen that handles user authentication.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignup = findViewById(R.id.tvSignup);

        btnLogin.setOnClickListener(this::onClick);

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void onClick(View v) {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Special check for hardcoded Admin (for Demo purposes)
        if (username.equalsIgnoreCase("admin") && password.equals("123")) {
            Toast.makeText(MainActivity.this, getString(R.string.msg_logging_in, "Admin"), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("USER_NAME", "Admin");
            intent.putExtra("USER_ROLE", getString(R.string.role_admin));
            startActivity(intent);
            finish();
            return;
        }

        // Special check for hardcoded lecturer (for Demo purposes)
        if (username.equalsIgnoreCase("zaim") && password.equals("123")) {
            Toast.makeText(MainActivity.this, getString(R.string.msg_logging_in, "Zaim"), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("USER_NAME", "Zaim");
            intent.putExtra("USER_ROLE", getString(R.string.role_lecturer));
            startActivity(intent);
            finish();
            return;
        }

        // Attempt Cloud Auth via Firebase if available
        if (DatabaseManager.getInstance().isFirebaseAvailable()) {
            Toast.makeText(this, getString(R.string.msg_logging_in, username), Toast.LENGTH_SHORT).show();
            DatabaseManager.getInstance().loginUser(this, username, password, new DatabaseManager.AuthCallback() {
                @Override
                public void onSuccess(String user, String role) {
                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    intent.putExtra("USER_NAME", user);
                    intent.putExtra("USER_ROLE", role);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Fallback to local SharedPreferences
                    performLocalLogin(username, password);
                }
            });
            return;
        }

        performLocalLogin(username, password);
    }

    private void performLocalLogin(String username, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String savedPassword = prefs.getString(username, null);

        if (savedPassword != null && savedPassword.equals(password)) {
            String role = prefs.getString(username + "_role", getString(R.string.role_student));
            Toast.makeText(MainActivity.this, getString(R.string.msg_logging_in, username), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
            intent.putExtra("USER_NAME", username);
            intent.putExtra("USER_ROLE", role);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
        }
    }
}