package com.example.edutech.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edutech.R;
import com.example.edutech.Shift;
import com.example.edutech.adapters.ShiftAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ViewScheduleActivity extends AppCompatActivity implements ShiftAdapter.OnShiftListener {

    private RecyclerView scheduleRecyclerView;
    private ShiftAdapter shiftAdapter;
    private List<Shift> shiftList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());

        scheduleRecyclerView = findViewById(R.id.scheduleRecyclerView);
        shiftList = new ArrayList<>();
        shiftAdapter = new ShiftAdapter(shiftList, true, this); // 'true' for admin
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(shiftAdapter);

        fetchActiveSchedules();
    }

    private void fetchActiveSchedules() {
        db.collection("schedules")
                // This line filters to show only schedules that haven't ended yet
                .whereGreaterThanOrEqualTo("endTime", Timestamp.now())
                // This line sorts them from latest to oldest
                .orderBy("endTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        shiftList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Shift shift = document.toObject(Shift.class);
                            shift.setDocumentId(document.getId());
                            shiftList.add(shift);
                        }
                        shiftAdapter.notifyDataSetChanged();
                        if (shiftList.isEmpty()) {
                            Toast.makeText(this, "No active schedules found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("AdminViewSchedule", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Error fetching schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteClick(int position) {
        Shift shiftToDelete = shiftList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Schedule")
                .setMessage("This will permanently delete the schedule for all users. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("schedules").document(shiftToDelete.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                shiftList.remove(position);
                                shiftAdapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error deleting schedule", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}