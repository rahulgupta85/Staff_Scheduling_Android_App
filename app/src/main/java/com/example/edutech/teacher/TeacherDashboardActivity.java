package com.example.edutech.teacher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.edutech.Notification;
import com.example.edutech.R;
import com.example.edutech.authViewModel;
import com.example.edutech.fragments.NotificationsFragment; // <-- Make sure this is imported
import com.example.edutech.fragments.ProfileFragment;
import com.example.edutech.teacher.fragments.TeacherHomeFragment;
import com.example.edutech.teacher.fragments.ViewAnnouncementsFragment;
import com.example.edutech.teacher.fragments.ViewMyLeaveRequestsFragment;
import com.example.edutech.teacher.fragments.ViewScheduleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.LinkedList;
import java.util.Queue;

public class TeacherDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private TextView notificationBadge;
    private Queue<Pair<String, Notification>> unreadNotificationsQueue = new LinkedList<>();

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "You will not receive important push notifications.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            loadFragment(new TeacherHomeFragment(), false);
        }
        loadTeacherProfile();
        askNotificationPermission();
        checkForUnreadNotifications();
    }

    private void loadTeacherProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            if (firstName != null) {
                                toolbar.setTitle("Hello, " + firstName + "!");
                            }
                        }
                    });
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new TeacherHomeFragment();
                    loadTeacherProfile();
                } else if (itemId == R.id.nav_schedule) {
                    selectedFragment = new ViewScheduleFragment();
                    setTitle("My Schedule");
                } else if (itemId == R.id.nav_leave) {
                    selectedFragment = new ViewMyLeaveRequestsFragment();
                    setTitle("Leave Requests");
                } else if (itemId == R.id.nav_announcements) {
                    selectedFragment = new ViewAnnouncementsFragment();
                    setTitle("Announcements");
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    setTitle("My Profile");
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, false);
                }
                return true;
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_toolbar_menu, menu);
        final MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        View actionView = notificationItem.getActionView();
        notificationBadge = actionView.findViewById(R.id.notification_badge);
        actionView.setOnClickListener(v -> onOptionsItemSelected(notificationItem));
        setupNotificationBadge();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // --- THIS IS THE CORRECTED SECTION ---
        // It now loads the NotificationsFragment instead of starting an Activity
        if (item.getItemId() == R.id.action_notifications) {
            loadFragment(new NotificationsFragment(), true); // 'true' to add to back stack
            setTitle("Notifications");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationBadge != null) {
            setupNotificationBadge();
        }
    }

    private void setupNotificationBadge() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || notificationBadge == null) return;
        db.collection("notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int unreadCount = queryDocumentSnapshots.size();
                    if (unreadCount > 0) {
                        notificationBadge.setText(String.valueOf(Math.min(unreadCount, 99)));
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    private void checkForUnreadNotifications() {
        // ... (This method is correct and does not need to change)
    }

    private void showNextNotification() {
        // ... (This method is correct and does not need to change)
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }
}