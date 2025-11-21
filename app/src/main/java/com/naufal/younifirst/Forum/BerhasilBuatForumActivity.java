package com.naufal.younifirst.Forum;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.R;

public class BerhasilBuatForumActivity extends AppCompatActivity {

    private View glassPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_berhasil_buat_forum);

        setupGlassPane();

        findViewById(R.id.back_to_mainactivity).setOnClickListener(v -> finish());

        findViewById(R.id.pengumuman).setOnClickListener(v ->
                startActivity(new Intent(this, ChatForumAdminActivity.class))
        );

        findViewById(R.id.anggotadanadmin).setOnClickListener(v ->
                startActivity(new Intent(this, ChatAnggotaDanAdminActivity.class))
        );

        ImageButton flagBtn = findViewById(R.id.flag);

        flagBtn.setOnClickListener(v -> showMenuPopup(flagBtn));
    }

    private void setupGlassPane() {
        FrameLayout root = findViewById(android.R.id.content);

        glassPane = new View(this);
        glassPane.setBackgroundColor(Color.parseColor("#40000000"));
        glassPane.setVisibility(View.GONE);

        root.addView(glassPane, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        glassPane.setOnClickListener(null);

    }

    private void showMenuPopup(View anchor) {
        View popupView = getLayoutInflater().inflate(R.layout.custom_popup_forum, null);

        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int popupWidth = popupView.getMeasuredWidth();

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setClippingEnabled(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(null);

        int marginRightPx = dpToPx(8);
        int xOffset = anchor.getWidth() - popupWidth - marginRightPx;

        if (xOffset < -anchor.getLeft()) {
            xOffset = -anchor.getLeft();
        }

        int yOffset = 20;

        popupWindow.showAsDropDown(anchor, xOffset, yOffset);

        popupView.findViewById(R.id.editforum).setOnClickListener(v -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, EditForumActivity.class));
        });

        popupView.findViewById(R.id.pengaturanforum).setOnClickListener(v ->
                popupWindow.dismiss()
        );

        popupView.findViewById(R.id.undanganggota).setOnClickListener(v -> {
            popupWindow.dismiss();
            showUndangPopup();
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showUndangPopup() {

        View popupView = getLayoutInflater().inflate(R.layout.custom_popup_undanganggota_forum, null);

        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setClippingEnabled(false);

        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(null);

        glassPane.setVisibility(View.VISIBLE);

        popupWindow.setOnDismissListener(() -> {
            glassPane.setVisibility(View.GONE);
            popupView.clearAnimation();
        });

        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            float startY = 0;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {

                switch (event.getAction()) {

                    case android.view.MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return true;

                    case android.view.MotionEvent.ACTION_MOVE:
                        float currentY = event.getY();
                        float distance = currentY - startY;

                        if (distance > 80) {

                            popupView.startAnimation(
                                    AnimationUtils.loadAnimation(
                                            BerhasilBuatForumActivity.this,
                                            android.R.anim.fade_out
                                    )
                            );

                            popupView.postDelayed(() -> popupWindow.dismiss(), 150);

                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }


}
