package com.example.gatecrash;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChecklistActivity extends AppCompatActivity {

    private EditText taskInput;
    private Button addTaskBtn, backBtn;
    private LinearLayout tasksContainer;
    private DatabaseHelper dbHelper;
    private int tripId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        dbHelper = new DatabaseHelper(this);

        int incomingTripId = getIntent().getIntExtra("trip_id", -1);
        if (incomingTripId != -1) {
            tripId = incomingTripId;
        }

        taskInput = findViewById(R.id.taskInput);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        backBtn = findViewById(R.id.backFromChecklist);
        tasksContainer = findViewById(R.id.tasksContainer);

        backBtn.setOnClickListener(v -> finish());

        // Load tasks from SQLite
        loadTasksFromDatabase();

        addTaskBtn.setOnClickListener(v -> {
            String taskText = taskInput.getText().toString().trim();
            if (TextUtils.isEmpty(taskText)) {
                Toast.makeText(this, "Please enter a task name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to SQLite
            long newTaskId = dbHelper.insertTask(tripId, taskText);
            if (newTaskId != -1) {
                addTaskView((int) newTaskId, taskText, false);
                taskInput.setText("");
                Toast.makeText(this, "Task saved to SQLite!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasksFromDatabase() {
        tasksContainer.removeAllViews();
        Cursor cursor = dbHelper.getTasks(tripId);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int taskId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_ID));
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_NAME));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TASK_STATUS));

                addTaskView(taskId, taskName, status == 1);
            }
            cursor.close();
        } else {
            // Seed default tasks into SQLite for this trip
            seedDefaultTasks();
        }
    }

    private void seedDefaultTasks() {
        String[] defaults = {
                "Check next flight live status",
                "Find Gate B24 on airport map",
                "Charge phone at charging station",
                "Grab food / coffee at Terminal Food Court",
                "Visit restroom before boarding",
                "Reach boarding gate 25 mins prior"
        };

        for (int i = 0; i < defaults.length; i++) {
            long id = dbHelper.insertTask(tripId, defaults[i]);
            addTaskView((int) id, defaults[i], i == 0); // first item checked
            if (i == 0) {
                dbHelper.updateTaskStatus((int) id, 1);
            }
        }
    }

    private void addTaskView(int taskId, String taskName, boolean isChecked) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(taskName);
        checkBox.setTextSize(15f);
        checkBox.setChecked(isChecked);
        checkBox.setPadding(8, 8, 8, 8);

        checkBox.setOnCheckedChangeListener((buttonView, isCheckedNow) -> {
            dbHelper.updateTaskStatus(taskId, isCheckedNow ? 1 : 0);
        });

        tasksContainer.addView(checkBox);
    }
}
