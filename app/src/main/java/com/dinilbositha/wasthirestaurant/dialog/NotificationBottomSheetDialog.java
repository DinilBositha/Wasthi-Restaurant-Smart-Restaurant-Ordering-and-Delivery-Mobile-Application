package com.dinilbositha.wasthirestaurant.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dinilbositha.wasthirestaurant.adapter.NotificationAdapter;
import com.dinilbositha.wasthirestaurant.databinding.BottomSheetNotificationsBinding;
import com.dinilbositha.wasthirestaurant.model.AppNotification;
import com.dinilbositha.wasthirestaurant.utils.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationBottomSheetDialog extends BottomSheetDialogFragment
        implements NotificationAdapter.OnNotificationActionListener {

    private static final String TAG = "NotificationDialog";

    private BottomSheetNotificationsBinding binding;
    private NotificationAdapter adapter;
    private ListenerRegistration notificationsListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            dismiss();
            return;
        }
        currentUserId = currentUser.getUid();

        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnMarkAllRead.setOnClickListener(v -> {
            if (currentUserId != null) {
                NotificationHelper.markAllAsRead(currentUserId);
            }
        });
    }

    private void loadNotifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        notificationsListener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || binding == null) return;

                    binding.progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Log.e(TAG, "Failed to listen for notifications", e);
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        adapter.setNotifications(new ArrayList<>());
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                        binding.txtUnreadCountBadge.setVisibility(View.GONE);
                        binding.btnMarkAllRead.setVisibility(View.GONE);
                    } else {
                        List<AppNotification> list = new ArrayList<>();
                        int unreadCount = 0;

                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            AppNotification notif = doc.toObject(AppNotification.class);
                            if (notif != null) {
                                if (notif.getId() == null) {
                                    notif.setId(doc.getId());
                                }
                                if (!notif.isRead()) {
                                    unreadCount++;
                                }
                                list.add(notif);
                            }
                        }

                        adapter.setNotifications(list);
                        binding.layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

                        if (unreadCount > 0) {
                            binding.txtUnreadCountBadge.setVisibility(View.VISIBLE);
                            binding.txtUnreadCountBadge.setText(unreadCount + " new");
                            binding.btnMarkAllRead.setVisibility(View.VISIBLE);
                        } else {
                            binding.txtUnreadCountBadge.setVisibility(View.GONE);
                            binding.btnMarkAllRead.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onNotificationClick(AppNotification notification) {
        if (!notification.isRead() && currentUserId != null && notification.getId() != null) {
            NotificationHelper.markAsRead(currentUserId, notification.getId());
        }
    }

    @Override
    public void onNotificationDelete(AppNotification notification) {
        if (currentUserId != null && notification.getId() != null) {
            NotificationHelper.deleteNotification(currentUserId, notification.getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
        binding = null;
    }
}
