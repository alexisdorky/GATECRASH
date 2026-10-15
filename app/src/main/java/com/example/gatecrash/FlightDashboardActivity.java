package com.example.gatecrash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FlightDashboardActivity extends AppCompatActivity {

    private TextView dashFlightNumber, dashAirline, dashAirport, dashTerminal, dashGate, dashDepartureTime;
    private Button backToHomeBtn;
    private Button dashLiveFlightBtn, dashAirportExplorerBtn, dashMapBtn, dashChecklistBtn, dashReminderBtn;
    private DatabaseHelper dbHelper;
    private int currentTripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_dashboard);

        dbHelper = new DatabaseHelper(this);

        dashFlightNumber = findViewById(R.id.dashFlightNumber);
        dashAirline = findViewById(R.id.dashAirline);
        dashAirport = findViewById(R.id.dashAirport);
        dashTerminal = findViewById(R.id.dashTerminal);
        dashGate = findViewById(R.id.dashGate);
        dashDepartureTime = findViewById(R.id.dashDepartureTime);

        backToHomeBtn = findViewById(R.id.backToHomeBtn);
        dashLiveFlightBtn = findViewById(R.id.dashLiveFlightBtn);
        dashAirportExplorerBtn = findViewById(R.id.dashAirportExplorerBtn);
        dashMapBtn = findViewById(R.id.dashMapBtn);
        dashChecklistBtn = findViewById(R.id.dashChecklistBtn);
        dashReminderBtn = findViewById(R.id.dashReminderBtn);

        // Check if data passed in intent extras
        Intent incomingIntent = getIntent();
        if (incomingIntent != null && incomingIntent.hasExtra("flight_number")) {
            currentTripId = incomingIntent.getIntExtra("trip_id", -1);
            String fNo = incomingIntent.getStringExtra("flight_number");
            String airline = incomingIntent.getStringExtra("airline");
            String airport = incomingIntent.getStringExtra("airport");
            String terminal = incomingIntent.getStringExtra("terminal");
            String gate = incomingIntent.getStringExtra("gate");
            String depTime = incomingIntent.getStringExtra("departure_time");

            if (fNo != null && !fNo.isEmpty()) dashFlightNumber.setText(fNo);
            if (airline != null && !airline.isEmpty()) dashAirline.setText(airline);
            if (airport != null && !airport.isEmpty()) dashAirport.setText(airport);
            if (terminal != null && !terminal.isEmpty()) dashTerminal.setText(terminal);
            if (gate != null && !gate.isEmpty()) dashGate.setText(gate);
            if (depTime != null && !depTime.isEmpty()) dashDepartureTime.setText(depTime);
        } else {
            // Otherwise, load latest trip from SQLite
            loadLatestTripFromDb();
        }

        // Navigation
        backToHomeBtn.setOnClickListener(v -> finish());

        dashLiveFlightBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FlightDashboardActivity.this, LiveFlightActivity.class);
            intent.putExtra("flight_number", dashFlightNumber.getText().toString());
            startActivity(intent);
        });

        dashAirportExplorerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FlightDashboardActivity.this, AirportExplorerActivity.class);
            startActivity(intent);
        });

        dashMapBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FlightDashboardActivity.this, MapActivity.class);
            intent.putExtra("airport_name", dashAirport.getText().toString());
            startActivity(intent);
        });

        dashChecklistBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FlightDashboardActivity.this, ChecklistActivity.class);
            intent.putExtra("trip_id", currentTripId);
            startActivity(intent);
        });

        dashReminderBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FlightDashboardActivity.this, ReminderActivity.class);
            intent.putExtra("trip_id", currentTripId);
            startActivity(intent);
        });
    }

    private void loadLatestTripFromDb() {
        SharedPreferences prefs = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", 1);

        Cursor cursor = dbHelper.getLatestTrip(userId);
        if (cursor != null && cursor.moveToFirst()) {
            currentTripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
            dashAirline.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRLINE)));
            dashFlightNumber.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FLIGHT_NUMBER)));
            dashAirport.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRPORT)));
            dashTerminal.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TERMINAL)));
            dashGate.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GATE)));
            dashDepartureTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DEPARTURE_TIME)));
            cursor.close();
        }
    }
}
