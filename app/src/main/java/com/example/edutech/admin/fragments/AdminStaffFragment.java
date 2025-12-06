package com.example.edutech.admin.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edutech.R;
import com.example.edutech.User;
import com.example.edutech.adapters.StaffAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminStaffFragment extends Fragment {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<User> staffList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_staff, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.staffRecyclerView);
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(staffAdapter);

        fetchStaffData();

        return view;
    }

    private void fetchStaffData() {
        db.collection("users")
                .whereEqualTo("role", "Teacher")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        staffList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            staffList.add(document.toObject(User.class));
                        }
                        staffAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("AdminStaffFragment", "Error getting documents.", task.getException());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error fetching staff data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}