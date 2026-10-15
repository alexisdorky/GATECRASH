package com.example.gatecrash;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "GateCrash.db";
    public static final int DATABASE_VERSION = 1;

    // Table 1: Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_NAME = "name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // Table 2: Trips / Flights
    public static final String TABLE_TRIPS = "trips";
    public static final String COL_TRIP_ID = "trip_id";
    public static final String COL_TRIP_USER_ID = "user_id";
    public static final String COL_AIRLINE = "airline";
    public static final String COL_FLIGHT_NUMBER = "flight_number";
    public static final String COL_AIRPORT = "airport";
    public static final String COL_DATE = "date";
    public static final String COL_DEPARTURE_TIME = "departure_time";
    public static final String COL_ARRIVAL_TIME = "arrival_time";
    public static final String COL_TERMINAL = "terminal";
    public static final String COL_GATE = "gate";

    // Table 3: Checklist
    public static final String TABLE_CHECKLIST = "checklist";
    public static final String COL_TASK_ID = "task_id";
    public static final String COL_TASK_TRIP_ID = "trip_id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_TASK_STATUS = "status"; // 0 = incomplete, 1 = completed

    // Table 4: Reminders
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COL_REMINDER_ID = "reminder_id";
    public static final String COL_REMINDER_TRIP_ID = "trip_id";
    public static final String COL_REMINDER_TIME = "reminder_time";
    public static final String COL_REMINDER_ENABLED = "enabled"; // 0 = false, 1 = true

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Create Trips table
        String createTripsTable = "CREATE TABLE " + TABLE_TRIPS + " ("
                + COL_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TRIP_USER_ID + " INTEGER, "
                + COL_AIRLINE + " TEXT, "
                + COL_FLIGHT_NUMBER + " TEXT, "
                + COL_AIRPORT + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_DEPARTURE_TIME + " TEXT, "
                + COL_ARRIVAL_TIME + " TEXT, "
                + COL_TERMINAL + " TEXT, "
                + COL_GATE + " TEXT)";
        db.execSQL(createTripsTable);

        // Create Checklist table
        String createChecklistTable = "CREATE TABLE " + TABLE_CHECKLIST + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TRIP_ID + " INTEGER, "
                + COL_TASK_NAME + " TEXT, "
                + COL_TASK_STATUS + " INTEGER DEFAULT 0)";
        db.execSQL(createChecklistTable);

        // Create Reminders table
        String createRemindersTable = "CREATE TABLE " + TABLE_REMINDERS + " ("
                + COL_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_REMINDER_TRIP_ID + " INTEGER, "
                + COL_REMINDER_TIME + " TEXT, "
                + COL_REMINDER_ENABLED + " INTEGER DEFAULT 1)";
        db.execSQL(createRemindersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    // ==========================================
    // USER METHODS (Registration & Login)
    // ==========================================

    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_EMAIL, email.trim().toLowerCase());
        cv.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email.trim().toLowerCase()});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{email.trim().toLowerCase(), password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email.trim().toLowerCase()});
        String name = "Passenger";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email.trim().toLowerCase()});
        int id = 1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    // ==========================================
    // TRIP / FLIGHT METHODS (Add Flight, Dashboard)
    // ==========================================

    public long insertTrip(int userId, String airline, String flightNo, String airport,
                           String date, String depTime, String arrTime, String terminal, String gate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TRIP_USER_ID, userId);
        cv.put(COL_AIRLINE, airline);
        cv.put(COL_FLIGHT_NUMBER, flightNo);
        cv.put(COL_AIRPORT, airport);
        cv.put(COL_DATE, date);
        cv.put(COL_DEPARTURE_TIME, depTime);
        cv.put(COL_ARRIVAL_TIME, arrTime);
        cv.put(COL_TERMINAL, terminal);
        cv.put(COL_GATE, gate);

        return db.insert(TABLE_TRIPS, null, cv);
    }

    public Cursor getLatestTrip(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_USER_ID + " = ? ORDER BY " + COL_TRIP_ID + " DESC LIMIT 1",
                new String[]{String.valueOf(userId)});
    }

    public Cursor getAllTrips(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_USER_ID + " = ? ORDER BY " + COL_TRIP_ID + " DESC",
                new String[]{String.valueOf(userId)});
    }

    public Cursor getTripById(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COL_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)});
    }

    // ==========================================
    // CHECKLIST METHODS
    // ==========================================

    public long insertTask(int tripId, String taskName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TRIP_ID, tripId);
        cv.put(COL_TASK_NAME, taskName);
        cv.put(COL_TASK_STATUS, 0);
        return db.insert(TABLE_CHECKLIST, null, cv);
    }

    public Cursor getTasks(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CHECKLIST + " WHERE " + COL_TASK_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)});
    }

    public boolean updateTaskStatus(int taskId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_STATUS, status);
        int rows = db.update(TABLE_CHECKLIST, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return rows > 0;
    }

    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CHECKLIST, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return rows > 0;
    }

    // ==========================================
    // REMINDER METHODS
    // ==========================================

    public long setReminder(int tripId, String reminderTime, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Remove prior reminder for this trip
        db.delete(TABLE_REMINDERS, COL_REMINDER_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)});

        ContentValues cv = new ContentValues();
        cv.put(COL_REMINDER_TRIP_ID, tripId);
        cv.put(COL_REMINDER_TIME, reminderTime);
        cv.put(COL_REMINDER_ENABLED, enabled);
        return db.insert(TABLE_REMINDERS, null, cv);
    }

    public Cursor getReminder(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " WHERE " + COL_REMINDER_TRIP_ID + " = ?",
                new String[]{String.valueOf(tripId)});
    }

    public boolean deleteReminder(int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_REMINDERS, COL_REMINDER_TRIP_ID + " = ?", new String[]{String.valueOf(tripId)});
        return rows > 0;
    }
}
