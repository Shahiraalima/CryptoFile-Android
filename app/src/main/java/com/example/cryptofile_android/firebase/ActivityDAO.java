package com.example.cryptofile_android.firebase;

import com.example.cryptofile_android.models.ActivityLog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ActivityDAO {
    private static ActivityDAO instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_LOGS = "activity_logs";

    public ActivityDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized ActivityDAO getInstance() {
        if (instance == null) {
            instance = new ActivityDAO();
        }
        return instance;
    }

    public CompletableFuture<String> logActivity(ActivityLog log) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.collection(COLLECTION_LOGS)
                .add(log)
                .addOnSuccessListener(documentReference ->
                        future.complete(documentReference.getId()))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<List<ActivityLog>> getLogsByUserId(String userId) {
        CompletableFuture<List<ActivityLog>> future = new CompletableFuture<>();

        db.collection(COLLECTION_LOGS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ActivityLog> logs = querySnapshot.toObjects(ActivityLog.class);
                    future.complete(logs);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public CompletableFuture<List<ActivityLog>> getLogsByUserId(String userId, int limit) {
        CompletableFuture<List<ActivityLog>> future = new CompletableFuture<>();

        db.collection(COLLECTION_LOGS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ActivityLog> logs = querySnapshot.toObjects(ActivityLog.class);
                    future.complete(logs);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Integer> getLogsCountByUserId(String userId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.collection(COLLECTION_LOGS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot ->
                        future.complete(querySnapshot.size()))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }







}
