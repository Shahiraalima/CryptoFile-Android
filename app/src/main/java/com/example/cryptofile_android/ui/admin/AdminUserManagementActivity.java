package com.example.cryptofile_android.ui.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cryptofile_android.R;
import com.google.android.material.button.MaterialButton;

import java.util.Date;
import com.example.cryptofile_android.firebase.UserDAO;
import com.example.cryptofile_android.models.UserAdapter;


public class AdminUserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MaterialButton btnAddUser;
    private TextView totalUsersLabel;
    private TextView standardUsersLabel;
    private TextView adminsLabel;
    private TextView newUsersLabel;

    private UserAdapter userAdapter;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        userDAO = new UserDAO();

        initViews();
        setupRecyclerView();
        setupButtonListeners();
        loadUserData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view);
        btnAddUser = findViewById(R.id.btn_add_user);
        totalUsersLabel = findViewById(R.id.total_users_label);
        standardUsersLabel = findViewById(R.id.standard_users_label);
        adminsLabel = findViewById(R.id.admins_label);
        newUsersLabel = findViewById(R.id.new_users_label);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList -> {
            loadUserData();
        });
        recyclerView.setAdapter(userAdapter);
    }

    private void setupButtonListeners() {
        btnAddUser.setOnClickListener(v -> {
            AddUserDialogFragment dialog = new AddUserDialogFragment((userInfo) -> {
                userDAO.registerUser(userInfo);
                loadUserData();
            });
            dialog.show(getSupportFragmentManager(), "add_user_dialog");
        });
    }

    private void loadUserData() {
        userDAO.getAllUsers().thenAccept(users -> {
            if (users == null) return;

            int totalUsers = users.size();
            int standardUsers = (int) users.stream()
                    .filter(u -> "User".equals(u.getRole()))
                    .count();
            int admins = (int) users.stream()
                    .filter(u -> "Admin".equals(u.getRole()))
                    .count();
            int newUsers = (int) users.stream()
                    .filter(u -> isNewThisMonth(u.getCreatedAt()))
                    .count();

            runOnUiThread(() -> {
                totalUsersLabel.setText(String.valueOf(totalUsers));
                standardUsersLabel.setText(String.valueOf(standardUsers));
                adminsLabel.setText(String.valueOf(admins));
                newUsersLabel.setText(String.valueOf(newUsers));
                userAdapter.setUserList(users);
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    private boolean isNewThisMonth(Date createdAt) {
        long currentTime = System.currentTimeMillis();
        long oneMonthAgo = currentTime - (30L * 24 * 60 * 60 * 1000);
        return createdAt.getTime() > oneMonthAgo;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}

