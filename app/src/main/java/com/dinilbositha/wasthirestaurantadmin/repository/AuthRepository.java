package com.dinilbositha.wasthirestaurantadmin.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void loginAdmin(String email,
                           String password,
                           MutableLiveData<Boolean> loadingLiveData,
                           MutableLiveData<Boolean> loginSuccessLiveData,
                           MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser == null) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Login failed. User is null.");
                        return;
                    }

                    checkAdminAccess(firebaseUser.getUid(),
                            loadingLiveData,
                            loginSuccessLiveData,
                            errorLiveData);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(
                            e.getMessage() != null ? e.getMessage() : "Login failed"
                    );
                });
    }

    private void checkAdminAccess(String uid,
                                  MutableLiveData<Boolean> loadingLiveData,
                                  MutableLiveData<Boolean> loginSuccessLiveData,
                                  MutableLiveData<String> errorLiveData) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingLiveData.setValue(false);

                    if (!documentSnapshot.exists()) {
                        firebaseAuth.signOut();
                        errorLiveData.setValue("User record not found.");
                        return;
                    }

                    String role = documentSnapshot.getString("role");
                    Boolean active = documentSnapshot.getBoolean("active");

                    if (!"admin".equals(role)) {
                        firebaseAuth.signOut();
                        errorLiveData.setValue("Access denied. Not an admin.");
                        return;
                    }

                    if (!Boolean.TRUE.equals(active)) {
                        firebaseAuth.signOut();
                        errorLiveData.setValue("Your account is disabled.");
                        return;
                    }

                    loginSuccessLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    firebaseAuth.signOut();
                    errorLiveData.setValue("Failed to verify admin access.");
                });
    }
}