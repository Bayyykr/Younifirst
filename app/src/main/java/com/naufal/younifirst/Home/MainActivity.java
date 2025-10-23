package com.naufal.younifirst.Home;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.naufal.younifirst.R;
import com.naufal.younifirst.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private LinearLayout fabMenuHome, fabMenuEvent, fabMenuForum;
    private ImageView btnAdd;
    private View fadeBackground;
    private boolean isMenuVisible = false;
    private String currentTag = "home";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fabMenuHome = findViewById(R.id.fabMenuHome);
        fabMenuEvent = findViewById(R.id.fabMenuEvent);
//        fabMenuForum = findViewById(R.id.fabMenuForum);
        btnAdd = findViewById(R.id.btnAdd);
        fadeBackground = findViewById(R.id.globalFadeBackground);

        hideAllMenusInstant();

        replaceFragment(new HomeFragment(), "home");

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
    }

    private void replaceFragment(Fragment fragment, String tag) {
        currentTag = tag;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();

        hideAllMenusInstant();

        switch (tag) {
            case "home":
                fabMenuHome.setVisibility(View.INVISIBLE);
                break;
            case "event":
                fabMenuEvent.setVisibility(View.INVISIBLE);
                break;
//            case "forum":
//                fabMenuForum.setVisibility(View.INVISIBLE);
//                break;
        }
    }

    private void toggleMenu() {
        if (isMenuVisible) hideMenu();
        else showMenu();
    }

    private void showMenu() {
        LinearLayout currentMenu = getMenuByTag(currentTag);
        if (currentMenu == null) return;

        fadeBackground.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(200);
        fadeBackground.startAnimation(fadeIn);

        currentMenu.setVisibility(View.VISIBLE);
        currentMenu.startAnimation(fadeIn);

        isMenuVisible = true;
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
            case "event":
                return fabMenuEvent;
//            case "forum":
//                return fabMenuForum;
            default:
                return null;
        }
    }

    private void hideAllMenusInstant() {
        if (fabMenuHome != null) fabMenuHome.setVisibility(View.INVISIBLE);
        if (fabMenuEvent != null) fabMenuEvent.setVisibility(View.INVISIBLE);
//        if (fabMenuForum != null) fabMenuForum.setVisibility(View.INVISIBLE);
        if (fadeBackground != null) fadeBackground.setVisibility(View.GONE);
        isMenuVisible = false;
    }
}
