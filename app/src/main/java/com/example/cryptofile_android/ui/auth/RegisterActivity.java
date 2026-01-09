package com.example.cryptofile_android.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.UserDAO;
import com.example.cryptofile_android.models.UserInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink, errorText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        userDAO = UserDAO.getInstance();

        usernameLayout = findViewById(R.id.username_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);
        errorText = findViewById(R.id.error_text);
        progressBar = findViewById(R.id.progress_bar);


        registerButton.setOnClickListener(v -> handleRegister());
        loginLink.setOnClickListener(v -> {
            finish();
        });
    }

    private void handleRegister() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!validateInput(username, email, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        userDAO.isUsernameTaken(username).thenAccept(isTaken -> {
            if (isTaken) {
                showLoading(false);
                errorText.setText("Username already exists");
                errorText.setVisibility(View.VISIBLE);
            } else {
                createUserWithFirebase(username, email, password);
            }
        }).exceptionally(throwable -> {
            showLoading(false);
            errorText.setText("Error checking username: " + throwable.getMessage());
            errorText.setVisibility(View.VISIBLE);
            return null;
        });


    }

    private void createUserWithFirebase(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserDocument(firebaseUser.getUid(), username, email, password);
                        }
                    } else {
                        showLoading(false);
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        errorText.setText(errorMessage);
                        errorText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void createUserDocument(String userId, String username, String email, String password) {
        UserInfo user = new UserInfo();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("User");
        user.setActive(true);

        userDAO.registerUser(user)
                .thenAccept(aVoid -> {

                    showLoading(false);
                    Toast.makeText(this, "Registration successful! Please login.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .exceptionally(throwable -> {
                    showLoading(false);
                    errorText.setText("Error creating user profile: " + throwable.getMessage());
                    errorText.setVisibility(View.VISIBLE);
                    return null;
                });
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        boolean isValid = true;

        // Clear previous errors
        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        errorText.setVisibility(View.GONE);

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 3) {
            usernameLayout.setError("Username must be at least 3 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError("Please confirm password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
        usernameInput.setEnabled(!show);
        emailInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
        confirmPasswordInput.setEnabled(!show);
    }
}


