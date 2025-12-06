package com.example.edutech.admin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

public class ViewScheduleFragment extends Fragment implements ShiftAdapter.OnShiftListener {

    private RecyclerView scheduleRecyclerView;
    private View emptyView; // View for the "empty state"
    private ShiftAdapter shiftAdapter;
    private List<Shift> shiftList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_schedule, container, false);

        db = FirebaseFirestore.getInstance();

        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);
        emptyView = view.findViewById(R.id.empty_view); // Find the empty state view
        shiftList = new ArrayList<>();
        shiftAdapter = new ShiftAdapter(shiftList, true, this);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        scheduleRecyclerView.setAdapter(shiftAdapter);

        fetchAllSchedules();

        return view;
    }

    private void fetchAllSchedules() {
        db.collection("schedules")
                .whereGreaterThanOrEqualTo("endTime", Timestamp.now())
                .orderBy("endTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (getContext() == null) return;

                    if (task.isSuccessful()) {
                        shiftList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Shift shift = document.toObject(Shift.class);
                            shift.setDocumentId(document.getId());
                            shiftList.add(shift);
                        }
                        shiftAdapter.notifyDataSetChanged();

                        // --- NEW LOGIC TO SHOW/HIDE THE EMPTY STATE ---
                        if (shiftList.isEmpty()) {
                            scheduleRecyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            scheduleRecyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e("ViewScheduleFragment", "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching schedule", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDeleteClick(int position) {
        if (getContext() == null || position >= shiftList.size()) return;

        Shift shiftToDelete = shiftList.get(position);

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Schedule")
                .setMessage("This will permanently delete the schedule for all users. Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("schedules").document(shiftToDelete.getDocumentId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                shiftList.remove(position);
                                shiftAdapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Schedule deleted", Toast.LENGTH_SHORT).show();
                                // Check if the list is now empty
                                if (shiftList.isEmpty()) {
                                    scheduleRecyclerView.setVisibility(View.GONE);
                                    emptyView.setVisibility(View.VISIBLE);
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting schedule", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}