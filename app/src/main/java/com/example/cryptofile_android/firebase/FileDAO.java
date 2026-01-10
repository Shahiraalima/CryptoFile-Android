package com.example.cryptofile_android.firebase;

import com.example.cryptofile_android.models.FileInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class FileDAO {
    private static FileDAO instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_FILES = "files";

    private FileDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FileDAO getInstance() {
        if (instance == null) {
            instance = new FileDAO();
        }
        return instance;
    }

    public CompletableFuture<String> saveFile(FileInfo fileInfo) {
        CompletableFuture<String> future = new CompletableFuture<>();
        db.collection(COLLECTION_FILES)
                .add(fileInfo)
                .addOnSuccessListener(documentReference ->
                        future.complete(documentReference.getId()))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<String> updateForReencryption(String userId, String originalFileHash,
                                                         String newFileName, long newFileSize,
                                                         String encryptedFileHash) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.collection(COLLECTION_FILES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("originalFileHash", originalFileHash)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        db.collection(COLLECTION_FILES)
                                .document(docId)
                                .update(
                                        "newFileName", newFileName,
                                        "newFileSize", newFileSize,
                                        "encryptedFileHash", encryptedFileHash,
                                        "status", "COMPLETED",
                                        "encryptedAt", com.google.firebase.Timestamp.now(),
                                        "decryptedAt", null
                                )
                                .addOnSuccessListener(aVoid -> future.complete(docId))
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        future.completeExceptionally(new Exception("File not found for re-encryption"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> validityEncryption(String encryptedFileHash, String userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.collection(COLLECTION_FILES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("encryptedFileHash", encryptedFileHash)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        FileInfo fileInfo = querySnapshot.getDocuments().get(0).toObject(FileInfo.class);
                        future.complete("COMPLETED".equals(fileInfo.getStatus()));
                    } else {
                        future.complete(false);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<String> checkFileExists(String originalFileHash, String userId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.collection(COLLECTION_FILES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("originalFileHash", originalFileHash)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        FileInfo fileInfo = querySnapshot.getDocuments().get(0).toObject(FileInfo.class);
                        future.complete(fileInfo.getStatus()); // Returns status: COMPLETED, DECRYPTED, etc.
                    } else {
                        future.complete("NOT_FOUND");
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> updateForDecryption(String userId, String encryptedFileHash,
                                                       String newFileName, long newFileSize) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (userId == null || encryptedFileHash == null) {
            future.completeExceptionally(new IllegalArgumentException("userId and encryptedFileHash cannot be null"));
            return future;
        }

        db.collection(COLLECTION_FILES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("encryptedFileHash", encryptedFileHash)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty() && querySnapshot.getDocuments().get(0) != null) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        if (docId != null && !docId.isEmpty()) {
                            db.collection(COLLECTION_FILES)
                                    .document(docId)
                                    .update(
                                            "newFileName", newFileName,
                                            "newFileSize", newFileSize,
                                            "encryptedFileHash", null,
                                            "status", "DECRYPTED",
                                            "decryptedAt", com.google.firebase.Timestamp.now()
                                    )
                                    .addOnSuccessListener(aVoid -> future.complete(null))
                                    .addOnFailureListener(future::completeExceptionally);
                        } else {
                            future.completeExceptionally(new Exception("Document ID is null"));
                        }
                    } else {
                        future.completeExceptionally(new Exception("File not found for decryption"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


}
