package com.example.cryptofile_android.firebase;

import com.example.cryptofile_android.models.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class UserDAO {
    private static UserDAO instance;
    private final FirebaseFirestore db;
    private static final String COLLECTION_USERS = "users";

    private UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    public CompletableFuture<UserInfo> loginVerify(String username, String password) {
        CompletableFuture<UserInfo> future = new CompletableFuture<>();

        db.collection(COLLECTION_USERS)
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        UserInfo user = querySnapshot.getDocuments().get(0).toObject(UserInfo.class);
                        future.complete(user);
                    } else {
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> registerUser(UserInfo user) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Boolean> isUsernameTaken(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        db.collection(COLLECTION_USERS)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    future.complete(!querySnapshot.isEmpty());
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

}
