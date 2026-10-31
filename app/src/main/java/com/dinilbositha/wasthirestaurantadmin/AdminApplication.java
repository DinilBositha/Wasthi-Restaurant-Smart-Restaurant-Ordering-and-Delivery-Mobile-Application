package com.dinilbositha.wasthirestaurantadmin;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class AdminApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        // --- Cloudinary Initialization ---
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "sgaznrjn");
        config.put("secure", true);
        MediaManager.init(this, config);
    }
}
