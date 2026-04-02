package com.dinilbositha.wasthirestaurant.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurant.databinding.ItemRecentSearchBinding;

import java.util.List;

public class SearchKeywordAdapter extends RecyclerView.Adapter<SearchKeywordAdapter.KeywordViewHolder> {

    public interface OnKeywordClickListener {
        void onKeywordClick(String keyword);
    }

    private final List<String> keywordList;
    private final OnKeywordClickListener listener;

    public SearchKeywordAdapter(List<String> keywordList, OnKeywordClickListener listener) {
        this.keywordList = keywordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecentSearchBinding binding = ItemRecentSearchBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new KeywordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        String keyword = keywordList.get(position);
        holder.binding.txtRecentItem.setText(keyword);
        holder.itemView.setOnClickListener(v -> listener.onKeywordClick(keyword));
    }

    @Override
    public int getItemCount() {
        return keywordList == null ? 0 : keywordList.size();
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {
        ItemRecentSearchBinding binding;

        public KeywordViewHolder(ItemRecentSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}