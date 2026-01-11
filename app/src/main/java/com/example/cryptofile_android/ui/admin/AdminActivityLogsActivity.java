package com.example.cryptofile_android.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.ActivityDAO;
import com.example.cryptofile_android.models.ActivityLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminActivityLogsActivity extends AppCompatActivity {

    private TextView emptyStateText;
    private RecyclerView recyclerView;
    private LogAdapter logAdapter;
    private ActivityDAO logDAO;
    private List<ActivityLog> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_logs);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Activity Logs");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        logDAO = ActivityDAO.getInstance();

        logAdapter = new LogAdapter(logList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(logAdapter);

        loadLogs();
    }

    private void loadLogs() {
        logDAO.getAllLogsWithUserInfo().thenAccept(logs -> {
            runOnUiThread(() -> {
                logList.clear();
                logList.addAll(logs);
                logAdapter.notifyDataSetChanged();

                // Toggle empty state
                if (logs.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                }
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() -> {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
                emptyStateText.setText("Failed to load logs: " + e.getMessage());
                Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

        private List<ActivityLog> logs;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public LogAdapter(List<ActivityLog> logs) {
            this.logs = logs;
        }

        @Override
        public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_activity_log_admin, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LogViewHolder holder, int position) {
            ActivityLog log = logs.get(position);

            String action = log.getAction() != null ? log.getAction() : "UNKNOWN";
            holder.actionText.setText(action);

            if ("ENCRYPT".equalsIgnoreCase(action)) {
                holder.actionIcon.setText("🔒");
                holder.actionText.setTextColor(0xFF27ae60); // Green
            } else if ("DECRYPT".equalsIgnoreCase(action)) {
                holder.actionIcon.setText("🔓");
                holder.actionText.setTextColor(0xFF3498db); // Blue
            } else {
                holder.actionIcon.setText("📄");
                holder.actionText.setTextColor(0xFF95a5a6); // Gray
            }

            holder.fileName.setText(log.getFileName() != null ?
                    log.getFileName() : "N/A");

            holder.user.setText(log.getUsername() != null ?
                    log.getUsername() : "Unknown");

            String status = log.getStatus() != null ? log.getStatus() : "UNKNOWN";
            holder.status.setText(formatStatus(status));
            setStatusBackground(holder.status, status);

            holder.fileSize.setText(formatFileSize(log.getFileSize()));

            if (log.getTimestamp() != null) {
                holder.date.setText(dateFormat.format(log.getTimestamp()));
            } else {
                holder.date.setText("N/A");
            }
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        private String formatStatus(String status) {
            if (status == null) return "Unknown";

            switch (status.toUpperCase()) {
                case "SUCCESS":
                case "COMPLETED":
                    return "Success";
                case "FAILED":
                    return "Failed";
                case "CANCELLED":
                    return "Cancelled";
                default:
                    return status;
            }
        }

        private void setStatusBackground(TextView textView, String status) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(12);

            if (status == null) {
                drawable.setColor(0xFF95a5a6); // Gray
            } else {
                switch (status.toUpperCase()) {
                    case "SUCCESS":
                    case "COMPLETED":
                        drawable.setColor(0xFF27ae60); // Green
                        break;
                    case "FAILED":
                        drawable.setColor(0xFFe74c3c); // Red
                        break;
                    case "CANCELLED":
                        drawable.setColor(0xFF95a5a6); // Gray
                        break;
                    default:
                        drawable.setColor(0xFF3498db); // Blue
                        break;
                }
            }

            textView.setBackground(drawable);
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return String.format(Locale.US, "%.2f KB", bytes / 1024.0);
            } else if (bytes < 1024 * 1024 * 1024) {
                return String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024));
            } else {
                return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024));
            }
        }

        class LogViewHolder extends RecyclerView.ViewHolder {
            TextView actionIcon, actionText, fileName, user, status, fileSize, date;

            public LogViewHolder(View itemView) {
                super(itemView);
                actionIcon = itemView.findViewById(R.id.action_icon);
                actionText = itemView.findViewById(R.id.action_text);
                fileName = itemView.findViewById(R.id.file_name_text);
                user = itemView.findViewById(R.id.user_text);
                status = itemView.findViewById(R.id.status_badge);
                fileSize = itemView.findViewById(R.id.file_size);
                date = itemView.findViewById(R.id.date_text);
            }
        }
    }
}

