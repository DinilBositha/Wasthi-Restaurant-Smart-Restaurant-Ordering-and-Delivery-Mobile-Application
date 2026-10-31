package com.dinilbositha.wasthirestaurant.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ItemNotificationBinding;
import com.dinilbositha.wasthirestaurant.model.AppNotification;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationActionListener {
        void onNotificationClick(AppNotification notification);
        void onNotificationDelete(AppNotification notification);
    }

    private final List<AppNotification> notifications = new ArrayList<>();
    private final OnNotificationActionListener listener;

    public NotificationAdapter(OnNotificationActionListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<AppNotification> newNotifications) {
        this.notifications.clear();
        if (newNotifications != null) {
            this.notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AppNotification item) {
            Context context = itemView.getContext();

            binding.txtNotificationTitle.setText(item.getTitle() != null ? item.getTitle() : "Notification");
            binding.txtNotificationBody.setText(item.getBody() != null ? item.getBody() : "");
            binding.txtNotificationTime.setText(formatTimestamp(item.getTimestamp()));

            if (item.isRead()) {
                // Read / Seen state
                binding.dotUnread.setVisibility(View.GONE);
                binding.txtSeenStatus.setVisibility(View.VISIBLE);
                binding.txtNotificationTitle.setTypeface(null, Typeface.NORMAL);
                binding.cardNotification.setStrokeColor(
                        ContextCompat.getColor(context, R.color.md_theme_outlineVariant)
                );
                binding.cardNotification.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.md_theme_surface)
                );
            } else {
                // Unread state
                binding.dotUnread.setVisibility(View.VISIBLE);
                binding.txtSeenStatus.setVisibility(View.GONE);
                binding.txtNotificationTitle.setTypeface(null, Typeface.BOLD);
                binding.cardNotification.setStrokeColor(
                        ContextCompat.getColor(context, R.color.md_theme_primary)
                );
                binding.cardNotification.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.md_theme_surfaceContainerLow)
                );
            }

            binding.cardNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(item);
                }
            });

            binding.btnDeleteNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationDelete(item);
                }
            });
        }

        private String formatTimestamp(Timestamp timestamp) {
            if (timestamp == null) return "";
            long diffMillis = System.currentTimeMillis() - timestamp.toDate().getTime();
            if (diffMillis < 0) diffMillis = 0;

            long seconds = diffMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days == 1) {
                return "Yesterday";
            } else if (days < 7) {
                return days + "d ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return sdf.format(timestamp.toDate());
            }
        }
    }
}
