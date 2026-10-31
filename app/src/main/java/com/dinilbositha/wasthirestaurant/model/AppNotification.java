package com.dinilbositha.wasthirestaurant.model;

import com.google.firebase.Timestamp;

public class AppNotification {
    private String id;
    private String title;
    private String body;
    private Timestamp timestamp;
    private boolean read;

    public AppNotification() {
        // Required empty constructor for Firestore
    }

    public AppNotification(String id, String title, String body, Timestamp timestamp, boolean read) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
