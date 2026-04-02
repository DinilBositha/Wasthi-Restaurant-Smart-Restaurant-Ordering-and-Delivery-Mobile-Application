package com.dinilbositha.wasthirestaurant.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.model.HomeAds;

import java.util.List;

public class HomeAdsAdapter extends RecyclerView.Adapter<HomeAdsAdapter.Viewholder> {

    private final List<HomeAds> homeAds;

    public HomeAdsAdapter(List<HomeAds> adsList) {
        this.homeAds = adsList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_ads, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        HomeAds ad = homeAds.get(position);
        
        if (ad != null && !TextUtils.isEmpty(ad.getImageUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(ad.getImageUrl())
                    .placeholder(R.drawable.test)
                    .error(R.drawable.app_logo)
                    .centerCrop()
                    .into(holder.homeImgView);
        } else {
            holder.homeImgView.setImageResource(R.drawable.app_logo);
        }
    }

    @Override
    public int getItemCount() {
        return homeAds == null ? 0 : homeAds.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        ImageView homeImgView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            homeImgView = itemView.findViewById(R.id.homeAdsImg);
        }
    }
}
