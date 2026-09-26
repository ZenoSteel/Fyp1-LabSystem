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
        
        // 1. Top Management (Always displayed first when "All Lecturers" is selected)
        allLecturers.add(new Lecturer("NAZRINA BINTI BAKAR\n(Ketua Jabatan)", "Management", "012-345 6789", "JTMK", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SAFINAH BINTI NAWAWI\n(Pen. Ketua Jabatan)", "Management", "012-345 6789", "JTMK", android.R.drawable.ic_menu_myplaces));

        // 2. Programming Section (Left Side of Org Chart)
        String prog = getString(R.string.office_1); // "Programming"
        allLecturers.add(new Lecturer("Ts. NG BOON DING\n(Ketua Program)", prog, "012-345 6789", "DNS", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Dr. ZAKIR HUSSAIN BIN IBRAHIM", prog, "012-345 6789", "DNS", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("FARRAH WAHEDA BINTI ABDULLAH", prog, "012-345 6789", "PGL 1", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SABRINA BINTI MILAN", prog, "012-345 6789", "PGL 2", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("Dr. CHEN MEI CHING", prog, "012-345 6789", "MML", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NUR YAHZELINA BINTI HANAFI", prog, "012-345 6789", "APL", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("KEDUNG FLETCHER", prog, "012-345 6789", "HYP", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NORAIMI BINTI MAHRAN", prog, "012-345 6789", "DNS", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NORFAZILAH BINTI MOHAMAD PON", prog, "012-345 6789", "PGL 1", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SURAFAH BINTI MOS", prog, "012-345 6789", "PGL 2", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("ANIZAWATI BINTI MUKHTAR", prog, "012-345 6789", "MML", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NUREDZAN BINTI ZALUDIN", prog, "012-345 6789", "APL", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("HERASUHANTI BINTI HERMAIN", prog, "012-345 6789", "HYP", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("AZHAR BIN ABD HAMID", prog, "012-345 6789", "DNS", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("ANDING NYUAK", prog, "012-345 6789", "PGL 1", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("AZIZAH BINTI OHAMZAH", prog, "012-345 6789", "PGL 2", android.R.drawable.ic_menu_myplaces));

        // 3. Networking Section (Right Side of Org Chart)
        String netw = getString(R.string.office_2); // "Networking"
        allLecturers.add(new Lecturer("ROHAYA BINTI MOHAMAD\n(Ketua Program)", netw, "012-345 6789", "DCN", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SHARAFI BIN MOHAMED YUSOFF", netw, "012-345 6789", "NWL", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("JOHARI AHMAD BIN GHAZALI", netw, "012-345 6789", "CSD", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("MOHAMAD NIZAM BIN RAHADI", netw, "012-345 6789", "Network Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SITI FATIA BINTI MOHAMAD ALI", netw, "012-345 6789", "SAD Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NORBAIZURA BINTI MOKHTAR", netw, "012-345 6789", "DCN", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SITI NOOR AISHAH BINTI MOHAMMAD", netw, "012-345 6789", "NWL", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SANJAY CHARLES ALBERT", netw, "012-345 6789", "CSD", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("DZULKIPLI BIN MARASAN", netw, "012-345 6789", "Network Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("WAHIDAH BINTI ANUAR", netw, "012-345 6789", "SAD Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("NURHAZURA BINTI HASSAN", netw, "012-345 6789", "DCN", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("AFZAN BINTI OMAR", netw, "012-345 6789", "NWL", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("SYAZWANI BINTI ABDUL KADIR", netw, "012-345 6789", "CSD", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("RAZELI BIN SANI", netw, "012-345 6789", "Network Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("HASBULLAH BIN ABDULLAH", netw, "012-345 6789", "SAD Lab", android.R.drawable.ic_menu_myplaces));
        allLecturers.add(new Lecturer("TANG SI KING", netw, "012-345 6789", "DCN", android.R.drawable.ic_menu_myplaces));
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
                if (l.getOffice().equals("Management") || l.getOffice().equals(office)) {
                    filteredList.add(l);
                }
            }
            adapter.updateList(filteredList);
        }
    }
}