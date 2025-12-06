package com.example.edutech.teacher;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutech.R;
import com.example.edutech.Shift;
import com.example.edutech.adapters.ShiftAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class ViewScheduleActivity extends AppCompatActivity {

    private RecyclerView scheduleRecyclerView;
    private ShiftAdapter shiftAdapter;
    private List<Shift> shiftList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button datePickerButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule_teacher);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        datePickerButton = findViewById(R.id.btn_date_picker);

        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        shiftList = new ArrayList<>();
        shiftAdapter = new ShiftAdapter(shiftList, false, null);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(shiftAdapter);

        // Setup the date picker button
        datePickerButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                // Create a calendar instance in UTC to handle the selection
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcCalendar.setTimeInMillis(selection);

                // Update button text
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
                datePickerButton.setText(sdf.format(utcCalendar.getTime()));

                // Fetch shifts for the selected date
                fetchShiftsForSelectedDate(utcCalendar);
            });

            datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        // Fetch schedule for today when the activity opens
        fetchShiftsForSelectedDate(Calendar.getInstance());
    }

    private void fetchShiftsForSelectedDate(Calendar selectedDate) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        Timestamp startOfDay = new Timestamp(selectedDate.getTime());

        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
        selectedDate.set(Calendar.MINUTE, 59);
        selectedDate.set(Calendar.SECOND, 59);
        Timestamp endOfDay = new Timestamp(selectedDate.getTime());

        db.collection("schedules")
                .whereEqualTo("userId", currentUserId)
                .whereGreaterThanOrEqualTo("startTime", startOfDay)
                .whereLessThanOrEqualTo("startTime", endOfDay)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        shiftList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            shiftList.add(document.toObject(Shift.class));
                        }
                        shiftAdapter.notifyDataSetChanged();
                        if (shiftList.isEmpty()) {
                            Toast.makeText(this, "No shifts scheduled for this day", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ViewSchedule", "Error: ", task.getException());
                        Toast.makeText(this, "Error fetching schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}