package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that allows new users to register by saving credentials to SharedPreferences.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etSignupUsername);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSignup.setOnClickListener(v -> registerUser());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences (Diploma Level "Local Database")
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(username, password); // Store username as KEY and password as VALUE
        editor.apply();

        Toast.makeText(this, getString(R.string.msg_signup_success), Toast.LENGTH_LONG).show();
        finish(); // Go back to login screen
    }
}