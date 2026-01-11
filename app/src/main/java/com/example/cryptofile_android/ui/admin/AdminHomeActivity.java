package com.example.cryptofile_android.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.cryptofile_android.R;
import com.example.cryptofile_android.ui.auth.LoginActivity;
import com.example.cryptofile_android.ui.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView welcomeText;

    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAuth = FirebaseAuth.getInstance();

        if (!SessionManager.loggedInUser.getRole().equals("Admin")) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupNavigationDrawer();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        welcomeText = findViewById(R.id.welcome_text);

        findViewById(R.id.card_manage_users).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManagementActivity.class)));

        findViewById(R.id.card_manage_files).setOnClickListener(v ->
                startActivity(new Intent(this, AdminFileManagementActivity.class)));

        findViewById(R.id.card_activity_logs).setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivityLogsActivity.class)));

        findViewById(R.id.card_admin_profile).setOnClickListener(v ->
                startActivity(new Intent(this, AdminProfileActivity.class)));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }
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
        TextView navRole = headerView.findViewById(R.id.nav_role);

        if (SessionManager.loggedInUser.getUsername() != null) {
            navUsername.setText(SessionManager.loggedInUser.getUsername());
            navEmail.setText(SessionManager.loggedInUser.getEmail());
            navRole.setText("Administrator");
        }
    }

    private void handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.nav_user_management) {
            startActivity(new Intent(this, AdminUserManagementActivity.class));
        } else if (itemId == R.id.nav_file_management) {
            startActivity(new Intent(this, AdminFileManagementActivity.class));
        } else if (itemId == R.id.nav_activity_logs) {
            startActivity(new Intent(this, AdminActivityLogsActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, AdminProfileActivity.class));
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

