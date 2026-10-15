package com.example.gatecrash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class AddFlightActivity extends AppCompatActivity {

    // Only user input: Flight Number
    private EditText flightNumberInput;
    private Button btnDetectFlight;

    // Quick test chips
    private Button chipAi302, chip6e204, chipUk955, chipEk500, chipBa198, chipAa292;

    // Auto-resolved Flight Preview
    private MaterialCardView flightPreviewCard;
    private TextView previewAirlineAndNumber, previewStatusBadge, previewAircraft;
    private TextView previewOriginCode, previewOriginName, previewDestCode, previewDestName;
    private TextView previewScheduleTime, previewTerminalGate, previewBaggage, previewPlacesVerifiedText;

    // Actions
    private Button saveFlightBtn, cancelFlightBtn;

    private DatabaseHelper dbHelper;
    private int currentUserId = 1;
    private FlightInfo currentResolvedFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1);

        // Bind Single Input & Search
        flightNumberInput = findViewById(R.id.flightNumberInput);
        btnDetectFlight = findViewById(R.id.btnDetectFlight);

        // Bind Chips
        chipAi302 = findViewById(R.id.chipAi302);
        chip6e204 = findViewById(R.id.chip6e204);
        chipUk955 = findViewById(R.id.chipUk955);
        chipEk500 = findViewById(R.id.chipEk500);
        chipBa198 = findViewById(R.id.chipBa198);
        chipAa292 = findViewById(R.id.chipAa292);

        // Bind Resolved Flight Display
        flightPreviewCard = findViewById(R.id.flightPreviewCard);
        previewAirlineAndNumber = findViewById(R.id.previewAirlineAndNumber);
        previewStatusBadge = findViewById(R.id.previewStatusBadge);
        previewAircraft = findViewById(R.id.previewAircraft);
        previewOriginCode = findViewById(R.id.previewOriginCode);
        previewOriginName = findViewById(R.id.previewOriginName);
        previewDestCode = findViewById(R.id.previewDestCode);
        previewDestName = findViewById(R.id.previewDestName);
        previewScheduleTime = findViewById(R.id.previewScheduleTime);
        previewTerminalGate = findViewById(R.id.previewTerminalGate);
        previewBaggage = findViewById(R.id.previewBaggage);
        previewPlacesVerifiedText = findViewById(R.id.previewPlacesVerifiedText);

        saveFlightBtn = findViewById(R.id.saveFlightBtn);
        cancelFlightBtn = findViewById(R.id.cancelFlightBtn);

        // Chip click listeners
        chipAi302.setOnClickListener(v -> lookupFlight("AI 302"));
        chip6e204.setOnClickListener(v -> lookupFlight("6E 204"));
        chipUk955.setOnClickListener(v -> lookupFlight("UK 955"));
        chipEk500.setOnClickListener(v -> lookupFlight("EK 500"));
        chipBa198.setOnClickListener(v -> lookupFlight("BA 198"));
        chipAa292.setOnClickListener(v -> lookupFlight("AA 292"));

        // Search button click
        btnDetectFlight.setOnClickListener(v -> {
            String input = flightNumberInput.getText().toString().trim();
            if (TextUtils.isEmpty(input)) {
                Toast.makeText(this, "Please enter a Flight Number (e.g. AI 302)", Toast.LENGTH_SHORT).show();
                return;
            }
            lookupFlight(input);
        });

        // Live text change listener: automatically fetches details when valid flight number is entered
        flightNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (val.length() >= 4 && val.matches(".*\\d+.*")) {
                    lookupFlightSilent(val);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save & Track
        saveFlightBtn.setOnClickListener(v -> saveAndTrackFlight());
        cancelFlightBtn.setOnClickListener(v -> finish());

        // Default initial flight lookup on screen load so user sees full details immediately
        flightNumberInput.setText("AI 302");
        lookupFlight("AI 302");
    }

    private void lookupFlight(String flightNumber) {
        flightNumberInput.setText(flightNumber);
        flightNumberInput.setSelection(flightNumber.length());
        fetchAndDisplayFlightInfo(flightNumber, true);
    }

    private void lookupFlightSilent(String flightNumber) {
        fetchAndDisplayFlightInfo(flightNumber, false);
    }

    private void fetchAndDisplayFlightInfo(String flightNumber, boolean showToast) {
        previewStatusBadge.setText("📡 SCANNING RADAR...");
        LiveFlightTrackerService.trackFlight(this, flightNumber, (flightInfo, isLiveApi) -> {
            runOnUiThread(() -> {
                if (flightInfo == null) return;
                currentResolvedFlight = flightInfo;

                // Bind all fetched details
                previewAirlineAndNumber.setText(flightInfo.getAirlineName() + " • " + flightInfo.getFlightNumber());
                previewStatusBadge.setText(flightInfo.getStatus());
                previewAircraft.setText(flightInfo.getAircraft());

                // Parse IATA Origin & Destination codes
                String origin = flightInfo.getOriginAirport();
                String dest = flightInfo.getDestinationAirport();

                previewOriginCode.setText(extractCode(origin, "DEL"));
                previewOriginName.setText(extractShortName(origin));
                previewDestCode.setText(extractCode(dest, "BOM"));
                previewDestName.setText(extractShortName(dest));

                previewScheduleTime.setText(flightInfo.getDepartureTime() + " ➔ " + flightInfo.getArrivalTime());
                previewTerminalGate.setText(flightInfo.getTerminal() + " • " + flightInfo.getGate());
                previewBaggage.setText("🧳 Baggage Claim: " + flightInfo.getBaggage());

                if (flightInfo.isPlacesVerified()) {
                    previewPlacesVerifiedText.setText("📍 Google Places Verified: " + flightInfo.getPlacesVerifiedName());
                } else {
                    previewPlacesVerifiedText.setText("📍 Airport: " + flightInfo.getPlacesVerifiedName());
                }

                if (showToast) {
                    if (isLiveApi) {
                        Toast.makeText(this, "🟢 Live Radar: Found " + flightInfo.getAirlineName() + " " + flightInfo.getFlightNumber() + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Resolved " + flightInfo.getAirlineName() + " " + flightInfo.getFlightNumber() + " via Places API", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private String extractCode(String fullAirport, String fallback) {
        if (fullAirport == null) return fallback;
        int start = fullAirport.lastIndexOf('(');
        int end = fullAirport.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start) {
            return fullAirport.substring(start + 1, end).trim();
        }
        return fallback;
    }

    private String extractShortName(String fullAirport) {
        if (fullAirport == null) return "Airport";
        int comma = fullAirport.indexOf(',');
        if (comma != -1) {
            return fullAirport.substring(0, comma).trim();
        }
        return fullAirport;
    }

    private void saveAndTrackFlight() {
        if (currentResolvedFlight == null) {
            String flightNo = flightNumberInput.getText().toString().trim();
            if (TextUtils.isEmpty(flightNo)) {
                Toast.makeText(this, "Please enter Flight Number", Toast.LENGTH_SHORT).show();
                return;
            }
            lookupFlight(flightNo);
            return;
        }

        String airline = currentResolvedFlight.getAirlineName();
        String flightNo = currentResolvedFlight.getFlightNumber();
        String airport = currentResolvedFlight.getDestinationAirport();
        String date = currentResolvedFlight.getDate();
        String depTime = currentResolvedFlight.getDepartureTime();
        String arrTime = currentResolvedFlight.getArrivalTime();
        String terminal = currentResolvedFlight.getTerminal();
        String gate = currentResolvedFlight.getGate();

        // Save to SQLite Trips database
        long tripId = dbHelper.insertTrip(currentUserId, airline, flightNo, airport, date, depTime, arrTime, terminal, gate);

        if (tripId != -1) {
            // Automatically insert intelligent checklist tasks for this flight
            dbHelper.insertTask((int) tripId, "Check live flight radar for " + airline + " " + flightNo);
            dbHelper.insertTask((int) tripId, "Navigate to " + terminal + ", " + gate + " on airport map");
            dbHelper.insertTask((int) tripId, "Locate charging pods & Wi-Fi station");
            dbHelper.insertTask((int) tripId, "Explore airport dining & coffee near " + gate);
            dbHelper.insertTask((int) tripId, "Boarding gate closes 20 mins prior to " + depTime);

            Toast.makeText(this, "Flight " + flightNo + " confirmed & tracked!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AddFlightActivity.this, FlightDashboardActivity.class);
            intent.putExtra("trip_id", (int) tripId);
            intent.putExtra("airline", airline);
            intent.putExtra("flight_number", flightNo);
            intent.putExtra("airport", airport);
            intent.putExtra("date", date);
            intent.putExtra("departure_time", depTime);
            intent.putExtra("arrival_time", arrTime);
            intent.putExtra("terminal", terminal);
            intent.putExtra("gate", gate);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error saving flight to database.", Toast.LENGTH_SHORT).show();
        }
    }
}
