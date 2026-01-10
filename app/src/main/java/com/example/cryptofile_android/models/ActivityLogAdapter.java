package com.example.cryptofile_android.models;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.models.ActivityLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {

    private List<ActivityLog> activityLogs = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_log2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityLog log = activityLogs.get(position);
        holder.bind(log);
    }

    @Override
    public int getItemCount() {
        return activityLogs.size();
    }

    public void setActivityLogs(List<ActivityLog> logs) {
        this.activityLogs = logs != null ? logs : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView statusIcon;
        private final TextView fileName;
        private final TextView operationBadge;
        private final TextView fileSize;
        private final TextView dateTime;
        private final TextView resultBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.status_icon);
            fileName = itemView.findViewById(R.id.file_name);
            operationBadge = itemView.findViewById(R.id.operation_badge);
            fileSize = itemView.findViewById(R.id.file_size);
            dateTime = itemView.findViewById(R.id.date_time);
            resultBadge = itemView.findViewById(R.id.result_badge);
        }

        public void bind(ActivityLog log) {
            switch (log.getStatus()) {
                case "SUCCESS":
                    statusIcon.setText("✓");
                    statusIcon.setTextColor(Color.parseColor("#27ae60"));
                    break;
                case "FAILED":
                    statusIcon.setText("✗");
                    statusIcon.setTextColor(Color.parseColor("#e74c3c"));
                    break;
                case "CANCELLED":
                    statusIcon.setText("○");
                    statusIcon.setTextColor(Color.parseColor("#95a5a6"));
                    break;
            }

            fileName.setText(log.getFileName() != null ? log.getFileName() : "Unknown");

            String action = log.getAction();
            if ("ENCRYPT".equalsIgnoreCase(action)) {
                operationBadge.setText("🔒 Encrypt");
                operationBadge.setBackgroundColor(Color.parseColor("#E3F2FD"));
                operationBadge.setTextColor(Color.parseColor("#1976D2"));
            } else if ("DECRYPT".equalsIgnoreCase(action)) {
                operationBadge.setText("🔓 Decrypt");
                operationBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
                operationBadge.setTextColor(Color.parseColor("#388E3C"));
            } else {
                operationBadge.setText(action);
                operationBadge.setBackgroundColor(Color.parseColor("#F5F5F5"));
                operationBadge.setTextColor(Color.parseColor("#757575"));
            }

            fileSize.setText(formatFileSize(log.getFileSize()));

            if (log.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
                dateTime.setText(sdf.format(log.getTimestamp()));
            } else {
                dateTime.setText("-");
            }

            switch (log.getStatus()) {
                case "SUCCESS":
                    resultBadge.setText("Success");
                    resultBadge.setBackgroundColor(Color.parseColor("#d4edda"));
                    resultBadge.setTextColor(Color.parseColor("#155724"));
                    break;
                case "FAILED":
                    resultBadge.setText("Failed");
                    resultBadge.setBackgroundColor(Color.parseColor("#fff3cd"));
                    resultBadge.setTextColor(Color.parseColor("#856404"));
                    break;
                case "CANCELLED":
                    resultBadge.setText("Cancelled");
                    resultBadge.setBackgroundColor(Color.parseColor("#f8d7da"));
                    resultBadge.setTextColor(Color.parseColor("#721c24"));
                    break;
            }
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            String pre = "KMGTPE".charAt(exp - 1) + "";
            return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
        }
    }
}
