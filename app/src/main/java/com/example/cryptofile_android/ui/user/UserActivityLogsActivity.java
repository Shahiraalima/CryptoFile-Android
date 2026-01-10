package com.example.cryptofile_android.ui.user;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.ActivityDAO;
import com.example.cryptofile_android.models.ActivityLogAdapter;
import com.example.cryptofile_android.models.ActivityLog;
import com.example.cryptofile_android.ui.utils.SessionManager;

import java.util.List;
import java.util.concurrent.Executors;

public class UserActivityLogsActivity extends AppCompatActivity {

    private TextView totalOperationsText;
    private TextView encryptedCountText;
    private TextView decryptedCountText;
    private TextView successRateText;

    private RecyclerView activityRecyclerView;
    private ActivityLogAdapter adapter;
    private ActivityDAO activityDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_activitylog);

        activityDAO = ActivityDAO.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadStatistics();
        loadActivityLogs();
    }

    private void initViews() {
        totalOperationsText = findViewById(R.id.total_operations_text);
        encryptedCountText = findViewById(R.id.encrypted_count_text);
        decryptedCountText = findViewById(R.id.decrypted_count_text);
        successRateText = findViewById(R.id.success_rate_text);
        activityRecyclerView = findViewById(R.id.activity_recycler_view);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Activity Logs");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ActivityLogAdapter();
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityRecyclerView.setAdapter(adapter);
    }

    private void loadStatistics() {
        String userId = SessionManager.loggedInUser.getUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int totalOperations = activityDAO.getLogsCountByUserId(userId).get();

                List<ActivityLog> allLogs = activityDAO.getLogsByUserId(userId, 1000).get();

                int encryptedCount = 0;
                int decryptedCount = 0;
                int successCount = 0;

                for (ActivityLog log : allLogs) {
                    if ("ENCRYPT".equalsIgnoreCase(log.getAction())) {
                        encryptedCount++;
                    } else if ("DECRYPT".equalsIgnoreCase(log.getAction())) {
                        decryptedCount++;
                    }

                    if ("SUCCESS".equalsIgnoreCase(log.getStatus())) {
                        successCount++;
                    }
                }

                double successRate = totalOperations > 0 ? (successCount * 100.0 / totalOperations) : 0.0;

                int finalEncryptedCount = encryptedCount;
                int finalDecryptedCount = decryptedCount;

                runOnUiThread(() -> {
                    animateCounter(totalOperationsText, totalOperations);
                    animateCounter(encryptedCountText, finalEncryptedCount);
                    animateCounter(decryptedCountText, finalDecryptedCount);

                    ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) successRate);
                    animator.setDuration(1000);
                    animator.addUpdateListener(animation -> {
                        float value = (float) animation.getAnimatedValue();
                        successRateText.setText(String.format("%.1f%%", value));
                    });
                    animator.start();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load statistics", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadActivityLogs() {
        String userId = SessionManager.loggedInUser.getUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<ActivityLog> logs = activityDAO.getLogsByUserId(userId, 100).get();

                runOnUiThread(() -> {
                    if (logs != null && !logs.isEmpty()) {
                        adapter.setActivityLogs(logs);
                    } else {
                        Toast.makeText(this, "No activity logs found", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load activity logs", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void animateCounter(TextView textView, int targetValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(value));
        });
        animator.start();
    }
}

