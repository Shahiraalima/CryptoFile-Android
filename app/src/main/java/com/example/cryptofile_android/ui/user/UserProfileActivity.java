package com.example.cryptofile_android.ui.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.UserDAO;
import com.example.cryptofile_android.models.UserInfo;
import com.example.cryptofile_android.ui.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class UserProfileActivity extends AppCompatActivity {

    private TextInputLayout fullNameLayout, emailLayout;
    private TextInputEditText fullNameInput, emailInput;
    private TextView usernameText, accountCreatedText;
    private TextView infoMessageLabel;
    private Button saveInfoButton, cancelInfoButton;

    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private TextInputEditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private TextView passwordMessageLabel;
    private Button changePasswordButton, cancelPasswordButton;

    private UserDAO userDAO;
    private FirebaseAuth firebaseAuth;

    private String originalFullName;
    private String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userDAO = UserDAO.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        fullNameLayout = findViewById(R.id.full_name_layout);
        emailLayout = findViewById(R.id.email_layout);
        fullNameInput = findViewById(R.id.full_name_input);
        emailInput = findViewById(R.id.email_input);
        usernameText = findViewById(R.id.username_text);
        accountCreatedText = findViewById(R.id.account_created_text);
        infoMessageLabel = findViewById(R.id.info_message_label);
        saveInfoButton = findViewById(R.id.save_info_button);
        cancelInfoButton = findViewById(R.id.cancel_info_button);
        currentPasswordLayout = findViewById(R.id.current_password_layout);
        newPasswordLayout = findViewById(R.id.new_password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        currentPasswordInput = findViewById(R.id.current_password_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        passwordMessageLabel = findViewById(R.id.password_message_label);
        changePasswordButton = findViewById(R.id.change_password_button);
        cancelPasswordButton = findViewById(R.id.cancel_password_button);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String userId = SessionManager.loggedInUser.getUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserInfo user = userDAO.getUserById(userId).get();
                if (user != null) {
                    runOnUiThread(() -> {
                        originalFullName = user.getFullName() != null ? user.getFullName() : "";
                        originalEmail = user.getEmail() != null ? user.getEmail() : "";

                        fullNameInput.setText(originalFullName);
                        emailInput.setText(originalEmail);
                        usernameText.setText(user.getUsername());

                        if (user.getCreatedAt() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                            accountCreatedText.setText(sdf.format(user.getCreatedAt()));
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupListeners() {
        saveInfoButton.setOnClickListener(v -> handleSaveInfo());
        cancelInfoButton.setOnClickListener(v -> handleCancelInfo());

        changePasswordButton.setOnClickListener(v -> handleChangePassword());
        cancelPasswordButton.setOnClickListener(v -> handleCancelPassword());

        newPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newPass = newPasswordInput.getText() != null ? newPasswordInput.getText().toString() : "";
                String confirmPass = s.toString();

                if (!confirmPass.isEmpty() && !confirmPass.equals(newPass)) {
                    confirmPasswordLayout.setError("Passwords do not match");
                } else {
                    confirmPasswordLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleSaveInfo() {
        String newFullName = fullNameInput.getText() != null ? fullNameInput.getText().toString().trim() : "";
        String newEmail = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";

        if (newFullName.isEmpty()) {
            fullNameLayout.setError("Full name is required");
            return;
        }

        if (newEmail.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            emailLayout.setError("Invalid email address");
            return;
        }

        fullNameLayout.setError(null);
        emailLayout.setError(null);

        saveInfoButton.setEnabled(false);
        infoMessageLabel.setVisibility(View.VISIBLE);
//        infoMessageLabel.setTextColor(getColor(R.color.secondary_text));
        infoMessageLabel.setText("Updating information...");

        String userId = SessionManager.loggedInUser.getUserId();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(userId);
                userInfo.setFullName(newFullName);
                userInfo.setEmail(newEmail);
                userDAO.updateUserInfo(userInfo).get();

                runOnUiThread(() -> {
                    originalFullName = newFullName;
                    originalEmail = newEmail;

//                    infoMessageLabel.setTextColor(getColor(R.color.success));
                    infoMessageLabel.setText("✓ Information updated successfully!");
                    saveInfoButton.setEnabled(true);

                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    infoMessageLabel.postDelayed(() ->
                            infoMessageLabel.setVisibility(View.GONE), 3000);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    infoMessageLabel.setTextColor(getColor(R.color.error));
                    infoMessageLabel.setText("❌ Failed to update information. Please try again.");
                    saveInfoButton.setEnabled(true);

                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleCancelInfo() {
        fullNameInput.setText(originalFullName);
        emailInput.setText(originalEmail);
        fullNameLayout.setError(null);
        emailLayout.setError(null);
        infoMessageLabel.setVisibility(View.GONE);
    }

    private void handleChangePassword() {
        String currentPass = currentPasswordInput.getText() != null ? currentPasswordInput.getText().toString() : "";
        String newPass = newPasswordInput.getText() != null ? newPasswordInput.getText().toString() : "";
        String confirmPass = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";

        if (currentPass.isEmpty()) {
            currentPasswordLayout.setError("Current password is required");
            return;
        }

        if (newPass.isEmpty()) {
            newPasswordLayout.setError("New password is required");
            return;
        }

        if (newPass.length() < 8) {
            newPasswordLayout.setError("Password must be at least 8 characters");
            return;
        }

        if (!isStrongPassword(newPass)) {
            newPasswordLayout.setError("Password does not meet strength requirements");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            confirmPasswordLayout.setError("Passwords do not match");
            return;
        }

        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Show loading
        changePasswordButton.setEnabled(false);
        passwordMessageLabel.setVisibility(View.VISIBLE);
//        passwordMessageLabel.setTextColor(getColor(R.color.secondary_text));
        passwordMessageLabel.setText("Changing password...");

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            passwordMessageLabel.setTextColor(getColor(R.color.error));
            passwordMessageLabel.setText("❌ User not authenticated");
            changePasswordButton.setEnabled(true);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                com.google.firebase.auth.AuthCredential credential =
                        com.google.firebase.auth.EmailAuthProvider.getCredential(
                                firebaseUser.getEmail(), currentPass);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                runOnUiThread(() -> {
//                                    passwordMessageLabel.setTextColor(getColor(R.color.success));
                                    passwordMessageLabel.setText("✓ Password changed successfully!");
                                    changePasswordButton.setEnabled(true);

                                    currentPasswordInput.setText("");
                                    newPasswordInput.setText("");
                                    confirmPasswordInput.setText("");

                                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();

                                    passwordMessageLabel.postDelayed(() ->
                                            passwordMessageLabel.setVisibility(View.GONE), 3000);
                                });
                            } else {
                                runOnUiThread(() -> {
                                    passwordMessageLabel.setTextColor(getColor(R.color.error));
                                    passwordMessageLabel.setText("❌ Failed to change password. Please try again.");
                                    changePasswordButton.setEnabled(true);
                                });
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            currentPasswordLayout.setError("Current password is incorrect");
                            passwordMessageLabel.setTextColor(getColor(R.color.error));
                            passwordMessageLabel.setText("❌ Current password is incorrect");
                            changePasswordButton.setEnabled(true);
                        });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    passwordMessageLabel.setTextColor(getColor(R.color.error));
                    passwordMessageLabel.setText("❌ Failed to change password. Please try again.");
                    changePasswordButton.setEnabled(true);
                });
            }
        });
    }

    private void handleCancelPassword() {
        currentPasswordInput.setText("");
        newPasswordInput.setText("");
        confirmPasswordInput.setText("");
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        passwordMessageLabel.setVisibility(View.GONE);
    }

    private void validatePasswordStrength(String password) {
        if (password.isEmpty()) {
            newPasswordLayout.setError(null);
            return;
        }

        if (password.length() < 8) {
            newPasswordLayout.setError("Too short");
            return;
        }

        if (!isStrongPassword(password)) {
            newPasswordLayout.setError("Weak password");
            return;
        }

        newPasswordLayout.setError(null);
        newPasswordLayout.setHelperText("Strong password ✓");
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}


