package com.dinilbositha.wasthirestaurant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.model.IntroSlide;

import java.util.List;

public class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.ViewHolder> {
    List<IntroSlide> introSlides;
    public IntroAdapter(List<IntroSlide> slides){
        this.introSlides = slides;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
IntroSlide slide = introSlides.get(position);
holder.imageView.setImageResource(slide.getImage());
holder.titleTxt.setText(slide.getTitle());
holder.descTxt.setText(slide.getDesc());
    }


    @Override
    public int getItemCount() {
        return introSlides.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView titleTxt;
        TextView descTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSlide);
            titleTxt = itemView.findViewById(R.id.txtTitle);
            descTxt = itemView.findViewById(R.id.txtDesc);
        }
    }

}
