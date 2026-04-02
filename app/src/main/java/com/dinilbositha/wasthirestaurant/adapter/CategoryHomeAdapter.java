package com.dinilbositha.wasthirestaurant.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.model.Category;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class CategoryHomeAdapter extends RecyclerView.Adapter<CategoryHomeAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private List<Category> categoryList;
    private FirebaseStorage storage;
    private OnCategoryClickListener listener;

    public CategoryHomeAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categoryList = categories;
        this.listener = listener;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public CategoryHomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_categoty, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHomeAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getCategoryName());

        if (category != null && !TextUtils.isEmpty(category.getImgUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImgUrl())
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.error_food)
                    .centerCrop()
                    .into(holder.categoryImg);
        } else {
            holder.categoryImg.setImageResource(R.drawable.app_logo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImg;
        TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImg = itemView.findViewById(R.id.categoryImg);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
