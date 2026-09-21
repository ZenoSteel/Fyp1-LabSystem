package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that allows new users to register by saving credentials to SharedPreferences.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etSignupUsername);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        setupRoleSpinner();

        btnSignup.setOnClickListener(v -> registerUser());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void setupRoleSpinner() {
        String[] roles = {getString(R.string.role_student), getString(R.string.role_lecturer), getString(R.string.role_admin)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Firebase requires password to be at least 6 characters.", Toast.LENGTH_LONG).show();
        }

        // Save locally to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(username, password);
        editor.putString(username + "_role", role);
        editor.apply();

        // Attempt Cloud Registration via Firebase if available
        if (DatabaseManager.getInstance().isFirebaseAvailable()) {
            DatabaseManager.getInstance().registerUser(this, username, password, role, new DatabaseManager.AuthCallback() {
                @Override
                public void onSuccess(String user, String r) {
                    Toast.makeText(SignupActivity.this, getString(R.string.msg_signup_success) + " (Cloud)", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(SignupActivity.this, "Cloud Auth Info: " + errorMessage, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            return;
        }

        Toast.makeText(this, getString(R.string.msg_signup_success), Toast.LENGTH_LONG).show();
        finish();
    }
}
