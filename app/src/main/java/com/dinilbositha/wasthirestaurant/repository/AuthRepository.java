package com.dinilbositha.wasthirestaurant.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void loginUser(String email,
                          String password,
                          MutableLiveData<Boolean> loadingLiveData,
                          MutableLiveData<Boolean> loginSuccessLiveData,
                          MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);
        loginSuccessLiveData.setValue(false);
        errorLiveData.setValue(null);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();

                    if (firebaseUser == null) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Login failed. User is null.");
                        return;
                    }

                    checkUserActiveStatus(
                            firebaseUser.getUid(),
                            loadingLiveData,
                            loginSuccessLiveData,
                            errorLiveData
                    );
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    loginSuccessLiveData.setValue(false);
                    errorLiveData.setValue(
                            e.getMessage() != null ? e.getMessage() : "Login failed"
                    );
                });
    }

    private void checkUserActiveStatus(String uid,
                                       MutableLiveData<Boolean> loadingLiveData,
                                       MutableLiveData<Boolean> loginSuccessLiveData,
                                       MutableLiveData<String> errorLiveData) {

        Log.d("LOGIN_DEBUG", "Checking user document for UID: " + uid);

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingLiveData.setValue(false);

                    Log.d("LOGIN_DEBUG", "Document exists: " + documentSnapshot.exists());

                    if (!documentSnapshot.exists()) {
                        firebaseAuth.signOut();
                        loginSuccessLiveData.setValue(false);
                        errorLiveData.setValue("User data not found.");
                        Log.e("LOGIN_DEBUG", "User document not found for UID: " + uid);
                        return;
                    }

                    Object rawActive = documentSnapshot.get("active");
                    Boolean active = getBooleanSafely(documentSnapshot, "active");

                    Log.d("LOGIN_DEBUG", "Raw active value: " + rawActive);
                    Log.d("LOGIN_DEBUG", "Raw active type: " +
                            (rawActive != null ? rawActive.getClass().getSimpleName() : "null"));
                    Log.d("LOGIN_DEBUG", "Parsed active value: " + active);

                    if (Boolean.TRUE.equals(active)) {
                        Log.d("LOGIN_DEBUG", "User is active. Login allowed.");
                        loginSuccessLiveData.setValue(true);
                    } else {
                        firebaseAuth.signOut();
                        loginSuccessLiveData.setValue(false);
                        errorLiveData.setValue("Your account is blocked.");
                        Log.e("LOGIN_DEBUG", "User is NOT active. Login blocked.");
                    }
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    firebaseAuth.signOut();
                    loginSuccessLiveData.setValue(false);
                    errorLiveData.setValue(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to verify account status."
                    );

                    Log.e("LOGIN_DEBUG", "Failed to fetch user document: " + e.getMessage(), e);
                });
    }
    private Boolean getBooleanSafely(DocumentSnapshot documentSnapshot, String fieldName) {
        Object value = documentSnapshot.get(fieldName);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return false;
    }

    public void resetPassword(String email,
                              MutableLiveData<Boolean> loadingLiveData,
                              MutableLiveData<Boolean> resetSuccessLiveData,
                              MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);
        resetSuccessLiveData.setValue(false);
        errorLiveData.setValue(null);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    loadingLiveData.setValue(false);
                    resetSuccessLiveData.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    resetSuccessLiveData.setValue(false);
                    errorLiveData.setValue(
                            e.getMessage() != null ? e.getMessage() : "Password reset failed"
                    );
                });
    }

    public void registerUser(String name,
                             String email,
                             String password,
                             MutableLiveData<Boolean> loadingLiveData,
                             MutableLiveData<Boolean> registerSuccessLiveData,
                             MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);
        registerSuccessLiveData.setValue(false);
        errorLiveData.setValue(null);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();

                    if (firebaseUser == null) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Registration failed. User is null.");
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    User user = User.builder()
                            .uid(uid)
                            .name(name)
                            .email(email)
                            .role("customer")
                            .active(true)
                            .build();

                    firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                loadingLiveData.setValue(false);
                                firebaseAuth.signOut();
                                registerSuccessLiveData.setValue(true);
                            })
                            .addOnFailureListener(e -> {
                                loadingLiveData.setValue(false);
                                registerSuccessLiveData.setValue(false);
                                errorLiveData.setValue(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to save user data."
                                );
                            });
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    registerSuccessLiveData.setValue(false);

                    if (e instanceof FirebaseAuthUserCollisionException) {
                        errorLiveData.setValue("This email is already registered. Please log in.");
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        errorLiveData.setValue("Password is too weak.");
                    } else {
                        errorLiveData.setValue(
                                e.getMessage() != null ? e.getMessage() : "Registration failed"
                        );
                    }
                });
    }
}