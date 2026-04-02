package com.dinilbositha.wasthirestaurant.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "order_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            if (title == null || title.isEmpty()) {
                title = data.get("title");
            }

            if (body == null || body.isEmpty()) {
                body = data.get("body");
            }
        }

        if (title == null || title.isEmpty()) {
            title = "Order Update";
        }

        if (body == null || body.isEmpty()) {
            body = "Your order status has been updated";
        }

        saveNotificationToFirestore(title, body);
        sendNotification(title, body);
    }

    private void saveNotificationToFirestore(String title, String body) {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
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
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved to Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification", e));
        } else {
            Log.w(TAG, "User not logged in, notification not saved");
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update token", e));
        } else {
            Log.w(TAG, "User not logged in, token not updated");
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_logo)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for order updates");
            channel.enableVibration(true);
            channel.enableLights(true);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}