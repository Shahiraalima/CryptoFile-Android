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
import com.example.cryptofile_android.firebase.FileDAO;
import com.example.cryptofile_android.models.FileInfo;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminFileManagementActivity extends AppCompatActivity {

    private TextView totalFilesCount, encryptedFilesCount, decryptedFilesCount;
    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private FileAdapter fileAdapter;
    private FileDAO fileDAO;
    private List<FileInfo> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_files);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("File Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        totalFilesCount = findViewById(R.id.total_files_count);
        encryptedFilesCount = findViewById(R.id.encrypted_files_count);
        decryptedFilesCount = findViewById(R.id.decrypted_files_count);
        recyclerView = findViewById(R.id.recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        fileDAO= FileDAO.getInstance();

        fileAdapter = new FileAdapter(fileList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        loadStatistics();
        loadFiles();
    }

    private void loadStatistics() {
        fileDAO.getTotalFilesCount().thenAccept(total -> {
            runOnUiThread(() -> totalFilesCount.setText(String.valueOf(total)));
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() -> totalFilesCount.setText("0"));
            return null;
        });

        fileDAO.getFilesCountByStatus("COMPLETED").thenAccept(encrypted -> {
            runOnUiThread(() -> encryptedFilesCount.setText(String.valueOf(encrypted)));
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() -> encryptedFilesCount.setText("0"));
            return null;
        });

        fileDAO.getFilesCountByStatus("DECRYPTED").thenAccept(decrypted -> {
            runOnUiThread(() -> decryptedFilesCount.setText(String.valueOf(decrypted)));
        }).exceptionally(e -> {
            e.printStackTrace();
            runOnUiThread(() -> decryptedFilesCount.setText("0"));
            return null;
        });
    }

    private void loadFiles() {
        fileDAO.getAllFilesWithUserInfo().thenAccept(files -> {
            runOnUiThread(() -> {
                fileList.clear();
                fileList.addAll(files);
                fileAdapter.notifyDataSetChanged();

                if (files.isEmpty()) {
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
                emptyStateText.setText("Failed to load files: " + e.getMessage());
                Toast.makeText(this, "Failed to load files", Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    private class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

        private List<FileInfo> files;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public FileAdapter(List<FileInfo> files) {
            this.files = files;
        }

        @Override
        public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_files, parent, false);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FileViewHolder holder, int position) {
            FileInfo file = files.get(position);

            holder.originalFileName.setText(file.getOriginalFileName() != null ?
                    file.getOriginalFileName() : "N/A");

            holder.owner.setText(file.getUsername() != null ?
                    file.getUsername() : "Unknown");

            String status = file.getStatus() != null ? file.getStatus() : "UNKNOWN";
            holder.status.setText(formatStatus(status));
            setStatusBackground(holder.status, status);

            holder.newFileName.setText(file.getNewFileName() != null ?
                    file.getNewFileName() : "N/A");

            holder.fileSize.setText(formatFileSize(file.getNewFileSize()));

            if (file.getModifiedAt() != null) {
                holder.date.setText(dateFormat.format(file.getModifiedAt()));
            } else {
                holder.date.setText("N/A");
            }
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        private String formatStatus(String status) {
            if ("COMPLETED".equals(status)) {
                return "Encrypted";
            } else if ("DECRYPTED".equals(status)) {
                return "Decrypted";
            } else if ("CANCELLED".equals(status)) {
                return "Cancelled";
            } else if ("FAILED".equals(status)) {
                return "Failed";
            }
            return status;
        }

        private void setStatusBackground(TextView textView, String status) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(12);

            if ("COMPLETED".equals(status)) {
                drawable.setColor(0xFF27ae60); // Green
            } else if ("DECRYPTED".equals(status)) {
                drawable.setColor(0xFFe74c3c); // Red
            } else if ("CANCELLED".equals(status)) {
                drawable.setColor(0xFF95a5a6); // Gray
            } else if ("FAILED".equals(status)) {
                drawable.setColor(0xFFe67e22); // Orange
            } else {
                drawable.setColor(0xFF3498db); // Blue
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

        class FileViewHolder extends RecyclerView.ViewHolder {
            TextView originalFileName, owner, status, newFileName, fileSize, date;

            public FileViewHolder(View itemView) {
                super(itemView);
                originalFileName = itemView.findViewById(R.id.original_file_name);
                owner = itemView.findViewById(R.id.owner_text);
                status = itemView.findViewById(R.id.status_badge);
                newFileName = itemView.findViewById(R.id.new_file_name);
                fileSize = itemView.findViewById(R.id.file_size);
                date = itemView.findViewById(R.id.date_text);
            }
        }
    }
}

