package com.example.edutech.teacher.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutech.R;
import com.example.edutech.Shift;
import com.example.edutech.adapters.ShiftAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewScheduleFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView scheduleRecyclerView;
    private ShiftAdapter shiftAdapter;
    private List<Shift> shiftList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_schedule_teacher, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Find views using the 'view' object from the inflated layout
        calendarView = view.findViewById(R.id.calendarView);
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);

        shiftList = new ArrayList<>();
        // Use requireContext() to get the context in a Fragment
        shiftAdapter = new ShiftAdapter(shiftList, false, null);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        scheduleRecyclerView.setAdapter(shiftAdapter);

        // Fetch schedule for today when the fragment is created
        fetchShiftsForDate(Calendar.getInstance());

        // Listener for when the user selects a new date
        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            fetchShiftsForDate(selectedDate);
        });

        return view;
    }

    private void fetchShiftsForDate(Calendar selectedDate) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        // Set time to the start of the selected day
        selectedDate.set(Calendar.HOUR_OF_DAY, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        Timestamp startOfDay = new Timestamp(selectedDate.getTime());

        // Set time to the end of the selected day
        selectedDate.set(Calendar.HOUR_OF_DAY, 23);
        selectedDate.set(Calendar.MINUTE, 59);
        Timestamp endOfDay = new Timestamp(selectedDate.getTime());

        // This query gets ALL shifts for the selected day for this teacher
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
                            Toast.makeText(getContext(), "No shifts scheduled for this day", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("ViewScheduleFragment", "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
