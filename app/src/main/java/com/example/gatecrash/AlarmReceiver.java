package com.example.gatecrash;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * AlarmReceiver triggered by Android AlarmManager when a flight boarding alarm fires.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";
    public static final String ACTION_ALARM_TRIGGER = "com.example.gatecrash.ACTION_ALARM_TRIGGER";

    public static final String EXTRA_FLIGHT_NUMBER = "extra_flight_number";
    public static final String EXTRA_AIRPORT = "extra_airport";
    public static final String EXTRA_TERMINAL = "extra_terminal";
    public static final String EXTRA_GATE = "extra_gate";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_TRIP_ID = "extra_trip_id";

    public static final String CHANNEL_ID = "gatecrash_alarm_channel";
    public static final String CHANNEL_NAME = "Flight Boarding Alarms";
    public static final int NOTIFICATION_ID = 5001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "⏰ AlarmManager triggered! Alarm received in AlarmReceiver.");

        String flightNumber = "AI 302";
        String terminal = "Terminal 2";
        String gate = "Gate B24";
        String airport = "Delhi (DEL) ➔ Mumbai (BOM)";
        String message = "Boarding starts now! Proceed to gate.";
        int tripId = -1;

        if (intent != null) {
            if (intent.hasExtra(EXTRA_FLIGHT_NUMBER)) {
                flightNumber = intent.getStringExtra(EXTRA_FLIGHT_NUMBER);
            }
            if (intent.hasExtra(EXTRA_TERMINAL)) {
                terminal = intent.getStringExtra(EXTRA_TERMINAL);
            }
            if (intent.hasExtra(EXTRA_GATE)) {
                gate = intent.getStringExtra(EXTRA_GATE);
            }
            if (intent.hasExtra(EXTRA_AIRPORT)) {
                airport = intent.getStringExtra(EXTRA_AIRPORT);
            }
            if (intent.hasExtra(EXTRA_MESSAGE)) {
                message = intent.getStringExtra(EXTRA_MESSAGE);
            }
            tripId = intent.getIntExtra(EXTRA_TRIP_ID, -1);
        }

        // 1. Show Toast on device screen
        Toast.makeText(context, "⏰ ALARM: Flight " + flightNumber + " Boarding Call!", Toast.LENGTH_LONG).show();

        // 2. Setup Notification Channel (Android 8.0+)
        createNotificationChannel(context);

        // 3. Intent to open Flight Dashboard when notification is tapped
        Intent openIntent = new Intent(context, FlightDashboardActivity.class);
        openIntent.putExtra("trip_id", tripId);
        openIntent.putExtra("flight_number", flightNumber);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                tripId != -1 ? tripId : 0,
                openIntent,
                pendingIntentFlags
        );

        // 4. Build High Priority Notification with Sound & Vibration
        Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSoundUri == null) {
            alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        String title = "⏰ BOARDING ALARM: " + flightNumber;
        String contentText = terminal + " • " + gate + " | " + message;
        String bigText = "Flight: " + flightNumber + " (" + airport + ")\n"
                + "Gate: " + gate + " • " + terminal + "\n"
                + "Alert: " + message + "\n"
                + "Scheduled by GateCrash AlarmManager.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(alarmSoundUri)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "VIEW FLIGHT", pendingIntent);

        // 5. Check POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Notification cannot be posted.");
                return;
            }
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Boarding Alarm Notification posted successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error posting alarm notification: " + e.getMessage(), e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
                if (channel == null) {
                    NotificationChannel newChannel = new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    newChannel.setDescription("Alarms triggered by AlarmManager for flight departures and boarding");
                    newChannel.enableLights(true);
                    newChannel.enableVibration(true);
                    newChannel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
                    manager.createNotificationChannel(newChannel);
                    Log.d(TAG, "NotificationChannel created for AlarmManager: " + CHANNEL_ID);
                }
            }
        }
    }
}
