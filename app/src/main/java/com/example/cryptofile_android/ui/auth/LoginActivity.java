package com.example.cryptofile_android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.firebase.UserDAO;
import com.example.cryptofile_android.models.UserInfo;
import com.example.cryptofile_android.ui.admin.AdminHomeActivity;
import com.example.cryptofile_android.ui.user.UserHomeActivity;
import com.example.cryptofile_android.ui.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout, passwordLayout;
    private TextInputEditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView registerLink, errorText;

    private FirebaseAuth mAuth;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userDAO = UserDAO.getInstance();


        usernameLayout = findViewById(R.id.username_layout);
        passwordLayout = findViewById(R.id.password_layout);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        errorText = findViewById(R.id.error_text);


        loginButton.setOnClickListener(v -> handleLogin());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }

        usernameLayout.setError(null);
        passwordLayout.setError(null);
        errorText.setVisibility(View.GONE);

        userDAO.loginVerify(username,password)
                .thenAccept(user -> {
                    if (user != null && user.isActive()) {
                        mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            onLoginSuccess(user);
                                        }
                                    } else {
                                        errorText.setText("Invalid username or password");
                                        errorText.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        errorText.setText("Invalid username or password");
                        errorText.setVisibility(View.VISIBLE);
                    }
                })
                .exceptionally(throwable -> {
                    errorText.setText("Error while logging in. Please try again.");
                    errorText.setVisibility(View.VISIBLE);
                    return null;
                });
    }

    private void onLoginSuccess(UserInfo user) {
        SessionManager.loggedInUser = user;

        Toast.makeText(this, "Welcome, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();

        navigateToHome();
    }


    private void navigateToHome() {
        Intent intent;
        if (SessionManager.loggedInUser.getRole().equals("Admin")) {
            intent = new Intent(this, AdminHomeActivity.class);
        } else {
            intent = new Intent(this, UserHomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

