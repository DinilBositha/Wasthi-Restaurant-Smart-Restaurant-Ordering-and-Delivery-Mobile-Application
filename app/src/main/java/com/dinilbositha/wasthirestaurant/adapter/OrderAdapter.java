package com.dinilbositha.wasthirestaurant.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ItemOrderBinding;
import com.dinilbositha.wasthirestaurant.model.CartItem;
import com.dinilbositha.wasthirestaurant.model.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private int expandedPosition = -1;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.binding.txtOrderId.setText("Order #" + safeText(order.getOrderId()));
        holder.binding.txtOrderStatus.setText(safeText(order.getOrderStatus()));
        holder.binding.txtOrderDate.setText(formatDate(order.getCreatedAt()));

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.binding.txtOrderItems.setText(itemCount + " item" + (itemCount == 1 ? "" : "s"));
        holder.binding.txtOrderTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", order.getTotal()));

        boolean isExpanded = position == expandedPosition;
        holder.binding.layoutItemsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.binding.imgExpand.setRotation(isExpanded ? 180f : 0f);

        if (isExpanded) {
            renderPreviewItems(holder.binding.layoutItemsContainer, order.getItems());
        } else {
            holder.binding.layoutItemsContainer.removeAllViews();
        }

        holder.binding.layoutOrderHeader.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (expandedPosition == currentPosition) {
                expandedPosition = -1;
                notifyItemChanged(currentPosition);
            } else {
                int previousPosition = expandedPosition;
                expandedPosition = currentPosition;

                notifyItemChanged(currentPosition);
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private void renderPreviewItems(LinearLayout container, List<CartItem> items) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(container.getContext());

        for (CartItem item : items) {
            View itemView = inflater.inflate(R.layout.item_order_preview, container, false);

            ImageView imgFood = itemView.findViewById(R.id.imgFood);
            TextView txtFoodName = itemView.findViewById(R.id.txtFoodName);
            TextView txtVariant = itemView.findViewById(R.id.txtVariant);
            TextView txtQty = itemView.findViewById(R.id.txtQty);
            TextView txtPrice = itemView.findViewById(R.id.txtPrice);

            txtFoodName.setText(safeText(item.getProductTitle()));
            txtVariant.setText(buildVariantText(item.getSelectedVariants()));
            txtQty.setText("Qty: " + item.getQuantity());
            txtPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getUnitPrice()));

            if (!TextUtils.isEmpty(item.getProductImage())) {
                Glide.with(container.getContext())
                        .load(item.getProductImage())
                        .placeholder(R.drawable.app_logo)
                        .error(R.drawable.error_food)
                        .into(imgFood);
            } else {
                imgFood.setImageResource(R.drawable.cart);
            }

            container.addView(itemView);
        }
    }

    private String buildVariantText(List<CartItem.SelectedVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return "No variant";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < variants.size(); i++) {
            CartItem.SelectedVariant variant = variants.get(i);

            if (variant == null) continue;

            if (builder.length() > 0) {
                builder.append(", ");
            }

            String type = variant.getType() != null ? variant.getType() : "";
            String name = variant.getName() != null ? variant.getName() : "";

            if (!type.isEmpty() && !name.isEmpty()) {
                builder.append(type).append(": ").append(name);
            } else if (!name.isEmpty()) {
                builder.append(name);
            } else if (!type.isEmpty()) {
                builder.append(type);
            }
        }

        return builder.length() == 0 ? "No variant" : builder.toString();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;

        public OrderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
