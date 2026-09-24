package com.example.fyp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LecturerListActivity extends AppCompatActivity {

    private RecyclerView rvLecturers;
    private Spinner spinnerOffice;
    private LecturerAdapter adapter;
    private List<Lecturer> allLecturers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_list);

        rvLecturers = findViewById(R.id.rvLecturers);
        spinnerOffice = findViewById(R.id.spinnerOffice);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        setupMockData();
        setupRecyclerView();
        setupSpinner();
    }

    private void setupMockData() {
        allLecturers = new ArrayList<>();
        // Office 1
        allLecturers.add(new Lecturer("Encik Ashar", "Office 1", "012-345 6789", "MML", "4:00 PM", true, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Encik Sharafi", "Office 1", "012-342 6789", "DCN", "2:00 PM", false, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Encik Dzulkipli", "Office 1", "012-345 6789", "NWL", "10:00 AM", true, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Encik Nizam", "Office 1", "012-345 6789", "DNS", "11:00 AM", true, android.R.drawable.ic_menu_myplaces));
        
        // Office 2
        allLecturers.add(new Lecturer("Encik Carles", "Office 2", "012-342 6789", "PGL1", "3:00 PM", false, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Encik Yusof", "Office 2", "012-345 6789", "PGL2", "9:00 AM", true, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Puan Siti", "Office 2", "012-345 6789", "HYP", "1:00 PM", true, android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Puan Aminah", "Office 2", "012-345 6789", "APL", "5:00 PM", true, android.R.drawable.ic_menu_myplaces));
    }

    private void setupRecyclerView() {
        rvLecturers.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new LecturerAdapter(new ArrayList<>(allLecturers), lecturer -> {
            Intent intent = new Intent(LecturerListActivity.this, LecturerDetailActivity.class);
            intent.putExtra("LECTURER_DATA", lecturer);
            startActivity(intent);
        });
        rvLecturers.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] offices = {
                getString(R.string.all_offices),
                getString(R.string.office_1),
                getString(R.string.office_2)
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, offices);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOffice.setAdapter(spinnerAdapter);

        spinnerOffice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterLecturers(offices[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterLecturers(String office) {
        if (office.equals(getString(R.string.all_offices))) {
            adapter.updateList(allLecturers);
        } else {
            List<Lecturer> filteredList = new ArrayList<>();
            for (Lecturer l : allLecturers) {
                if (l.getOffice().equals(office)) {
                    filteredList.add(l);
                }
            }
            adapter.updateList(filteredList);
        }
    }
}