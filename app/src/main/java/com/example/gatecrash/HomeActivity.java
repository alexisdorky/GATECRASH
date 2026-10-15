package com.example.gatecrash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button addFlightBtn, flightDashboardBtn;
    private Button liveFlightBtn, airportExplorerBtn, checklistBtn, reminderBtn;
    private TextView welcomeGreeting;
    private TextView flightNumberText, flightRouteText, flightTerminalText, flightGateText, flightTimeText;

    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;
    private int currentUserId = 1;
    private int latestTripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);

        currentUserId = sharedPreferences.getInt("userId", 1);
        String userName = sharedPreferences.getString("userName", "Passenger");

        welcomeGreeting = findViewById(R.id.welcomeGreeting);
        welcomeGreeting.setText("Welcome, " + userName);

        logoutButton = findViewById(R.id.logoutButton);
        addFlightBtn = findViewById(R.id.addFlightBtn);
        flightDashboardBtn = findViewById(R.id.flightDashboardBtn);
        liveFlightBtn = findViewById(R.id.liveFlightBtn);
        airportExplorerBtn = findViewById(R.id.airportExplorerBtn);
        checklistBtn = findViewById(R.id.checklistBtn);
        reminderBtn = findViewById(R.id.reminderBtn);

        flightNumberText = findViewById(R.id.flightNumberText);
        flightRouteText = findViewById(R.id.flightRouteText);
        flightTerminalText = findViewById(R.id.flightTerminalText);
        flightGateText = findViewById(R.id.flightGateText);
        flightTimeText = findViewById(R.id.flightTimeText);

        // Load latest flight from SQLite
        loadLatestFlight();

        // Logout
        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        addFlightBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddFlightActivity.class);
            startActivity(intent);
        });

        flightDashboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FlightDashboardActivity.class);
            intent.putExtra("trip_id", latestTripId);
            startActivity(intent);
        });

        liveFlightBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LiveFlightActivity.class);
            intent.putExtra("flight_number", flightNumberText.getText().toString());
            startActivity(intent);
        });

        airportExplorerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AirportExplorerActivity.class);
            startActivity(intent);
        });

        checklistBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ChecklistActivity.class);
            intent.putExtra("trip_id", latestTripId);
            startActivity(intent);
        });

        reminderBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReminderActivity.class);
            intent.putExtra("trip_id", latestTripId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when coming back from Add Flight
        loadLatestFlight();
    }

    private void loadLatestFlight() {
        Cursor cursor = dbHelper.getLatestTrip(currentUserId);
        if (cursor != null && cursor.moveToFirst()) {
            latestTripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
            String airline = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRLINE));
            String flightNo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FLIGHT_NUMBER));
            String airport = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRPORT));
            String terminal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TERMINAL));
            String gate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GATE));
            String depTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DEPARTURE_TIME));

            flightNumberText.setText(airline + " " + flightNo);
            flightRouteText.setText(airport);
            flightTerminalText.setText(terminal);
            flightGateText.setText(gate);
            flightTimeText.setText(depTime);
            cursor.close();
        } else {
            // Default demo trip if no trip added yet
            flightNumberText.setText("AI 302");
            flightRouteText.setText("Delhi (DEL)  ➔  Mumbai (BOM)");
            flightTerminalText.setText("Terminal 2");
            flightGateText.setText("Gate B24");
            flightTimeText.setText("18:30");
        }
    }
}
