package com.example.gatecrash;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    private Button backBtn, setReminderBtn, cancelAlarmBtn;
    private RadioGroup reminderRadioGroup;
    private RadioButton radioTest, radio15, radio30, radio60;
    private TextView reminderStatusText, alarmDetailText;
    private TextView reminderFlightInfo, boardingTimeText;

    private DatabaseHelper dbHelper;
    private AlarmManager alarmManager;

    private int tripId = 1;
    private String flightNumber = "AI 302";
    private String airline = "Air India";
    private String airport = "Delhi (DEL) ➔ Mumbai (BOM)";
    private String terminal = "Terminal 2";
    private String gate = "Gate B24";
    private String departureTime = "18:30";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        dbHelper = new DatabaseHelper(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        int incomingTripId = getIntent().getIntExtra("trip_id", -1);
        if (incomingTripId != -1) {
            tripId = incomingTripId;
        }

        // View bindings
        backBtn = findViewById(R.id.backFromReminder);
        setReminderBtn = findViewById(R.id.setReminderBtn);
        cancelAlarmBtn = findViewById(R.id.cancelAlarmBtn);
        reminderRadioGroup = findViewById(R.id.reminderRadioGroup);
        radioTest = findViewById(R.id.radioTest);
        radio15 = findViewById(R.id.radio15);
        radio30 = findViewById(R.id.radio30);
        radio60 = findViewById(R.id.radio60);
        reminderStatusText = findViewById(R.id.reminderStatusText);
        alarmDetailText = findViewById(R.id.alarmDetailText);
        reminderFlightInfo = findViewById(R.id.reminderFlightInfo);
        boardingTimeText = findViewById(R.id.boardingTimeText);

        backBtn.setOnClickListener(v -> finish());

        // Load flight details from Database or Defaults
        loadFlightDetails();

        // Check if reminder already saved in SQLite
        loadExistingReminder();

        // Ask for Notification permission on Android 13+
        requestNotificationPermissionIfNeeded();

        // Set Alarm with AlarmManager
        setReminderBtn.setOnClickListener(v -> scheduleAlarmWithManager());

        // Cancel Alarm with AlarmManager
        cancelAlarmBtn.setOnClickListener(v -> cancelAlarmWithManager());
    }

    private void loadFlightDetails() {
        Cursor cursor = null;
        if (tripId != -1) {
            cursor = dbHelper.getTripById(tripId);
        }

        if (cursor == null || !cursor.moveToFirst()) {
            SharedPreferences prefs = getSharedPreferences("GateCrashSession", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", 1);
            cursor = dbHelper.getLatestTrip(userId);
        }

        if (cursor != null && cursor.moveToFirst()) {
            tripId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRIP_ID));
            airline = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRLINE));
            flightNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FLIGHT_NUMBER));
            airport = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AIRPORT));
            terminal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TERMINAL));
            gate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_GATE));
            departureTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DEPARTURE_TIME));
            cursor.close();
        }

        reminderFlightInfo.setText("✈️ " + flightNumber + " • " + terminal + ", " + gate);
        boardingTimeText.setText("Departure: " + departureTime + " (" + airport + ")");
    }

    private void loadExistingReminder() {
        Cursor cursor = dbHelper.getReminder(tripId);
        if (cursor != null && cursor.moveToFirst()) {
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_TIME));
            reminderStatusText.setText("⏰ Active Alarm: " + time);
            reminderStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
            alarmDetailText.setText("AlarmManager is actively watching this flight. An alert will fire at the scheduled time.");
            cursor.close();
        }
    }

    private void scheduleAlarmWithManager() {
        requestNotificationPermissionIfNeeded();

        int selectedId = reminderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadio = findViewById(selectedId);
        String optionText = selectedRadio != null ? selectedRadio.getText().toString() : "10 Seconds Test Alarm";

        long delayMillis;
        String intervalLabel;

        if (selectedId == R.id.radioTest) {
            delayMillis = 10 * 1000L; // 10 seconds for demo test
            intervalLabel = "10 Seconds Demo Test";
        } else if (selectedId == R.id.radio15) {
            delayMillis = 15 * 60 * 1000L; // 15 mins
            intervalLabel = "15 minutes before";
        } else if (selectedId == R.id.radio30) {
            delayMillis = 30 * 60 * 1000L; // 30 mins
            intervalLabel = "30 minutes before";
        } else if (selectedId == R.id.radio60) {
            delayMillis = 60 * 60 * 1000L; // 60 mins
            intervalLabel = "60 minutes before";
        } else {
            delayMillis = 10 * 1000L;
            intervalLabel = "10 Seconds Demo Test";
        }

        long triggerAtMillis = System.currentTimeMillis() + delayMillis;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String formattedTriggerTime = sdf.format(new Date(triggerAtMillis));

        // Create Intent directed to AlarmReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGER);
        intent.putExtra(AlarmReceiver.EXTRA_FLIGHT_NUMBER, flightNumber);
        intent.putExtra(AlarmReceiver.EXTRA_TERMINAL, terminal);
        intent.putExtra(AlarmReceiver.EXTRA_GATE, gate);
        intent.putExtra(AlarmReceiver.EXTRA_AIRPORT, airport);
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, "Boarding begins soon for " + flightNumber + " at " + gate + "!");
        intent.putExtra(AlarmReceiver.EXTRA_TRIP_ID, tripId);

        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, tripId, intent, pendingFlags);

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                        } else {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }

                // Save in SQLite
                String recordTime = intervalLabel + " (Trigger at " + formattedTriggerTime + ")";
                dbHelper.setReminder(tripId, recordTime, 1);

                // Update UI Status
                reminderStatusText.setText("⏰ Active Alarm: " + recordTime);
                reminderStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
                alarmDetailText.setText("✅ AlarmManager scheduled successfully! Alarm will sound at " + formattedTriggerTime + ".");

                Toast.makeText(this, "Alarm scheduled with AlarmManager! Trigger at " + formattedTriggerTime, Toast.LENGTH_LONG).show();

            } catch (SecurityException se) {
                // Graceful fallback if exact alarms restricted
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                String recordTime = intervalLabel + " (Approx " + formattedTriggerTime + ")";
                dbHelper.setReminder(tripId, recordTime, 1);
                reminderStatusText.setText("⏰ Active Alarm: " + recordTime);
                reminderStatusText.setTextColor(ContextCompat.getColor(this, R.color.success));
                alarmDetailText.setText("✅ AlarmManager set for " + formattedTriggerTime + ".");
                Toast.makeText(this, "Alarm set with AlarmManager!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cancelAlarmWithManager() {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGER);

        int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, tripId, intent, pendingFlags);

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        // Delete from SQLite
        dbHelper.deleteReminder(tripId);

        // Update UI
        reminderStatusText.setText("🚫 No active alarm set.");
        reminderStatusText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        alarmDetailText.setText("AlarmManager has been cancelled. No alarms will fire for this trip.");

        Toast.makeText(this, "Alarm cancelled successfully via AlarmManager!", Toast.LENGTH_SHORT).show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted for Boarding Alarms!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
