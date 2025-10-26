package com.naufal.younifirst.Home;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naufal.younifirst.R;
import com.naufal.younifirst.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private LinearLayout fabMenuHome, fabMenuEvent, fabMenuKompetisi, fabMenuLostFound,fabMenuForum;
    private LinearLayout headerUtama, headerSecond, headerThird;
    private ImageView btnAdd;
    private View fadeBackground;
    private boolean isMenuVisible = false;
    private String currentTag = "home";
    private View btnAddContainer;
    private int colorAddDefault;
    private int colorAddActive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fabMenuHome = findViewById(R.id.fabMenuHome);
        fabMenuEvent = findViewById(R.id.fabMenuEvent);
        fabMenuKompetisi = findViewById(R.id.fabMenuKompetisi);
        fabMenuLostFound = findViewById(R.id.fabMenuLostFound);
        headerUtama = findViewById(R.id.header_utama);
        headerSecond = findViewById(R.id.header_second);
        headerThird = findViewById(R.id.header_third);
        btnAdd = findViewById(R.id.btnAdd);
        fadeBackground = findViewById(R.id.globalFadeBackground);
        btnAddContainer = findViewById(R.id.btnAddContainer);
        colorAddDefault = android.graphics.Color.parseColor("#3B5CCC");
        colorAddActive = android.graphics.Color.parseColor("#121A2C");

        hideAllMenusInstant();

        replaceFragment(new HomeFragment(), "home");
        updateFabMenuState();

        btnAdd.setOnClickListener(v -> toggleMenu());

        fadeBackground.setOnClickListener(v -> hideMenu());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            hideMenu();
            switch (item.getItemId()) {
                case R.id.nav_home:
                    replaceFragment(new HomeFragment(), "home");
                    break;
                case R.id.nav_event:
                    replaceFragment(new EventFragment(), "event");
                    break;
                case R.id.nav_forum:
                    replaceFragment(new ForumFragment(), "forum");
                    break;
                case R.id.nav_competition:
                    replaceFragment(new CompetitionTeamFragment(), "competition");
                    break;
                case R.id.nav_lostfound:
                    replaceFragment(new LostnFoundFragment(), "lostfound");
                    break;
            }
            return true;
        });

        fadeBackground.setOnClickListener(v -> {
            if (isMenuVisible) {
                animateHideFabMenu();
            }
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        stopAllAnimations();
        hideAllMenusInstant();
        isMenuVisible = false;

        currentTag = tag;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commitNowAllowingStateLoss();

        updateFabMenuState();
        updateHeaderVisibility(tag);
    }

    private void toggleMenu() {
        if (isMenuVisible) {
            animateHideFabMenu();
        } else {
            animateShowFabMenu();
        }
    }

    private void hideMenu() {
        LinearLayout currentMenu = getMenuByTag(currentTag);
        if (currentMenu == null) return;

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);
        fadeBackground.startAnimation(fadeOut);
        currentMenu.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation animation) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                fadeBackground.setVisibility(View.GONE);
                currentMenu.setVisibility(View.INVISIBLE);
            }
        });

        isMenuVisible = false;
    }

    private LinearLayout getMenuByTag(String tag) {
        switch (tag) {
            case "home":
                return fabMenuHome;
            case "competition":
                return fabMenuKompetisi;
            case "event":
                return fabMenuEvent;
            case "lostfound":
                return fabMenuLostFound;
            default:
                return null;
        }
    }

    private void hideAllMenusInstant() {
        if (fabMenuHome != null) fabMenuHome.setVisibility(View.GONE);
        if (fabMenuEvent != null) fabMenuEvent.setVisibility(View.GONE);
        if (fabMenuKompetisi != null) fabMenuKompetisi.setVisibility(View.GONE);
        if (fabMenuLostFound != null) fabMenuLostFound.setVisibility(View.GONE);
        if (fadeBackground != null) fadeBackground.setVisibility(View.GONE);
        isMenuVisible = false;
    }

    private void stopAllAnimations() {
        if (fabMenuHome != null) fabMenuHome.clearAnimation();
        if (fabMenuEvent != null) fabMenuEvent.clearAnimation();
        if (fabMenuKompetisi != null) fabMenuKompetisi.clearAnimation();
        if (fabMenuLostFound != null) fabMenuLostFound.clearAnimation();
        if (fadeBackground != null) fadeBackground.clearAnimation();
    }

    private void animateShowFabMenu() {
        LinearLayout currentMenu = getMenuByTag(currentTag);
        if (currentMenu == null || isFinishing()) return;

        stopAllAnimations();

        fadeBackground.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(200);
        fadeIn.setFillAfter(true);

        fadeBackground.startAnimation(fadeIn);
        currentMenu.setVisibility(View.VISIBLE);
        currentMenu.startAnimation(fadeIn);
        btnAdd.animate().rotation(45f).setDuration(200).start();

        btnAddContainer.setBackgroundTintList(ColorStateList.valueOf(colorAddActive));

        isMenuVisible = true;
    }

    private void animateHideFabMenu() {
        LinearLayout currentMenu = getMenuByTag(currentTag);
        if (currentMenu == null) return;

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(200);
        fadeBackground.startAnimation(fadeOut);
        currentMenu.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation animation) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                fadeBackground.setVisibility(View.GONE);
                currentMenu.setVisibility(View.GONE);
            }
        });

        btnAdd.animate().rotation(0f).setDuration(200).start();

        btnAddContainer.setBackgroundTintList(ColorStateList.valueOf(colorAddDefault));

        isMenuVisible = false;
    }

    private void updateFabMenuState() {
        LinearLayout currentMenu = getMenuByTag(currentTag);
        if (currentMenu == null) return;

        hideAllMenusInstant();

        int childCount = currentMenu.getChildCount();

        if (childCount == 1) {
            currentMenu.setVisibility(View.VISIBLE);
            btnAddContainer.setVisibility(View.GONE);

            currentMenu.setTranslationY(55f);
            fadeBackground.setVisibility(View.GONE);
            isMenuVisible = false;
            btnAdd.setRotation(0f);

        } else if (childCount > 1) {
            btnAddContainer.setVisibility(View.VISIBLE);
            currentMenu.setVisibility(View.GONE);
            fadeBackground.setVisibility(View.GONE);
            isMenuVisible = false;
        } else {
            btnAddContainer.setVisibility(View.GONE);
            fadeBackground.setVisibility(View.GONE);
        }
    }
    private void updateHeaderVisibility(String tag) {
        if (headerUtama == null || headerSecond == null || headerThird == null) return;
        headerUtama.setVisibility(View.GONE);
        headerSecond.setVisibility(View.GONE);
        headerThird.setVisibility(View.GONE);
        switch (tag) {
            case "home":
                headerUtama.setVisibility(View.VISIBLE);
                break;
            case "event":
                headerThird.setVisibility(View.VISIBLE);
                break;
            default:
                headerSecond.setVisibility(View.VISIBLE);
                break;
        }
    }

}
