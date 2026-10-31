package com.dinilbositha.wasthirestaurant.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.model.Product;

import java.util.List;

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ViewHolder> {
    private List<Product> products;
    private OnListingItemClickListener listener;

    public ProductHomeAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productName.setText(product.getProductTitle());
        holder.productCategory.setText(product.getCategoryName());
        holder.productPrice.setText(String.format("LKR %.2f", product.getBasePrice()));

        String imageUrl = (product.getProductImage() != null && !product.getProductImage().isEmpty()) 
                ? product.getProductImage().get(0) : null;

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.error_food)
                    .centerCrop()
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.cart);
        }

        holder.itemView.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(),R.anim.click_animation);
            view.setAnimation(animation);
            if(listener != null){
                listener.onListingItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName;
        TextView productPrice;

        TextView productCategory;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            productImage = itemView.findViewById(R.id.homeProductImg);
            productName = itemView.findViewById(R.id.homeProductName);
            productPrice = itemView.findViewById(R.id.homeProductPrice);
            productCategory = itemView.findViewById(R.id.homeCategoryName);
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
    }
}
