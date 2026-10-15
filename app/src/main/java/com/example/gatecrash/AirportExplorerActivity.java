package com.example.gatecrash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AirportExplorerActivity extends AppCompatActivity {

    private AutoCompleteTextView facilitySearchInput;
    private TextView placesStatusSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_explorer);

        Button backBtn = findViewById(R.id.backFromExplorer);
        placesStatusSubtitle = findViewById(R.id.placesStatusSubtitle);
        facilitySearchInput = findViewById(R.id.facilitySearchInput);

        Button btnRestrooms = findViewById(R.id.btnRestrooms);
        Button btnCharging = findViewById(R.id.btnCharging);
        Button btnFood = findViewById(R.id.btnFood);
        Button btnLounge = findViewById(R.id.btnLounge);
        Button btnShopping = findViewById(R.id.btnShopping);
        Button btnMedical = findViewById(R.id.btnMedical);
        Button openMapBtn = findViewById(R.id.openMapDirectlyBtn);

        if (PlacesManager.isApiKeyConfigured(this)) {
            placesStatusSubtitle.setText("🟢 Google Places & GPS Active • Terminal 2");
        } else {
            placesStatusSubtitle.setText("📍 Airport Places & GPS Active • Terminal 2");
        }

        backBtn.setOnClickListener(v -> finish());

        // Setup facility search autocomplete
        setupSearchAutocomplete();

        // Category click handlers
        View.OnClickListener categoryClick = v -> {
            String category = "All";
            int id = v.getId();
            if (id == R.id.btnRestrooms) category = "Restrooms";
            else if (id == R.id.btnCharging) category = "Charging Stations";
            else if (id == R.id.btnFood) category = "Food & Dining";
            else if (id == R.id.btnLounge) category = "Airport Lounges";
            else if (id == R.id.btnShopping) category = "Duty Free & Shopping";
            else if (id == R.id.btnMedical) category = "Medical Center";

            Toast.makeText(this, "Opening map for " + category + "...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AirportExplorerActivity.this, MapActivity.class);
            intent.putExtra("facility_category", category);
            startActivity(intent);
        };

        btnRestrooms.setOnClickListener(categoryClick);
        btnCharging.setOnClickListener(categoryClick);
        btnFood.setOnClickListener(categoryClick);
        btnLounge.setOnClickListener(categoryClick);
        btnShopping.setOnClickListener(categoryClick);
        btnMedical.setOnClickListener(categoryClick);

        openMapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AirportExplorerActivity.this, MapActivity.class);
            intent.putExtra("facility_category", "All");
            startActivity(intent);
        });
    }

    private void setupSearchAutocomplete() {
        List<AirportFacility> facilities = AirportDataRepository.getFacilitiesForAirport(AirportDataRepository.DEFAULT_AIRPORT_LOCATION);
        List<String> facilityNames = new ArrayList<>();
        for (AirportFacility f : facilities) {
            facilityNames.add(f.getIconEmoji() + " " + f.getName() + " (" + f.getCategory() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, facilityNames);
        facilitySearchInput.setAdapter(adapter);
        facilitySearchInput.setThreshold(1);

        facilitySearchInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = adapter.getItem(position);
            if (selectedItem != null) {
                // Find matching category
                String targetCategory = "All";
                for (AirportFacility f : facilities) {
                    if (selectedItem.contains(f.getName())) {
                        targetCategory = f.getCategory();
                        break;
                    }
                }
                Intent intent = new Intent(AirportExplorerActivity.this, MapActivity.class);
                intent.putExtra("facility_category", targetCategory);
                startActivity(intent);
            }
        });
    }
}
