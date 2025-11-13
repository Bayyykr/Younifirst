package com.naufal.younifirst.Bantuan;

import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import com.naufal.younifirst.R;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private View dot1, dot2;
    private final int flipInterval = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pusat_bantuan);

        viewFlipper = findViewById(R.id.viewFlipperBantuan);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);

        viewFlipper.setInAnimation(null);
        viewFlipper.setOutAnimation(null);

        viewFlipper.setFlipInterval(flipInterval);
        viewFlipper.startFlipping();

        viewFlipper.getInAnimation();
        viewFlipper.getOutAnimation();

        updateDots(0);
    }

    private void updateDots(int position) {
        if (position == 0) {
            setDotActive(dot1, true);
            setDotActive(dot2, false);
        } else {
            setDotActive(dot1, false);
            setDotActive(dot2, true);
        }
    }

    private void setDotActive(View dot, boolean isActive) {
        dot.setBackgroundResource(isActive ? R.drawable.circle_dot_two : R.drawable.circle_dot);
    }
}
