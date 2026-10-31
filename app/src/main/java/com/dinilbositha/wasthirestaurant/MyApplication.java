package com.dinilbositha.wasthirestaurant;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        FirebaseApp.initializeApp(this);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
        );

        // Cloudinary Initialization
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "sgaznrjn");
        config.put("secure", true);
        MediaManager.init(this, config);
    }
}