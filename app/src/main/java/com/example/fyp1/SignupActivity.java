package com.example.fyp1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that allows new users to register by saving credentials to SharedPreferences.
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etClassSection;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etSignupUsername);
        etPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etClassSection = findViewById(R.id.etClassSection);
        spinnerRole = findViewById(R.id.spinnerRole);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        setupRoleSpinner();

        ImageButton btnToggleSignupPassword = findViewById(R.id.btnToggleSignupPassword);
        ImageButton btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        if (btnToggleSignupPassword != null) {
            btnToggleSignupPassword.setOnClickListener(v -> toggleVisibility(etPassword));
        }
        if (btnToggleConfirmPassword != null) {
            btnToggleConfirmPassword.setOnClickListener(v -> toggleVisibility(etConfirmPassword));
        }

        btnSignup.setOnClickListener(v -> registerUser());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void toggleVisibility(EditText editText) {
        if (editText == null) return;
        if (editText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        editText.setSelection(editText.getText().length());
    }

    private void setupRoleSpinner() {
        String[] roles = {getString(R.string.role_student), getString(R.string.role_lecturer), getString(R.string.role_admin)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selected = roles[position];
                if (getString(R.string.role_student).equals(selected)) {
                    if (etClassSection != null) etClassSection.setVisibility(android.view.View.VISIBLE);
                } else {
                    if (etClassSection != null) etClassSection.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String className = (etClassSection != null && etClassSection.getVisibility() == View.VISIBLE) ?
                etClassSection.getText().toString().trim() : "";

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

        // Save metadata locally to SharedPreferences (DO NOT SAVE PASSWORD)
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(username + "_role", role);
        if (!className.isEmpty()) editor.putString(username + "_class", className);
        editor.apply();

        // Auto-populate student's schedule with master timetable classes matching their class/section
        if (getString(R.string.role_student).equals(role)) {
            autoPopulateStudentSchedule(className);
        }

        // Attempt Cloud Registration via Firebase if available
        if (DatabaseManager.getInstance().isFirebaseAvailable()) {
            DatabaseManager.getInstance().registerUser(this, username, password, role, className, new DatabaseManager.AuthCallback() {
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

    private void autoPopulateStudentSchedule(String studentClass) {
        java.util.List<ClassSession> master = ScheduleManager.getMasterTimetable(this);
        java.util.List<ClassSession> studentSchedule = new java.util.ArrayList<>();

        for (ClassSession cs : master) {
            if (studentClass.isEmpty() || cs.getClassName().equalsIgnoreCase(studentClass)) {
                studentSchedule.add(cs);
            }
        }

        if (!studentSchedule.isEmpty()) {
            ScheduleManager.saveStudentSchedule(this, studentSchedule);
        }
    }
}
