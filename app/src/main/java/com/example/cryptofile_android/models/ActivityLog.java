package com.example.cryptofile_android.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ActivityLog {
    @DocumentId
    private String logId;
    private String fileId;
    private String userId;
    private String action; //Encrypt or Decrypt
    private String status; // Success, Failed and cancelled
    private String fileName;
    private long fileSize;
    @ServerTimestamp
    private Date timestamp;

    public ActivityLog() {
    }

    public ActivityLog(String logId, String fileId, String userId, String action, String status, String fileName, long fileSize) {
        this.logId = logId;
        this.fileId = fileId;
        this.userId = userId;
        this.action = action;
        this.status = status;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
