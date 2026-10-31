package com.dinilbositha.wasthirestaurant.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dinilbositha.wasthirestaurant.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static void showNotification(Context context,
                                        String channelId,
                                        String channelName,
                                        String channelDescription,
                                        int notificationId,
                                        String title,
                                        String message,
                                        Intent intent) {

        createNotificationChannel(context, channelId, channelName, channelDescription);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission missing", e);
        }

        saveNotificationToFirestore(title, message);
    }

    public static void createNotificationChannel(Context context,
                                                 String channelId,
                                                 String channelName,
                                                 String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(channelDescription);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void saveNotificationToFirestore(String title, String body) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Log.d(TAG, "User not logged in, notification not saved to Firestore");
            return;
        }

        String id = UUID.randomUUID().toString();
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", id);
        notification.put("title", title);
        notification.put("body", body);
        notification.put("timestamp", Timestamp.now());
        notification.put("read", false);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(id)
                .set(notification)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved to Firestore: " + title))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification to Firestore", e));
    }

    public static void markAsRead(String userId, String notificationId) {
        if (userId == null || notificationId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read: " + notificationId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read", e));
    }

    public static void markAllAsRead(String userId) {
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) return;

                    WriteBatch batch = db.batch();
                    queryDocumentSnapshots.getDocuments().forEach(doc -> {
                        batch.update(doc.getReference(), "read", true);
                    });

                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "All notifications marked as read"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to mark all notifications as read", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch unread notifications", e));
    }

    public static void deleteNotification(String userId, String notificationId) {
        if (userId == null || notificationId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification deleted: " + notificationId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete notification", e));
    }
}