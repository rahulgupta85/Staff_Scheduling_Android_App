package com.example.edutech.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.edutech.fragments.NotificationsFragment;
import com.example.edutech.R;
import com.example.edutech.admin.fragments.AdminAnnouncementsFragment;
import com.example.edutech.admin.fragments.AdminHomeFragment;
import com.example.edutech.admin.fragments.AdminStaffFragment;
import com.example.edutech.admin.fragments.ManageLeaveRequestsFragment;
import com.example.edutech.admin.fragments.ScheduleOptionsFragment;
import com.example.edutech.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private TextView notificationBadge; // Badge for notifications

    // ✅ NEW VARIABLE ADDED
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You will not receive important updates.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment(), false);
            setTitle("Home");
        }
        loadAdminProfile();

        // ✅ NEW CALL ADDED AT END OF onCreate
        askNotificationPermission();
    }

    private void loadAdminProfile() {
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
                    selectedFragment = new AdminHomeFragment();
                    loadAdminProfile();
                } else if (itemId == R.id.nav_staff) {
                    selectedFragment = new AdminStaffFragment();
                    setTitle("Staff");
                } else if (itemId == R.id.nav_schedule) {
                    selectedFragment = new ScheduleOptionsFragment();
                    setTitle("Schedule");
                } else if (itemId == R.id.nav_leave_requests) {
                    selectedFragment = new ManageLeaveRequestsFragment();
                    setTitle("Leave Requests");
                } else if (itemId == R.id.nav_announcements) {
                    selectedFragment = new AdminAnnouncementsFragment();
                    setTitle("Announcements");
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            loadFragment(new ProfileFragment(), true);
            setTitle("My Profile");
            return true;
        } else if (itemId == R.id.action_notifications) {
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

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
    }

    // ✅ NEW METHOD ADDED
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Already granted
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
