package com.example.edutech.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.edutech.R;
import com.example.edutech.User; // <-- ADD THIS IMPORT LINE
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewStaffActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<User> staffList;
    private FirebaseFirestore db;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_staff);

        db = FirebaseFirestore.getInstance();
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList);

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> {
            finish();
        });

        recyclerView = findViewById(R.id.staffRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(staffAdapter);

        fetchStaffData();
    }

    private void fetchStaffData() {
        db.collection("users")
                .whereEqualTo("role", "Teacher")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        staffList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            staffList.add(user);
                        }
                        staffAdapter.notifyDataSetChanged();
                        if (staffList.isEmpty()) {
                            Toast.makeText(this, "No teachers found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("ViewStaffActivity", "Error getting documents.", task.getException());
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}