package com.dinilbositha.wasthirestaurantadmin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurantadmin.R;
import com.dinilbositha.wasthirestaurantadmin.databinding.ItemOrderAdminBinding;
import com.dinilbositha.wasthirestaurantadmin.model.Order;
import com.dinilbositha.wasthirestaurantadmin.model.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    public interface OnOrderStatusClickListener {
        void onOrderStatusClick(Order order);
    }

    private final List<Order> fullList = new ArrayList<>();
    private final OnOrderStatusClickListener listener;
    private int expandedPosition = -1;

    public AdminOrderAdapter(OnOrderStatusClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderAdminBinding binding = ItemOrderAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = fullList.get(position);

        holder.binding.txtOrderId.setText("Order #" + safe(order.getOrderId()));
        holder.binding.txtCustomerName.setText(safe(order.getCustomerName()));
        holder.binding.txtOrderTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotal()));
        holder.binding.txtOrderDate.setText(formatDate(order.getCreatedAt()));
        holder.binding.txtOrderMeta.setText(buildOrderMeta(order));

        setStatusButtonStyle(holder, order.getOrderStatus());

        boolean isExpanded = position == expandedPosition;
        holder.binding.layoutItemsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            renderOrderItems(holder.binding.layoutItemsContainer, order.getItems());
        } else {
            holder.binding.layoutItemsContainer.removeAllViews();
        }

        holder.binding.layoutOrderSummary.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            int previousExpanded = expandedPosition;

            if (expandedPosition == currentPosition) {
                expandedPosition = -1;
            } else {
                expandedPosition = currentPosition;
            }

            if (previousExpanded != -1) {
                notifyItemChanged(previousExpanded);
            }
            notifyItemChanged(currentPosition);
        });

        holder.binding.btnOrderStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderStatusClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fullList.size();
    }

    public void updateData(List<Order> newList) {
        fullList.clear();
        if (newList != null) {
            fullList.addAll(newList);
        }
        expandedPosition = -1;
        notifyDataSetChanged();
    }

    private void renderOrderItems(LinearLayout container, List<OrderItem> items) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        for (OrderItem item : items) {
            View itemView = inflater.inflate(R.layout.item_order_food_preview, container, false);

            ImageView imgFood = itemView.findViewById(R.id.imgFood);
            TextView txtFoodName = itemView.findViewById(R.id.txtFoodName);
            TextView txtFoodVariants = itemView.findViewById(R.id.txtFoodVariants);
            TextView txtFoodQty = itemView.findViewById(R.id.txtFoodQty);

            txtFoodName.setText(safe(item.getProductTitle()));
            txtFoodVariants.setText(buildVariantText(item.getSelectedVariants()));
            txtFoodQty.setText("Qty: " + item.getQuantity());

            if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
                Glide.with(container.getContext())
                        .load(item.getProductImage())
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.app_logo)
                        .into(imgFood);
            } else {
                imgFood.setImageResource(R.drawable.app_logo);
            }

            container.addView(itemView);
        }
    }

    private String buildVariantText(List<OrderItem.SelectedVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return "No variants";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < variants.size(); i++) {
            OrderItem.SelectedVariant variant = variants.get(i);

            if (variant == null) continue;

            String type = variant.getType() != null ? variant.getType() : "";
            String name = variant.getName() != null ? variant.getName() : "";

            if (builder.length() > 0) {
                builder.append(", ");
            }

            if (!type.isEmpty() && !name.isEmpty()) {
                builder.append(type).append(": ").append(name);
            } else if (!name.isEmpty()) {
                builder.append(name);
            } else {
                builder.append(type);
            }
        }

        return builder.length() == 0 ? "No variants" : builder.toString();
    }

    private String buildOrderMeta(Order order) {
        if (order == null) return "";

        if ("TABLE_ORDER".equalsIgnoreCase(order.getOrderType())) {
            return "Table: " + safe(order.getTableNumber());
        } else if ("DELIVERY".equalsIgnoreCase(order.getOrderType())) {
            return "Delivery: " + safe(order.getDeliveryAddress());
        }

        return "";
    }

    private void setStatusButtonStyle(OrderViewHolder holder, String status) {
        String safeStatus = safe(status);
        holder.binding.btnOrderStatus.setText(safeStatus);

        int color;
        switch (safeStatus.toLowerCase(Locale.getDefault())) {
            case "pending":
                color = Color.parseColor("#FF9800");
                break;
            case "preparing":
                color = Color.parseColor("#3F51B5");
                break;
            case "ready":
                color = Color.parseColor("#009688");
                break;
            case "delivered":
            case "served":
                color = Color.parseColor("#4CAF50");
                break;
            case "cancelled":
                color = Color.parseColor("#F44336");
                break;
            default:
                color = Color.parseColor("#9E9E9E");
                break;
        }

        holder.binding.btnOrderStatus.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(long time) {
        if (time <= 0) return "";
        return new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                .format(new Date(time));
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderAdminBinding binding;

        public OrderViewHolder(@NonNull ItemOrderAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}