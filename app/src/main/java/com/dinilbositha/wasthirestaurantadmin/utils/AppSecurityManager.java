package com.dinilbositha.wasthirestaurantadmin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import androidx.biometric.BiometricManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AppSecurityManager {

    private static final String PREFS_NAME = "wasthi_admin_security_prefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";
    private static final String KEY_PASSCODE_HASH = "passcode_hash";
    private static final String KEY_PASSCODE_SALT = "passcode_salt";
    private static final String KEY_SAVED_EMAIL = "saved_auth_email";
    private static final String KEY_SAVED_PASSWORD = "saved_auth_password";

    private static AppSecurityManager instance;
    private final SharedPreferences preferences;

    private AppSecurityManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AppSecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppSecurityManager(context);
        }
        return instance;
    }

    // --- Biometric Hardware & Enrollment Check ---
    public boolean isBiometricSupported(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        int canAuth = biometricManager.canAuthenticate(authenticators);
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS || canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
    }

    public boolean isBiometricEnrolled(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    // --- Biometric Setting ---
    public boolean isBiometricEnabled() {
        return preferences.getBoolean(KEY_BIOMETRIC_ENABLED, true);
    }

    public void setBiometricEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply();
    }

    // --- App Launch Lock Setting ---
    public boolean isAppLockEnabled() {
        return preferences.getBoolean(KEY_APP_LOCK_ENABLED, false);
    }

    public void setAppLockEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply();
    }

    // --- Passcode Management (SHA-256 with Salt) ---
    public boolean isPasscodeSet() {
        return !TextUtils.isEmpty(preferences.getString(KEY_PASSCODE_HASH, null));
    }

    public void setPasscode(String passcode) {
        if (TextUtils.isEmpty(passcode)) {
            preferences.edit()
                    .remove(KEY_PASSCODE_HASH)
                    .remove(KEY_PASSCODE_SALT)
                    .apply();
            return;
        }

        String salt = String.valueOf(System.currentTimeMillis());
        String hash = hashPasscode(passcode, salt);

        preferences.edit()
                .putString(KEY_PASSCODE_HASH, hash)
                .putString(KEY_PASSCODE_SALT, salt)
                .apply();
    }

    public boolean verifyPasscode(String inputPasscode) {
        String savedHash = preferences.getString(KEY_PASSCODE_HASH, null);
        String savedSalt = preferences.getString(KEY_PASSCODE_SALT, null);

        if (TextUtils.isEmpty(savedHash) || TextUtils.isEmpty(savedSalt)) {
            return false;
        }

        String inputHash = hashPasscode(inputPasscode, savedSalt);
        return savedHash.equals(inputHash);
    }

    public void removePasscode() {
        preferences.edit()
                .remove(KEY_PASSCODE_HASH)
                .remove(KEY_PASSCODE_SALT)
                .apply();
    }

    private String hashPasscode(String passcode, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String toHash = salt + "$" + passcode;
            byte[] hashBytes = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hashBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf((passcode + salt).hashCode());
        }
    }

    // --- Saved Credentials for Quick Biometric Login ---
    public void saveUserCredentials(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            return;
        }

        String encodedEmail = Base64.encodeToString(email.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String encodedPassword = Base64.encodeToString(password.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        preferences.edit()
                .putString(KEY_SAVED_EMAIL, encodedEmail)
                .putString(KEY_SAVED_PASSWORD, encodedPassword)
                .apply();
    }

    public String getSavedEmail() {
        String encoded = preferences.getString(KEY_SAVED_EMAIL, null);
        if (TextUtils.isEmpty(encoded)) return null;
        try {
            return new String(Base64.decode(encoded, Base64.NO_WRAP), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public String getSavedPassword() {
        String encoded = preferences.getString(KEY_SAVED_PASSWORD, null);
        if (TextUtils.isEmpty(encoded)) return null;
        try {
            return new String(Base64.decode(encoded, Base64.NO_WRAP), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasSavedCredentials() {
        return !TextUtils.isEmpty(getSavedEmail()) && !TextUtils.isEmpty(getSavedPassword());
    }

    public void clearSavedCredentials() {
        preferences.edit()
                .remove(KEY_SAVED_EMAIL)
                .remove(KEY_SAVED_PASSWORD)
                .apply();
    }

    public boolean isQuickLoginAvailable(Context context) {
        return hasSavedCredentials() && isBiometricEnrolled(context) && isBiometricEnabled();
    }
}
