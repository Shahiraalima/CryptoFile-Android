package com.example.cryptofile_android.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserInfo {
    @DocumentId
    private String userId;
    private String username;
    private String email;
    private String role; // "User" or "Admin"
    private String password;
    private String fullName;
    @ServerTimestamp
    private Date createdAt;
    private boolean active;

    public UserInfo() {
    }

    public UserInfo(String userId, String username, String email, String role, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        this.fullName = fullName;
        this.active = true;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
