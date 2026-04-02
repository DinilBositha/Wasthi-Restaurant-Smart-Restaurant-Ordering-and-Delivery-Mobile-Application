package com.dinilbositha.wasthirestaurant.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dinilbositha.wasthirestaurant.R;

public class SpalshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
           getWindow().setDecorFitsSystemWindows(false);
           WindowInsetsController controller = getWindow().getInsetsController();
           if(controller != null){
               controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
           }

       }else{
           getWindow().setFlags(
                   WindowManager.LayoutParams.FLAG_FULLSCREEN,
                   WindowManager.LayoutParams.FLAG_FULLSCREEN
           );
       }
        setContentView(R.layout.activity_spalsh);
        ImageView imageView = findViewById(R.id.spalshLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.app_logo)
                .override(300)
                .into(imageView);
        new Handler().postDelayed(()->{
            startActivity(new Intent(SpalshActivity.this, IntroActivity.class));
        finish();
        },3000);
    }
}