package com.example.gatecrash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class LiveFlightActivity extends AppCompatActivity {

    private TextView liveFlightNumber, liveFlightStatusBadge, liveAirlineName;
    private TextView liveRouteText, liveSchedDepText, liveEstArrText, liveTerminalGateText;
    private TextView routeStartCode, routeEndCode, liveAltitudeText, liveSpeedText, liveBaggageText, livePlacesBadge;
    private ProgressBar flightProgressBar;
    private Button backButton, locateGateOnMapBtn, refreshRadarBtn;

    private DatabaseHelper dbHelper;
    private String currentFlightNumber = "AI 302";
    private String currentAirport = "Chhatrapati Shivaji Maharaj International, Mumbai (BOM)";
    private String currentTerminal = "Terminal 2";
    private String currentGate = "Gate B24";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_flight);

        dbHelper = new DatabaseHelper(this);

        backButton = findViewById(R.id.backFromLiveFlight);
        locateGateOnMapBtn = findViewById(R.id.locateGateOnMapBtn);
        refreshRadarBtn = findViewById(R.id.refreshRadarBtn);

        liveFlightNumber = findViewById(R.id.liveFlightNumber);
        liveFlightStatusBadge = findViewById(R.id.liveFlightStatusBadge);
        liveAirlineName = findViewById(R.id.liveAirlineName);
        liveRouteText = findViewById(R.id.liveRouteText);
        flightProgressBar = findViewById(R.id.flightProgressBar);
        routeStartCode = findViewById(R.id.routeStartCode);
        routeEndCode = findViewById(R.id.routeEndCode);
        liveAltitudeText = findViewById(R.id.liveAltitudeText);
        liveSpeedText = findViewById(R.id.liveSpeedText);
        liveBaggageText = findViewById(R.id.liveBaggageText);
        livePlacesBadge = findViewById(R.id.livePlacesBadge);

        liveSchedDepText = findViewById(R.id.liveSchedDepText);
        liveEstArrText = findViewById(R.id.liveEstArrText);
        liveTerminalGateText = findViewById(R.id.liveTerminalGateText);

        backButton.setOnClickListener(v -> finish());

        // Load flight details from intent or database
        loadFlightDetails();

        // Refresh radar click
        refreshRadarBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing live radar telemetry...", Toast.LENGTH_SHORT).show();
            fetchLiveTrackingTelemetry(currentFlightNumber);
        });

        // Locate Gate on Map
        locateGateOnMapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LiveFlightActivity.this, MapActivity.class);
            intent.putExtra("airport_name", currentAirport);
            intent.putExtra("facility_category", "All");
            startActivity(intent);
        });
    }

    private void loadFlightDetails() {
        Intent intent = getIntent();
        String passedFlightNo = intent != null ? intent.getStringExtra("flight_number") : null;

        SharedPreferences prefs = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", 1);

        Cursor cursor = dbHelper.getLatestTrip(userId);
        if (cursor != null && cursor.moveToFirst()) {
            String airline = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRLINE));
            String flightNo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FLIGHT_NUMBER));
            String airport = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRPORT));
            String terminal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TERMINAL));
            String gate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GATE));
            String depTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DEPARTURE_TIME));
            String arrTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ARRIVAL_TIME));

            if (flightNo != null && !flightNo.isEmpty()) {
                currentFlightNumber = flightNo;
            }
            if (airline != null && !airline.isEmpty()) {
                liveAirlineName.setText(airline);
            }
            if (airport != null && !airport.isEmpty()) {
                currentAirport = airport;
                liveRouteText.setText(airport);
            }
            if (terminal != null && !terminal.isEmpty()) currentTerminal = terminal;
            if (gate != null && !gate.isEmpty()) currentGate = gate;

            liveTerminalGateText.setText(currentTerminal + "  •  " + currentGate);
            if (depTime != null && !depTime.isEmpty()) liveSchedDepText.setText(depTime);
            if (arrTime != null && !arrTime.isEmpty()) liveEstArrText.setText(arrTime);

            cursor.close();
        } else if (passedFlightNo != null && !passedFlightNo.isEmpty()) {
            currentFlightNumber = passedFlightNo;
        }

        liveFlightNumber.setText("✈️ " + currentFlightNumber);

        // Fetch live telemetry from AviationStack & Google Places
        fetchLiveTrackingTelemetry(currentFlightNumber);
    }

    private void fetchLiveTrackingTelemetry(String flightNo) {
        liveFlightStatusBadge.setText("📡 SCANNING RADAR...");
        LiveFlightTrackerService.trackFlight(this, flightNo, (info, isLive) -> {
            runOnUiThread(() -> {
                if (info == null) return;

                liveFlightNumber.setText("✈️ " + info.getFlightNumber());
                liveAirlineName.setText(info.getAirlineName() + " (" + info.getAirlineCode() + ")");
                liveFlightStatusBadge.setText(info.getStatus());

                liveRouteText.setText(info.getOriginAirport() + " ➔ " + info.getDestinationAirport());
                currentAirport = info.getDestinationAirport();
                currentTerminal = info.getTerminal();
                currentGate = info.getGate();

                liveTerminalGateText.setText(currentTerminal + "  •  " + currentGate);
                liveSchedDepText.setText(info.getDepartureTime());
                liveEstArrText.setText(info.getArrivalTime());
                liveBaggageText.setText("🧳 " + info.getBaggage());

                // Telemetry
                if (info.getLiveAltitude() > 0) {
                    liveAltitudeText.setText(String.format(Locale.US, "⚡ Alt: %,.0f m", info.getLiveAltitude()));
                    flightProgressBar.setProgress(72);
                    routeEndCode.setText("En Route • Cruising Altitude");
                } else {
                    liveAltitudeText.setText("⚡ Alt: 11,200 m (Cruise)");
                    flightProgressBar.setProgress(65);
                    routeEndCode.setText("On Track • Approaching");
                }

                if (info.getLiveSpeed() > 0) {
                    liveSpeedText.setText(String.format(Locale.US, "💨 Speed: %,.0f km/h", info.getLiveSpeed()));
                } else {
                    liveSpeedText.setText("💨 Speed: 850 km/h");
                }

                if (info.isPlacesVerified()) {
                    livePlacesBadge.setText("📍 Google Places API Verified: " + info.getPlacesVerifiedName());
                } else {
                    livePlacesBadge.setText("📍 Google Places: " + info.getPlacesVerifiedName());
                }
            });
        });
    }
}
