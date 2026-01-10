package com.example.cryptofile_android.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.models.ActivityLog;
import com.example.cryptofile_android.ui.auth.LoginActivity;
import com.example.cryptofile_android.ui.encryption.DecryptFileActivity;
import com.example.cryptofile_android.ui.encryption.EncryptFileActivity;
import com.example.cryptofile_android.ui.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserHomeActivity extends AppCompatActivity {

    private static final String TAG = "UserHomeActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView welcomeText;
    private TextView viewAllLink;
    private LinearLayout activityLogsContainer;
    private TextView emptyActivityText;
    private MaterialCardView encryptCard;
    private MaterialCardView decryptCard;
    private MaterialCardView activityCard;
    private MaterialCardView settingsCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupCardClickListeners();
        displayWelcomeMessage();
        loadTodayActivityLogs();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        welcomeText = findViewById(R.id.welcome_text);
        viewAllLink = findViewById(R.id.view_all_link);
        activityLogsContainer = findViewById(R.id.activity_logs_container);
        emptyActivityText = findViewById(R.id.empty_activity_text);
        encryptCard = findViewById(R.id.encrypt_card);
        decryptCard = findViewById(R.id.decrypt_card);
        activityCard = findViewById(R.id.activity_card);
        settingsCard = findViewById(R.id.settings_card);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("CryptoFile");
        }
    }

    private void setupCardClickListeners() {
        encryptCard.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, EncryptFileActivity.class));
        });

        decryptCard.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, DecryptFileActivity.class));
        });

        activityCard.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, UserActivityLogsActivity.class));
        });

        settingsCard.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, UserProfileActivity.class));
        });

        viewAllLink.setOnClickListener(v -> {
            startActivity(new Intent(UserHomeActivity.this, UserActivityLogsActivity.class));
        });
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        android.view.View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);

        if (SessionManager.loggedInUser != null) {
            navUsername.setText(SessionManager.loggedInUser.getUsername());
            navEmail.setText(SessionManager.loggedInUser.getEmail());
        } else {
            navUsername.setText("");
            navEmail.setText("");
        }
    }

    private void displayWelcomeMessage() {
        String username = SessionManager.loggedInUser.getUsername();
        if (username != null) {
            String firstName = username.split(" ")[0];
            welcomeText.setText("Welcome back, " + firstName + "!");
        }
    }

    private void loadTodayActivityLogs() {
        String userId = SessionManager.loggedInUser.getUserId();
        if (userId == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        db.collection("activity_logs")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityLogsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyActivityText.setVisibility(View.VISIBLE);
                        emptyActivityText.setText("No activity today");
                    } else {
                        emptyActivityText.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ActivityLog log = document.toObject(ActivityLog.class);
                            View logView = createLogRow(log);
                            activityLogsContainer.addView(logView);

                            if (!document.equals(queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1))) {
                                View separator = new View(this);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        1
                                );
                                params.setMargins(0, 16, 0, 16);
                                separator.setLayoutParams(params);
                                separator.setBackgroundColor(0xFFE0E0E0);
                                activityLogsContainer.addView(separator);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading activity logs", e);
                    emptyActivityText.setVisibility(View.VISIBLE);
                    emptyActivityText.setText("Failed to load activity");
                });
    }

    private View createLogRow(ActivityLog log) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_activity_log2, activityLogsContainer, false);

        TextView statusIcon = rowView.findViewById(R.id.status_icon);
        TextView fileName = rowView.findViewById(R.id.file_name);
        TextView operationBadge = rowView.findViewById(R.id.operation_badge);
        TextView fileSize = rowView.findViewById(R.id.file_size);
        TextView dateTime = rowView.findViewById(R.id.date_time);
        TextView resultBadge = rowView.findViewById(R.id.result_badge);

        switch (log.getStatus()) {
            case "SUCCESS":
                statusIcon.setText("✓");
                statusIcon.setTextColor(android.graphics.Color.parseColor("#27ae60"));
                break;
            case "FAILED":
                statusIcon.setText("✗");
                statusIcon.setTextColor(android.graphics.Color.parseColor("#e74c3c"));
                break;
            case "CANCELLED":
                statusIcon.setText("○");
                statusIcon.setTextColor(android.graphics.Color.parseColor("#95a5a6"));
                break;
        }

        fileName.setText(log.getFileName() != null ? log.getFileName() : "Unknown");

        String action = log.getAction();
        if ("ENCRYPT".equalsIgnoreCase(action)) {
            operationBadge.setText("🔒 Encrypt");
            operationBadge.setBackgroundColor(android.graphics.Color.parseColor("#E3F2FD"));
            operationBadge.setTextColor(android.graphics.Color.parseColor("#1976D2"));
        } else if ("DECRYPT".equalsIgnoreCase(action)) {
            operationBadge.setText("🔓 Decrypt");
            operationBadge.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
            operationBadge.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        } else if ("RE-ENCRYPT".equalsIgnoreCase(action)) {
            operationBadge.setText("🔄 Re-encrypt");
            operationBadge.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
            operationBadge.setTextColor(android.graphics.Color.parseColor("#F57C00"));
        } else {
            operationBadge.setText(action);
            operationBadge.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            operationBadge.setTextColor(android.graphics.Color.parseColor("#757575"));
        }

        fileSize.setText(formatFileSize(log.getFileSize()));

        if (log.getTimestamp() != null) {
            dateTime.setText(formatTimeAgo(log.getTimestamp()));
        } else {
            dateTime.setText("-");
        }

        switch (log.getStatus()) {
            case "SUCCESS":
                resultBadge.setText("Success");
                resultBadge.setBackgroundColor(android.graphics.Color.parseColor("#d4edda"));
                resultBadge.setTextColor(android.graphics.Color.parseColor("#155724"));
                break;
            case "FAILED":
                resultBadge.setText("Failed");
                resultBadge.setBackgroundColor(android.graphics.Color.parseColor("#fff3cd"));
                resultBadge.setTextColor(android.graphics.Color.parseColor("#856404"));
                break;
            case "CANCELLED":
                resultBadge.setText("Cancelled");
                resultBadge.setBackgroundColor(android.graphics.Color.parseColor("#f8d7da"));
                resultBadge.setTextColor(android.graphics.Color.parseColor("#721c24"));
                break;
        }

        return rowView;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatTimeAgo(Date timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes == 0) {
            return "Just now";
        } else if (minutes == 1) {
            return "1 minute ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours == 1) {
            return "1 hour ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(timestamp);
        }
    }

    private void handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.nav_encrypt) {
            startActivity(new Intent(this, EncryptFileActivity.class));
        } else if (itemId == R.id.nav_decrypt) {
            startActivity(new Intent(this, DecryptFileActivity.class));
        } else if (itemId == R.id.nav_activity_logs) {
            startActivity(new Intent(this, UserActivityLogsActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        }
    }

    private void handleLogout() {
        mAuth.signOut();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

