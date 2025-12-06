package com.example.edutech.admin;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edutech.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ScheduleOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_options);

        // Find views
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        MaterialCardView createScheduleCard = findViewById(R.id.createScheduleCard);
        MaterialCardView viewScheduleCard = findViewById(R.id.viewScheduleCard);

        // Handle back button click
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Handle "Create Schedule" card click
        createScheduleCard.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateScheduleActivity.class));
        });

        // Handle "View Schedule" card click
        viewScheduleCard.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewScheduleActivity.class));
        });
    }
}