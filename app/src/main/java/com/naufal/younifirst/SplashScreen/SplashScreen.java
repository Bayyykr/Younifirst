package com.naufal.younifirst.SplashScreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.naufal.younifirst.Home.MainActivity;
import com.naufal.younifirst.R;
import com.naufal.younifirst.opening.opening;

public class SplashScreen extends AppCompatActivity {

    private ImageView logo;
    private View lingkarang_splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.logo);
        lingkarang_splash = findViewById(R.id.lingkarang_splashscreen);

        lingkarang_splash.setVisibility(View.INVISIBLE);

        // Jalankan animasi logo setelah layout tampil
        logo.post(this::mulaianimasilogo);
    }

    private void mulaianimasilogo() {
        // Mulai dari posisi bawah
        float startY = logo.getY() + 2000;
        logo.setY(startY);

        logo.setScaleX(0f);
        logo.setScaleY(0f);

        ObjectAnimator moveY = ObjectAnimator.ofFloat(logo, "y", startY, logo.getY() - 2000);
        moveY.setDuration(1200);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);
        scaleX.setDuration(1200);
        scaleY.setDuration(1200);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeIn.setDuration(1200);

        AnimatorSet logoAnimator = new AnimatorSet();
        logoAnimator.playTogether(moveY, scaleX, scaleY, fadeIn);
        logoAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mulaianimasilingkaran();
            }
        });
        logoAnimator.start();
    }

    private void mulaianimasilingkaran() {
        lingkarang_splash.setVisibility(View.VISIBLE);

        // Animasi lingkaran membesar
        ObjectAnimator scaleXCircle = ObjectAnimator.ofFloat(lingkarang_splash, "scaleX", 0f, 20f);
        ObjectAnimator scaleYCircle = ObjectAnimator.ofFloat(lingkarang_splash, "scaleY", 0f, 20f);
        ObjectAnimator fadeInCircle = ObjectAnimator.ofFloat(lingkarang_splash, "alpha", 0f, 1f);

        // Animasi logo mengecil ke tengah
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 0f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 0f);
        ObjectAnimator fadeOutLogo = ObjectAnimator.ofFloat(logo, "alpha", 1f, 0f);

        AnimatorSet circleAndLogoAnimator = new AnimatorSet();
        circleAndLogoAnimator.playTogether(scaleXCircle, scaleYCircle, fadeInCircle,
                scaleXLogo, scaleYLogo, fadeOutLogo);
        circleAndLogoAnimator.setDuration(1200);

        circleAndLogoAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // ✅ Cek status login setelah animasi selesai
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

                Intent intent;
                if (isLoggedIn) {
                    intent = new Intent(SplashScreen.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashScreen.this, opening.class);
                }

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        circleAndLogoAnimator.start();
    }
}
