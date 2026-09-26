package com.example.fyp1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LecturerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_detail);

        Lecturer lecturer = (Lecturer) getIntent().getSerializableExtra("LECTURER_DATA");

        if (lecturer == null) {
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView ivProfile = findViewById(R.id.ivDetailProfile);
        View viewStatus = findViewById(R.id.viewStatusIndicator);
        TextView tvStatus = findViewById(R.id.tvStatusText);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvOffice = findViewById(R.id.tvDetailOffice);
        TextView tvLab = findViewById(R.id.tvDetailLab);
        TextView tvPhone = findViewById(R.id.tvDetailPhone);
        Button btnWhatsApp = findViewById(R.id.btnContactWhatsApp);

        btnBack.setOnClickListener(v -> finish());

        // Set Data
        ivProfile.setImageResource(lecturer.getProfileImageResId());
        tvName.setText(lecturer.getName());
        tvOffice.setText(lecturer.getOffice());
        tvLab.setText(lecturer.getLabCoordinator());
        tvPhone.setText(lecturer.getPhone());

        if (lecturer.isAvailable()) {
            viewStatus.setBackgroundColor(getResources().getColor(R.color.status_available));
            tvStatus.setText(getString(R.string.status_in_office));
        } else {
            viewStatus.setBackgroundColor(getResources().getColor(R.color.status_not_available));
            tvStatus.setText(getString(R.string.status_not_in_office));
        }

        btnWhatsApp.setOnClickListener(v -> {
            String url = "https://api.whatsapp.com/send?phone=" + lecturer.getPhone();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }
}