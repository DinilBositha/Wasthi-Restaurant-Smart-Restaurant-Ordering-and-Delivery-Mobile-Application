package com.dinilbositha.wasthirestaurantadmin.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurantadmin.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final String TAG = "USER_DEBUG";

    private final FirebaseFirestore firestore;

    public UserRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getAllUsers(MutableLiveData<Boolean> loadingLiveData,
                            MutableLiveData<List<User>> usersLiveData,
                            MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);
        Log.d(TAG, "Fetching users from Firestore...");

        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingLiveData.setValue(false);

                    List<User> userList = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            // uid missing නම් document id එකෙන් ගන්න
                            if (user.getUid() == null || user.getUid().isEmpty()) {
                                user.setUid(document.getId());
                            }
                            userList.add(user);
                        }
                    }

                    Log.d(TAG, "Users fetched successfully. Count: " + userList.size());
                    usersLiveData.setValue(userList);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    Log.e(TAG, "Error fetching users", e);
                    errorLiveData.setValue("Failed to load users: " + e.getMessage());
                });
    }

    public void updateUserStatus(String uid,
                                 boolean active,
                                 MutableLiveData<String> messageLiveData,
                                 MutableLiveData<String> errorLiveData) {

        firestore.collection("users")
                .document(uid)
                .update("active", active)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "User status updated: " + uid);
                    messageLiveData.setValue("User status updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user status", e);
                    errorLiveData.setValue("Failed to update user status.");
                });
    }
}