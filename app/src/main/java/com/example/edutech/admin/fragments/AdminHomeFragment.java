package com.example.edutech.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutech.R;
import com.example.edutech.Shift;
import com.example.edutech.adapters.ShiftAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private TextView textViewShiftsToday, textViewOnLeave, textViewNoSchedule;
    private RecyclerView todaysScheduleRecyclerView;
    private ShiftAdapter shiftAdapter;
    private List<Shift> todaysShiftList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        db = FirebaseFirestore.getInstance();

        textViewShiftsToday = view.findViewById(R.id.textViewShiftsTodayCount);
        textViewOnLeave = view.findViewById(R.id.textViewOnLeaveCount);
        todaysScheduleRecyclerView = view.findViewById(R.id.todaysScheduleRecyclerView);
        textViewNoSchedule = view.findViewById(R.id.textViewNoSchedule);

        // Setup RecyclerView for today's schedule
        todaysShiftList = new ArrayList<>();
        shiftAdapter = new ShiftAdapter(todaysShiftList, false, null);
        todaysScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todaysScheduleRecyclerView.setAdapter(shiftAdapter);

        loadDashboardData();

        return view;
    }

    private void loadDashboardData() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        Timestamp startOfDay = new Timestamp(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Timestamp endOfDay = new Timestamp(cal.getTime());

        fetchShiftsTodayCount(startOfDay, endOfDay);
        fetchOnLeaveTodayCount(startOfDay, endOfDay);
        fetchTodaysSchedule(startOfDay, endOfDay);
    }

    private void fetchShiftsTodayCount(Timestamp start, Timestamp end) {
        db.collection("schedules")
                .whereGreaterThanOrEqualTo("startTime", start)
                .whereLessThanOrEqualTo("startTime", end)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    textViewShiftsToday.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    textViewShiftsToday.setText("0"); // Set to 0 on failure
                });
    }

    private void fetchOnLeaveTodayCount(Timestamp start, Timestamp end) {
        db.collection("leaveRequests")
                .whereEqualTo("status", "approved")
                .whereLessThanOrEqualTo("startDate", end)
                .whereGreaterThanOrEqualTo("endDate", start)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // This will now correctly show "0" if the list is empty
                    textViewOnLeave.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    // Also set to 0 if there's an error
                    textViewOnLeave.setText("0");
                });
    }

    private void fetchTodaysSchedule(Timestamp start, Timestamp end) {
        db.collection("schedules")
                .whereGreaterThanOrEqualTo("startTime", start)
                .whereLessThanOrEqualTo("startTime", end)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todaysShiftList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        todaysShiftList.add(document.toObject(Shift.class));
                    }
                    shiftAdapter.notifyDataSetChanged();

                    // Show/hide the "no schedule" message
                    if (todaysShiftList.isEmpty()) {
                        todaysScheduleRecyclerView.setVisibility(View.GONE);
                        textViewNoSchedule.setVisibility(View.VISIBLE);
                    } else {
                        todaysScheduleRecyclerView.setVisibility(View.VISIBLE);
                        textViewNoSchedule.setVisibility(View.GONE);
                    }
                });
    }
}
