package com.example.cryptofile_android.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class FileInfo {
    @DocumentId
    private String fileId;
    private String userId;
    private String originalFileName;
    private long originalFileSize;
    private String originalFileHash;
    private String username;

    private String newFileName;
    private long newFileSize;
    private String encryptedFileHash;

    private String status;
    @ServerTimestamp// e.g., "encrypted", "decrypted"
    private Date encryptedAt;
    private Date decryptedAt;

    public FileInfo() {}

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

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public long getOriginalFileSize() {
        return originalFileSize;
    }

    public void setOriginalFileSize(long originalFileSize) {
        this.originalFileSize = originalFileSize;
    }

    public String getOriginalFileHash() {
        return originalFileHash;
    }

    public void setOriginalFileHash(String originalFileHash) {
        this.originalFileHash = originalFileHash;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public long getNewFileSize() {
        return newFileSize;
    }

    public void setNewFileSize(long newFileSize) {
        this.newFileSize = newFileSize;
    }

    public String getEncryptedFileHash() {
        return encryptedFileHash;
    }

    public void setEncryptedFileHash(String encryptedFileHash) {
        this.encryptedFileHash = encryptedFileHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEncryptedAt() {
        return encryptedAt;
    }

    public void setEncryptedAt(Date encryptedAt) {
        this.encryptedAt = encryptedAt;
    }

    public Date getDecryptedAt() {
        return decryptedAt;
    }

    public void setDecryptedAt(Date decryptedAt) {
        this.decryptedAt = decryptedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getModifiedAt() {
        if ("COMPLETED".equals(status) && encryptedAt != null) {
            return encryptedAt;
        } else if ("DECRYPTED".equals(status) && decryptedAt != null) {
            return decryptedAt;
        }
        return null;
    }
}
