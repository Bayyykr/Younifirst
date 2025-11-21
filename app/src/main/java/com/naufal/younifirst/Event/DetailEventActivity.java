package com.naufal.younifirst.Event;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.ColorUtils;
import androidx.core.content.ContextCompat;

import com.naufal.younifirst.R;

public class DetailEventActivity extends AppCompatActivity {

    private ConstraintLayout headerContainer;
    private ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_event);
        headerContainer = findViewById(R.id.header_container);
        scrollView = findViewById(R.id.scrollContent);

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            int threshold = 100;

            if (scrollY >= threshold) {
                headerContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
                headerContainer.setElevation(8f);
            } else {
                float alpha = (float) scrollY / threshold;
                int color = ContextCompat.getColor(this, R.color.primary_color);
                int colorWithAlpha = ColorUtils.setAlphaComponent(color, Math.round(alpha * 255));
                headerContainer.setBackgroundColor(colorWithAlpha);
                headerContainer.setElevation(alpha * 8f);
            }
        });
    }
}
