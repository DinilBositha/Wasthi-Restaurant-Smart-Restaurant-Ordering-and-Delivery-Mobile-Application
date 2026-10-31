package com.dinilbositha.wasthirestaurant.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ItemCartBinding;
import com.dinilbositha.wasthirestaurant.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
        void onDelete(CartItem item);
    }

    private final List<CartItem> cartItems = new ArrayList<>();
    private final CartListener listener;

    public CartAdapter(CartListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> list) {
        cartItems.clear();
        if (list != null) {
            cartItems.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.binding.cartProductName.setText(item.getProductTitle());
        holder.binding.cartproductQty.setText(String.valueOf(item.getQuantity()));
        holder.binding.cartProductPrice.setText(
                String.format("LKR %.2f", item.getUnitPrice() * item.getQuantity())
        );

        String variantText = getVariantDisplayText(item.getSelectedVariants());
        if (TextUtils.isEmpty(variantText)) {
            holder.binding.txtVariant.setVisibility(View.GONE);
        } else {
            holder.binding.txtVariant.setVisibility(View.VISIBLE);
            holder.binding.txtVariant.setText(variantText);
        }

        if (!TextUtils.isEmpty(item.getProductImage())) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getProductImage())
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.error_food)
                    .into(holder.binding.imgProduct);
        } else {
            holder.binding.imgProduct.setImageResource(R.drawable.cart);
        }

        if (!item.isAvailable()) {
            holder.binding.getRoot().setAlpha(0.5f);
            holder.binding.txtVariant.setText(
                    TextUtils.isEmpty(variantText)
                            ? item.getValidationMessage()
                            : variantText + "\n" + item.getValidationMessage()
            );
            holder.binding.txtVariant.setVisibility(View.VISIBLE);
            holder.binding.txtVariant.setTextColor(Color.RED);
            holder.binding.btnPlus.setEnabled(false);
            holder.binding.btnMinus.setEnabled(false);
        } else {
            holder.binding.getRoot().setAlpha(1f);
            holder.binding.txtVariant.setTextColor(Color.parseColor("#7E849D"));
            holder.binding.btnPlus.setEnabled(true);
            holder.binding.btnMinus.setEnabled(true);
        }

        holder.binding.btnPlus.setOnClickListener(v -> listener.onIncrease(item));
        holder.binding.btnMinus.setOnClickListener(v -> listener.onDecrease(item));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private String getVariantDisplayText(List<CartItem.SelectedVariant> selectedVariants) {
        if (selectedVariants == null || selectedVariants.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < selectedVariants.size(); i++) {
            CartItem.SelectedVariant variant = selectedVariants.get(i);

            builder.append(variant.getType())
                    .append(": ")
                    .append(variant.getName());

            if (i < selectedVariants.size() - 1) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ItemCartBinding binding;

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}