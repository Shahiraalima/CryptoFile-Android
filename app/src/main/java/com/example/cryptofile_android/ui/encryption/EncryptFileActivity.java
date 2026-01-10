package com.example.cryptofile_android.ui.encryption;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.ActivityDAO;
import com.example.cryptofile_android.firebase.FileDAO;
import com.example.cryptofile_android.models.ActivityLog;
import com.example.cryptofile_android.models.FileInfo;
import com.example.cryptofile_android.ui.utils.EncryptDecrypt;
import com.example.cryptofile_android.ui.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class EncryptFileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialCardView uploadCard;
    private Button encryptButton;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText confirmPasswordInput;

    private ListView filesListView;
    private Button removeAllButton;
    private Button cancelAllButton;
    private TextView fileCountLabel;
    private TextView statusLabel;

    private final List<Uri> selectedFileUris = new ArrayList<>();
    private final List<String> displayItems = new ArrayList<>();
    private ArrayAdapter<String> filesAdapter;

    private volatile boolean cancelRequested = false;
    private boolean isEncrypting = false;

    private FileDAO fileDAO;
    private ActivityDAO logDAO;


    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        ClipData clip = data.getClipData();
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            if (uri != null) selectedFileUris.add(uri);
                        }
                    } else if (data.getData() != null) {
                        Uri uri = data.getData();
                        selectedFileUris.add(uri);
                    }

                    if (!selectedFileUris.isEmpty()) {
                        displaySelectedFiles();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openFilePicker();
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_file);

        fileDAO = FileDAO.getInstance();
        logDAO = ActivityDAO.getInstance();

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        uploadCard = findViewById(R.id.upload_card);
        encryptButton = findViewById(R.id.encrypt_button);
        passwordLayout = findViewById(R.id.password_layout);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);

        filesListView = findViewById(R.id.list_view);
        removeAllButton = findViewById(R.id.remove_all_button);
        cancelAllButton = findViewById(R.id.cancel_all_button);
        fileCountLabel = findViewById(R.id.file_count_label);
        statusLabel = findViewById(R.id.status_label);

        filesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayItems);
        filesListView.setAdapter(filesAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Encrypt File");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        uploadCard.setOnClickListener(v -> checkPermissionAndOpenPicker());

        removeAllButton.setOnClickListener(v -> {
            if (!isEncrypting) {
                selectedFileUris.clear();
                displayItems.clear();
                filesAdapter.notifyDataSetChanged();
                updateFileCountLabel();
                removeAllButton.setVisibility(View.GONE);
                encryptButton.setEnabled(false);
                adjustListViewHeight();
            }
        });

        filesListView.setOnItemClickListener((parent, view, position, id) -> {
            Uri uri = selectedFileUris.get(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        encryptButton.setOnClickListener(v -> handleEncryption());

        cancelAllButton.setOnClickListener(v -> {
            cancelRequested = true;
            statusLabel.setText("Cancelling...");
            cancelAllButton.setEnabled(false);
        });

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void checkPermissionAndOpenPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    private void displaySelectedFiles() {
        displayItems.clear();
        for (Uri uri : selectedFileUris) {
            displayItems.add(getFileName(uri));
        }
        filesAdapter.notifyDataSetChanged();
        updateFileCountLabel();
        removeAllButton.setVisibility(selectedFileUris.isEmpty() ? View.GONE : View.VISIBLE);
        encryptButton.setEnabled(!selectedFileUris.isEmpty());
        adjustListViewHeight();
    }

    private void adjustListViewHeight() {
        if (displayItems.isEmpty()) {
            ViewGroup.LayoutParams params = filesListView.getLayoutParams();
            params.height = 0;
            filesListView.setLayoutParams(params);
            return;
        }

        int itemHeight = 80;
        int maxItems = 6;
        int itemCount = Math.min(displayItems.size(), maxItems);

        float density = getResources().getDisplayMetrics().density;
        int heightInDp = itemHeight * itemCount;
        int heightInPx = (int) (heightInDp * density);

        ViewGroup.LayoutParams params = filesListView.getLayoutParams();
        params.height = heightInPx;
        filesListView.setLayoutParams(params);
    }

    private void updateFileCountLabel() {
        fileCountLabel.setText("Selected Files (" + selectedFileUris.size() + ")");
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            String path = uri.getPath();
            if (path == null) return uri.toString();
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                result = path.substring(cut + 1);
            } else result = path;
        }
        return result;
    }

    private void handleEncryption() {
        String password = passwordInput.getText() == null ? "" : passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText() == null ? "" : confirmPasswordInput.getText().toString().trim();

        if (selectedFileUris.isEmpty()) {
            Toast.makeText(this, "Please select at least one file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            return;
        }

        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        performEncryption(password);
    }

    private void performEncryption(String password) {
        cancelRequested = false;
        isEncrypting = true;

        displayItems.clear();
        for (int i = 0; i < selectedFileUris.size(); i++) {
            String name = getFileName(selectedFileUris.get(i));
            displayItems.add(name + " - Pending");
        }
        filesAdapter.notifyDataSetChanged();
        adjustListViewHeight();

        statusLabel.setVisibility(View.VISIBLE);
        statusLabel.setText("Starting encryption...");
        removeAllButton.setVisibility(View.GONE);
        cancelAllButton.setVisibility(View.VISIBLE);
        cancelAllButton.setEnabled(true);
        encryptButton.setEnabled(false);
        uploadCard.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String userId = SessionManager.loggedInUser.getUserId();

                for (int i = 0; i < selectedFileUris.size(); i++) {
                    if (cancelRequested) {
                        final int idx = i;
                        final Uri cancelledUri = selectedFileUris.get(idx);
                        final String cancelledFileName = getFileName(cancelledUri);

                        runOnUiThread(() -> {
                            displayItems.set(idx, cancelledFileName + " - Cancelled");
                            filesAdapter.notifyDataSetChanged();
                            statusLabel.setText("Cancelled by user");
                        });

                        try {
                            ActivityLog cancelLog = new ActivityLog();
                            cancelLog.setUserId(userId);
                            cancelLog.setAction("ENCRYPT");
                            cancelLog.setStatus("CANCELLED");
                            cancelLog.setFileName(cancelledFileName);
                            cancelLog.setFileSize(0);
                            logDAO.logActivity(cancelLog);
                        } catch (Exception ignored) {}

                        break;
                    }

                    Uri uri = selectedFileUris.get(i);
                    File tempInputFile = createTempFile();
                    copyUriToFile(uri, tempInputFile);

                    String fileName = getFileName(uri);
                    long originalFileSize = tempInputFile.length();
                    String originalHash = calculateFileHash(tempInputFile);

                    File outputFile = new File(getExternalFilesDir(null), fileName + ".enc");

                    String fileStatus = fileDAO.checkFileExists(originalHash, userId).get();
                    boolean isReencryption = !"NOT_FOUND".equals(fileStatus);

                    if (isReencryption) {
                        final int idx = i;
                        runOnUiThread(() -> {
                            displayItems.set(idx, fileName + " - Re-encrypting (file exists)...");
                            filesAdapter.notifyDataSetChanged();
                        });
                    }

                    final int index = i;
                    runOnUiThread(() -> {
                        displayItems.set(index, fileName + " - Encrypting...");
                        filesAdapter.notifyDataSetChanged();
                        statusLabel.setText("Encrypting " + fileName);
                    });

                    EncryptDecrypt.encryptFile(
                            tempInputFile.getAbsolutePath(),
                            outputFile.getAbsolutePath(),
                            password,
                            progress -> runOnUiThread(() -> {
                                int percentage = (int) (progress * 100);
                                displayItems.set(index, fileName + " - Encrypting... " + percentage + "%");
                                filesAdapter.notifyDataSetChanged();
                                statusLabel.setText("Encrypting " + fileName + " - " + percentage + "%");
                            })
                    );

                    String encryptedHash = calculateFileHash(outputFile);
                    long encryptedFileSize = outputFile.length();

                    String fileId;
                    if (isReencryption) {
                        fileDAO.updateForReencryption(
                                userId,
                                originalHash,
                                outputFile.getName(),
                                encryptedFileSize,
                                encryptedHash
                        ).get();
                        fileId = null;
                    } else {
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setUserId(userId);
                        fileInfo.setOriginalFileName(fileName);
                        fileInfo.setOriginalFileSize(originalFileSize);
                        fileInfo.setOriginalFileHash(originalHash);
                        fileInfo.setNewFileName(outputFile.getName());
                        fileInfo.setNewFileSize(encryptedFileSize);
                        fileInfo.setEncryptedFileHash(encryptedHash);
                        fileInfo.setStatus("COMPLETED");

                        fileId = fileDAO.saveFile(fileInfo).get().toString();
                    }

                    ActivityLog log = new ActivityLog();
                    if (fileId != null) {
                        log.setFileId(fileId);
                    }
                    log.setUserId(userId);
                    log.setAction(isReencryption ? "RE-ENCRYPT" : "ENCRYPT");
                    log.setStatus("SUCCESS");
                    log.setFileName(fileName);
                    log.setFileSize(encryptedFileSize);
                    logDAO.logActivity(log);

                    runOnUiThread(() -> {
                        displayItems.set(index, fileName + " - ✓ Completed");
                        filesAdapter.notifyDataSetChanged();
                    });

                    tempInputFile.delete();
                }

                runOnUiThread(() -> {
                    isEncrypting = false;
                    statusLabel.setText("✓ All operations completed successfully!");
                    cancelAllButton.setVisibility(View.GONE);
                    uploadCard.setEnabled(true);

                    Toast.makeText(this, "Encryption completed!", Toast.LENGTH_LONG).show();

                    filesListView.postDelayed(() -> {
                        selectedFileUris.clear();
                        displayItems.clear();
                        filesAdapter.notifyDataSetChanged();
                        updateFileCountLabel();
                        statusLabel.setVisibility(View.GONE);
                        encryptButton.setEnabled(false);
                        adjustListViewHeight();
                    }, 2000);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    isEncrypting = false;
                    statusLabel.setText("❌ Encryption failed: " + e.getMessage());
                    statusLabel.setVisibility(View.VISIBLE);
                    cancelAllButton.setVisibility(View.GONE);
                    removeAllButton.setVisibility(View.VISIBLE);
                    uploadCard.setEnabled(true);
                    Toast.makeText(this, "Encryption failed", Toast.LENGTH_SHORT).show();
                });

                try {
                    String userId = SessionManager.loggedInUser.getUserId();
                    for (Uri uri : selectedFileUris) {
                        ActivityLog log = new ActivityLog();
                        log.setUserId(userId);
                        log.setAction("ENCRYPT");
                        log.setStatus("FAILED");
                        log.setFileName(getFileName(uri));
                        log.setFileSize(0);
                        logDAO.logActivity(log);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    private File createTempFile() throws Exception {
        return File.createTempFile("temp_", ".tmp", getCacheDir());
    }

    private void copyUriToFile(Uri uri, File destination) throws Exception {
        try (InputStream in = getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private String calculateFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
