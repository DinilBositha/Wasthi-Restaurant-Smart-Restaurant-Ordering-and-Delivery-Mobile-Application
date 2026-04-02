package com.dinilbositha.wasthirestaurant.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.adapter.IntroAdapter;
import com.dinilbositha.wasthirestaurant.databinding.ActivityIntroBinding;
import com.dinilbositha.wasthirestaurant.model.IntroSlide;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ActivityIntroBinding activityIntroBinding;
    private ViewPager2 viewPager;
    private Button btnNext;
    private WormDotsIndicator wormDotsIndicator;

    private static final String PREF_NAME = "wasthi_prefs";
    private static final String KEY_INTRO_SEEN = "intro_seen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // app eka kalin open karala intro balala thiyenawada kiyala balanna
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isIntroSeen = preferences.getBoolean(KEY_INTRO_SEEN, false);

        if (isIntroSeen) {
            startActivity(new Intent(IntroActivity.this, SignUpActivity.class));
            finish();
            return;
        }

        activityIntroBinding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(activityIntroBinding.getRoot());

        viewPager = activityIntroBinding.viewPager;
        btnNext = activityIntroBinding.btnNext;
        wormDotsIndicator = activityIntroBinding.wormDotsIndicator;

        List<IntroSlide> slides = new ArrayList<>();
        slides.add(new IntroSlide(
                R.drawable.anushka2,
                "දැන් සැපද",
                "Taste the Real Explosion!\nSpicy. Tasty. Deadly.",
                "Next"
        ));

        slides.add(new IntroSlide(
                R.drawable.dulaj,
                "අඩේ.. අප්පා..",
                "A Feast for Kings\nExperience the Royal Taste.",
                "Next"
        ));

        slides.add(new IntroSlide(
                R.drawable.nayani,
                "බලන්න නයනි.",
                "The ultimate hangout spot where great vibes meet sizzling flavors—perfect for those looking for a dining experience like no other.",
                "Get Started"
        ));

        IntroAdapter adapter = new IntroAdapter(slides);
        viewPager.setAdapter(adapter);
        wormDotsIndicator.attachTo(viewPager);

        // first button text
        btnNext.setText(slides.get(0).getButtonText());

        btnNext.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();

            if (currentPosition < slides.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
            } else {
                saveFirstTimeStatus();
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                btnNext.setText(slides.get(position).getButtonText());
            }
        });
    }

    private void saveFirstTimeStatus() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_INTRO_SEEN, true);
        editor.apply();
    }
}