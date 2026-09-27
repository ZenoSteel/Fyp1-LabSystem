package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * The main login screen that handles user authentication.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        // Firebase Persistent Offline Session Check
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && DatabaseManager.getInstance().isFirebaseAvailable()) {
            String uid = auth.getCurrentUser().getUid();
            String emailPrefix = auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail().split("@")[0] : "User";
            String displayUser = emailPrefix.toUpperCase(Locale.US);

            DatabaseManager.getInstance().fetchUserRolePublic(uid, displayUser, new DatabaseManager.AuthCallback() {
                @Override
                public void onSuccess(String user, String role) {
                    if (getString(R.string.role_admin).equals(role)) {
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        intent.putExtra("USER_NAME", user);
                        intent.putExtra("USER_ROLE", role);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SecondPageActivity.class);
                        intent.putExtra("USER_NAME", user);
                        intent.putExtra("USER_ROLE", role);
                        startActivity(intent);
                    }
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    setContentView(R.layout.activity_main);
                    setupLoginUI();
                }
            });
            return;
        }

        setContentView(R.layout.activity_main);
        setupLoginUI();
    }

    private void setupLoginUI() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignup = findViewById(R.id.tvSignup);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);

        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                etPassword.setSelection(etPassword.getText().length());
            });
        }

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
                    String normalizedKey = user.toLowerCase(Locale.US);
                    getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit().putString(normalizedKey + "_role", role).apply();
                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    intent.putExtra("USER_NAME", user.toUpperCase(Locale.US));
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
        // Local login is disabled to prevent unencrypted password access.
        // All authentication must go through Firebase Cloud.
        Toast.makeText(MainActivity.this, getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
    }
}